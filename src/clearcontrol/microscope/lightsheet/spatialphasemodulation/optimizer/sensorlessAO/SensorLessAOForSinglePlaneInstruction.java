package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.imaging.singleview.WriteSingleLightSheetImageAsTifToDiscInstruction;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.stack.StackInterface;
import net.imglib2.RandomAccess;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class SensorLessAOForSinglePlaneInstruction extends LightSheetMicroscopeInstructionBase{


    private BoundedVariable<Integer> mZernikeFactor = new BoundedVariable<Integer>("Zernike Factor",3,0,66);
    private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private BoundedVariable<Double> mPositionZ;
    double[] zernikes;
    private BoundedVariable<Double> mStepSize = new BoundedVariable<Double>("Defocus step size",0.25, 0.0, 2.0, 0.0000000001);

    private BoundedVariable<Integer> mNumberOfTilesX = new BoundedVariable<Integer>("Number Of Tiles On X",1,0,2048);
    private BoundedVariable<Integer> mNumberOfTilesY = new BoundedVariable<Integer>("Number Of Tiles On Y",1,0,2048);;

    public SensorLessAOForSinglePlaneInstruction(LightSheetMicroscope pLightSheetMicroscope, SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {
        super("Adaptive optics: Sensorless Single PLane AO optimizer for " + pSpatialPhaseModulatorDeviceInterface.getName(), pLightSheetMicroscope);
        this.mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mStepSize.set(0.25);
        mZernikeFactor.set(3);
        mNumberOfTilesY.set(1);
        mNumberOfTilesX.set(1);
    }


    @Override
    public boolean initialize() {
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
        mPositionZ = new BoundedVariable<Double>("position Z", (lState.getStackZLowVariable().get().doubleValue() + lState.getStackZHighVariable().get().doubleValue()) / 2, lState.getStackZLowVariable().getMin().doubleValue(), lState.getStackZHighVariable().getMax().doubleValue(), lState.getStackZLowVariable().getGranularity().doubleValue());

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
        optimize();
        return false;
    }


    public boolean optimize(){

        zernikes[mZernikeFactor.get()] = 0;

        // Unchanged Zernike factor Imager
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        StackInterface lDefaultStack = image();
        double[][] lDefaultQuality = determineTileWiseQuality(lDefaultStack);


        // decrease Zernike factor by step size
        double[] zernikesFactorDecreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorDecreased, 0, zernikes.length);
        zernikesFactorDecreased[mZernikeFactor.get()] -= mStepSize.get();
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorDecreased);
        StackInterface lFactorDecreasedStack = image();
        double[][] lFactorDecreasedQuality = determineTileWiseQuality(lFactorDecreasedStack);

        // increase Zernike factor by step size
        double[] zernikesFactorIncreased = new double[zernikes.length];
        System.arraycopy(zernikes, 0, zernikesFactorIncreased, 0, zernikes.length);
        zernikesFactorIncreased[mZernikeFactor.get()] += mStepSize.get();
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikesFactorIncreased);
        StackInterface lFactorIncreasedStack = image();
        double[][] lFactorIncreasedQuality = determineTileWiseQuality(lFactorIncreasedStack);


        double dec = zernikesFactorDecreased[mZernikeFactor.get()];
        double inc = zernikesFactorIncreased[mZernikeFactor.get()];
        double def = 0;
        // Tile wise maximum finding
        double[][] lMaxima = new double[mNumberOfTilesX.get()][mNumberOfTilesY.get()];
        for (int x = 0; x < mNumberOfTilesX.get(); x++)
        {
            for (int y = 0; y < mNumberOfTilesY.get(); y++)
            {
                double[] result = CalcParabolaVertex(dec,lFactorDecreasedQuality[x][y],def,lDefaultQuality[x][y],inc,lFactorIncreasedQuality[x][y]);
                if(result[0]>10){
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



        // TODO Check rest of the code on actual scope before running this code
        // Taking a stack of images with different mirror modes
//        WriteSingleLightSheetImageAsTifToDiscInstruction lWrite =  new WriteSingleLightSheetImageAsTifToDiscInstruction(0, 0, getLightSheetMicroscope());
//        for (int x = 0; x < mNumberOfTilesX.get(); x++) {
//            for (int y = 0; y < mNumberOfTilesY.get(); y++) {
//                zernikes[mZernikeFactor.get()] = lMaxima[x][y];
//                mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
//                StackInterface lImage = image();
//                lWrite.enqueue(x*10000 + y);
//                System.out.println(x);
//            }
//        }

        // Setting back to 0
        zernikes[mZernikeFactor.get()] = 0;
        mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikes);
        return true;
    }

    public StackInterface image(){
        InterpolatedAcquisitionState currentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getDevice(AcquisitionStateManager.class, 0).getCurrentState();
        SingleViewPlaneImager lImager = new SingleViewPlaneImager(getLightSheetMicroscope(), mPositionZ.get());
        lImager.setImageWidth(currentState.getImageWidthVariable().get().intValue());
        lImager.setImageHeight(currentState.getImageHeightVariable().get().intValue());
        lImager.setExposureTimeInSeconds(currentState.getExposureInSecondsVariable().get().doubleValue());
        lImager.setDetectionArmIndex(0);
        lImager.setLightSheetIndex(0);
        StackInterface lStack = lImager.acquire();
        return lStack;
    }

    public double determineQuality(StackInterface lStack){
        DiscreteConsinusTransformEntropyPerSliceEstimator lQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
        double lQuality = lQualityEstimator.getQualityArray()[0];
        return lQuality;
    }


    public StackInterface crop(StackInterface lStack, int lCropX, int lCropY, int lHieght, int lWidth){
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage src = clij.converter(lStack).getClearCLImage();
        ClearCLImage dst = clij.createCLImage(new long[]{lWidth, lHieght},
                src.getChannelDataType());
        Kernels.crop(clij, src, dst, lCropX, lCropY);
        //clij.show(dst, "Processing Quality On");

        StackInterface lCroppedStack = clij.converter(dst).getOffHeapPlanarStack();
        dst.close();
        src.close();
        return lCroppedStack;
    }

    public double[][] determineTileWiseQuality(StackInterface lStack){
        int lTileHeight = (int)lStack.getHeight()/mNumberOfTilesY.get();
        int lTileWidth = (int)lStack.getWidth()/mNumberOfTilesX.get();
        double[][] tilesQulaity = new double[mNumberOfTilesX.get()][mNumberOfTilesY.get()];
        for (int x = 0; x < mNumberOfTilesX.get(); x++)
        {
            for (int y = 0; y < mNumberOfTilesY.get(); y++)
            {
                final StackInterface lTile = crop(lStack,x *lTileWidth, y * lTileHeight ,lTileHeight,lTileWidth);
                double focusMeasureValue = determineQuality(lTile);
                tilesQulaity[x][y] = focusMeasureValue;
            }
        }
        return tilesQulaity;
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
//    public BoundedVariable<Double> getPositionZ(){
//        return mPositionZ;
//    }
    public BoundedVariable<Integer> getZernikeFactor(){
        return mZernikeFactor;
    }

    @Override
    public clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.SensorLessAOForSinglePlaneInstruction copy() {
        return new clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.SensorLessAOForSinglePlaneInstruction(getLightSheetMicroscope(), mSpatialPhaseModulatorDeviceInterface);
    }

}
