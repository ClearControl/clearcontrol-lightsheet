package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.calibrator.gui.ImageJOverlayViewer;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.imaging.SingleStackImager;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.zernike.ZernikeSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefocusDiversityInstruction extends AbstractAcquistionInstruction {

    private BoundedVariable<Double> mStepSize = new BoundedVariable<Double>("Defocus step size",5.0, 0.0, Double.MAX_VALUE, 0.0000000001);

    private LightSheetMicroscope mLightSheetMicroscope;
    private StackInterface mResultImage;


    public DefocusDiversityInstruction(LightSheetMicroscope pLightSheetMicroscope, double pStepSize) {
        super("Adaptive optics: Defocus Diversity", pLightSheetMicroscope);
        mStepSize.set(pStepSize);
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        image();
        return false;
    }

    public boolean image(){

        LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
        lQueue.clearQueue();
        int pImageWidth = 2048;
        int pImageHeight = 2048;

        lQueue.setFullROI();
        lQueue.setCenteredROI(pImageWidth, pImageHeight);

        lQueue.setExp(0.5);

        // reset everything
        for (int i = 0; i < mLightSheetMicroscope.getNumberOfLightSheets(); i++)
        {
            lQueue.setI(i, false);
        }

        lQueue.setI(0, true);
        lQueue.setIZ(0, 50.0);

        lQueue.setDZ(0, 50 - mStepSize.get());


        lQueue.setC(0, true);

        lQueue.addCurrentStateToQueue();
        lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                mLightSheetMicroscope.getTimelapse().getTimePointCounterVariable().get());

        lQueue.setDZ(0, 50);
        lQueue.addCurrentStateToQueue();
        lQueue.setDZ(0, 50 + mStepSize.get());
        lQueue.addCurrentStateToQueue();

        lQueue.setTransitionTime(0.1);

        lQueue.finalizeQueue();

        info("Acquiring 3 images for defocus diversity");
        mLightSheetMicroscope.useRecycler("adaptation", 1, 4, 4);
        final Boolean lPlayQueueAndWait;
        try
        {
            lPlayQueueAndWait = mLightSheetMicroscope.playQueueAndWaitForStacks(lQueue,
                    100 + lQueue.getQueueLength(),
                    TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
            return false;
        }

        if (!lPlayQueueAndWait)
        {
            return false;
        }
        mResultImage = mLightSheetMicroscope.getCameraStackVariable(0)
                .get();
        if (mResultImage == null) {
            System.out.println("Null Image in Stack");
            return false;
        }
        SequentialImageDataContainer lContainer = new SequentialImageDataContainer(mLightSheetMicroscope);


        putStackInContainer("C" + 0 + "L" + 0, mResultImage, lContainer);
        getLightSheetMicroscope().getDataWarehouse().put("sequential_raw_" + mLightSheetMicroscope.getTimelapse().getTimePointCounterVariable().get(), lContainer);

        return true;
    }

    @Override
    public clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity.DefocusDiversityInstruction copy() {
        return new clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity.DefocusDiversityInstruction(getLightSheetMicroscope(), 5.0);
    }

}
