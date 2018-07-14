package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.imagej.ImageJFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.imaging.singleview.WriteSingleLightSheetImageAsTifToDiscInstruction;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.postprocessing.processing.CropInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropAllContainersOfTypeInstruction;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.stack.StackInterface;
import net.imglib2.RandomAccess;
import org.ejml.data.DenseMatrix64F;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class SensorLessAOForSinglePlaneInstruction extends LightSheetMicroscopeInstructionBase implements ImageJFeature, LoggingFeature{


    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

    private BoundedVariable<Double> mPositionZ = new BoundedVariable<Double>("position Z",
            50.0,0.0,100.0);
    private BoundedVariable<Double> mStepSize = new BoundedVariable<Double>("Defocus step size",
            0.25, 0.0, 2.0, 0.0000000001);
    private BoundedVariable<Integer> mZernikeFactor = new BoundedVariable<Integer>("Zernike Factor",
            3,0,66);
    private BoundedVariable<Integer> mNumberOfTilesX = new BoundedVariable<Integer>("Number Of Tiles On X",
            1,0,2048);
    private BoundedVariable<Integer> mNumberOfTilesY = new BoundedVariable<Integer>("Number Of Tiles On Y",
            1,0,2048);;
    private ClearCLIJ clij = ClearCLIJ.getInstance();

    private double[] zernikes;
    private int mTileHeight = 0;
    private int mTileWidth = 0;

    WriteSingleLightSheetImageAsTifToDiscInstruction lWrite =  new WriteSingleLightSheetImageAsTifToDiscInstruction(
            0, 0, getLightSheetMicroscope());

    public SensorLessAOForSinglePlaneInstruction(LightSheetMicroscope pLightSheetMicroscope,
                                                 SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface)
    {
        super("Adaptive optics: Sensorless Single PLane AO optimizer for " +
                pSpatialPhaseModulatorDeviceInterface.getName(), pLightSheetMicroscope);
        this.mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mStepSize.set(0.25);
        mZernikeFactor.set(3);
        mNumberOfTilesY.set(1);
        mNumberOfTilesX.set(1);
        mPositionZ.set(50.0);
    }


    @Override
    public boolean initialize() {
        //showImageJ();
        zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();
        for(int i = 0; i< Array.getLength(zernikes); i++){
            zernikes[i] = 0;
        }
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        zernikes = mSpatialPhaseModulatorDeviceInterface.getZernikeFactors();
        try {
            optimize();
        } catch (InterruptedException e) {
            System.out.println("Sleeping Error");
        }
        return false;
    }


    public boolean optimize() throws InterruptedException {

        zernikes[mZernikeFactor.get()] = 0;

        // Unchanged Zernike factor Imager
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        Thread.sleep(mSpatialPhaseModulatorDeviceInterface.getRelaxationTimeInMilliseconds());
        StackInterface lDefaultStack = image();

        mTileHeight = (int)lDefaultStack.getHeight()/mNumberOfTilesY.get();
        mTileWidth = (int)lDefaultStack.getWidth()/mNumberOfTilesX.get();

        double[][] lDefaultQuality = determineTileWiseQuality(lDefaultStack);
        lWrite.enqueue(mNumberOfTilesX.get()*mNumberOfTilesY.get());
        lDefaultStack.release();



        // decrease Zernike factor by step size
        double[] zernikesFactorDecreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorDecreased, 0, zernikes.length);
        zernikesFactorDecreased[mZernikeFactor.get()] -= mStepSize.get();
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorDecreased);
        Thread.sleep(mSpatialPhaseModulatorDeviceInterface.getRelaxationTimeInMilliseconds());
        StackInterface lFactorDecreasedStack = image();
        double[][] lFactorDecreasedQuality = determineTileWiseQuality(lFactorDecreasedStack);
        lFactorDecreasedStack.release();

        // increase Zernike factor by step size
        double[] zernikesFactorIncreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorIncreased, 0, zernikes.length);
        zernikesFactorIncreased[mZernikeFactor.get()] += mStepSize.get();
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorIncreased);
        Thread.sleep(mSpatialPhaseModulatorDeviceInterface.getRelaxationTimeInMilliseconds());
        StackInterface lFactorIncreasedStack = image();
        double[][] lFactorIncreasedQuality = determineTileWiseQuality(lFactorIncreasedStack);
        lFactorIncreasedStack.release();

        double decreasedValue = zernikesFactorDecreased[mZernikeFactor.get()];
        double increasedValue = zernikesFactorIncreased[mZernikeFactor.get()];
        double defaultValue = 0;

        // Tile wise maximum finding
        double[][] lMaxima = new double[mNumberOfTilesX.get()][mNumberOfTilesY.get()];
        for (int x = 0; x < mNumberOfTilesX.get(); x++)
        {
            for (int y = 0; y < mNumberOfTilesY.get(); y++)
            {
                double[] result = CalcParabolaVertex(decreasedValue,lFactorDecreasedQuality[x][y],defaultValue,
                        lDefaultQuality[x][y],increasedValue,lFactorIncreasedQuality[x][y]);
                if(result[0]>10 || result[0]<-10){
                    info("Optimizer trying to set extreme amount of optimization" + result[0]);
                    result[0]=0.0;
                }
                lMaxima[x][y] = result[0];

            }
        }

        System.out.println("Zernikes factor decreased state" + Arrays.toString(zernikesFactorDecreased));
        System.out.println("Zernikes factor increased state" + Arrays.toString(zernikesFactorIncreased));

        System.out.println("Zernikes default quality: " + Arrays.deepToString(lDefaultQuality));
        System.out.println("Zernikes factor decreased quality: " + Arrays.deepToString(lFactorDecreasedQuality));
        System.out.println("Zernikes factor increased quality: " + Arrays.deepToString(lFactorIncreasedQuality));

        System.out.println("Zernikes for maxima image quality: " + Arrays.deepToString(lMaxima));


        // Taking a stack of images with different mirror modes, crop it and save to disc
        acquireTiledImages(lMaxima);

        // Setting the zernike factor back to 0
        zernikes[mZernikeFactor.get()] = 0;
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);

        return true;
    }

    public void acquireTiledImages(double[][] pMaxima) throws InterruptedException {
        int lCounter = 0;
        double[][] lMaxima = pMaxima;
        DropAllContainersOfTypeInstruction lRemoveOldContainers = new DropAllContainersOfTypeInstruction
                (StackInterfaceContainer.class,getLightSheetMicroscope().getDataWarehouse());
        File lFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory();
        File lFile = new File(lFolder, "TileCoordinates.txt");

        try {
            BufferedWriter lOutputStream = new BufferedWriter(new FileWriter(lFile));
            lOutputStream.write("Counter\tCoordX\tCoordY\tWidth\tHeight\tBestAberrationCoeff\n");

            for (int x = 0; x < mNumberOfTilesX.get(); x++) {
                for (int y = 0; y < mNumberOfTilesY.get(); y++) {
                    zernikes[mZernikeFactor.get()] = lMaxima[x][y];
                    mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
                    Thread.sleep(mSpatialPhaseModulatorDeviceInterface.getRelaxationTimeInMilliseconds());
                    StackInterface lImage = image();
                    CropInstruction lCrop = new CropInstruction(getLightSheetMicroscope().getDataWarehouse(),
                            x *mTileWidth, y * mTileHeight ,mTileWidth, mTileHeight);
                    lCrop.enqueue(0);
                    lWrite.enqueue(lCounter);
                    lRemoveOldContainers.enqueue(lCounter);
                    lOutputStream.write(lCounter + "\t" + x *mTileWidth + "\t" + y * mTileHeight + "\t" +
                            mTileWidth + "\t" + mTileHeight + "\t" + String.format("%.5f", lMaxima[x][y]) + "\n" );
                    lCounter++;
                }
            }
            lOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StackInterface image() {
        InterpolatedAcquisitionState currentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().
                getDevice(AcquisitionStateManager.class, 0).getCurrentState();
        SingleViewPlaneImager lImager = new SingleViewPlaneImager(getLightSheetMicroscope(), mPositionZ.get());
        lImager.setImageWidth(currentState.getImageWidthVariable().get().intValue());
        lImager.setImageHeight(currentState.getImageHeightVariable().get().intValue());
        lImager.setExposureTimeInSeconds(currentState.getExposureInSecondsVariable().get().doubleValue());
        lImager.setDetectionArmIndex(0);
        lImager.setLightSheetIndex(0);
        StackInterface lStack = lImager.acquire();
        //ClearCLIJ.getInstance().show(lStack, "acquired stack");
        return lStack;
    }

    public double determineQuality(StackInterface lStack){
        DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator
                (lStack);
        double lQuality = lQualityEstimator.getQualityArray()[0];
        return lQuality;
    }

    public double[][] determineTileWiseQuality(StackInterface lStack){
        double[][] tilesQuality = new double[mNumberOfTilesX.get()][mNumberOfTilesY.get()];
        for (int x = 0; x < mNumberOfTilesX.get(); x++)
        {
            for (int y = 0; y < mNumberOfTilesY.get(); y++)
            {
                final StackInterface lTile = crop(lStack,x *mTileWidth, y * mTileHeight ,mTileHeight,mTileWidth);
                double focusMeasureValue = determineQuality(lTile);
                tilesQuality[x][y] = focusMeasureValue;
            }
        }
        return tilesQuality;
    }

    public StackInterface crop(StackInterface lStack, int lCropX, int lCropY, int lHieght, int lWidth){
        ClearCLImage src = clij.converter(lStack).getClearCLImage();
        ClearCLImage dst = clij.createCLImage(new long[]{lWidth, lHieght, lStack.getDepth()}, src.getChannelDataType());
        Kernels.crop(clij, src, dst, lCropX, lCropY, 0);
        //clij.show(dst, "Processing Quality On");

        StackInterface lCroppedStack = clij.converter(dst).getOffHeapPlanarStack();
        dst.close();
        src.close();
        return lCroppedStack;
    }



    //Checked
    public static double[] CalcParabolaVertex(double x1, double y1, double x2, double y2, double x3, double y3)
    {

        double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
        double A     = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
        double B     = (x3*x3 * (y1 - y2) + x2*x2 * (y3 - y1) + x1*x1 * (y2 - y3)) / denom;
        double C     = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;

        double xv = -B / (2*A);
        double yv = C - B*B / (4*A);

        double[] result = {xv,yv};

        return result;
    }

    public BoundedVariable<Double> getstepSize(){
        return mStepSize;
    }
    public BoundedVariable<Integer> getNumberOfTilesX(){
        return mNumberOfTilesX;
    }
    public BoundedVariable<Integer> getmNumberOfTilesY(){
        return mNumberOfTilesY;
    }
    public BoundedVariable<Double> getPositionZ(){ return mPositionZ; }
    public BoundedVariable<Integer> getZernikeFactor(){
        return mZernikeFactor;
    }

    @Override
    public clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.SensorLessAOForSinglePlaneInstruction copy() {
        return new clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.SensorLessAOForSinglePlaneInstruction(getLightSheetMicroscope(), mSpatialPhaseModulatorDeviceInterface);
    }

}

