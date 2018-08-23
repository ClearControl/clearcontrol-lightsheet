package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * HybridInterleavedOpticsPrefusedAcquisitionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class HybridInterleavedOpticsPrefusedAcquisitionInstruction  extends
        AbstractAcquistionInstruction implements
        InstructionInterface,
        LoggingFeature
{

    /**
     * INstanciates a virtual device with a given name
     */
    public HybridInterleavedOpticsPrefusedAcquisitionInstruction(LightSheetMicroscope pLightSheetMicroscope)
    {
        super("Acquisition: Hybrid interleaved/optics-prefused", pLightSheetMicroscope);
        mChannelName.set("interleaved");
    }

    @Override public boolean enqueue(long pTimePoint)
    {
        mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

        int lImageWidth = mCurrentState.getImageWidthVariable().get().intValue();
        int lImageHeight = mCurrentState.getImageHeightVariable().get().intValue();
        double lExposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

        int lNumberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

        // build a queue
        LightSheetMicroscopeQueue
                lQueue =
                getLightSheetMicroscope().requestQueue();

        // initialize queue
        lQueue.clearQueue();
        lQueue.setCenteredROI(lImageWidth, lImageHeight);

        lQueue.setExp(lExposureTimeInSeconds);

        // initial position
        goToInitialPosition(getLightSheetMicroscope(),
                lQueue,
                mCurrentState.getStackZLowVariable().get().doubleValue(),
                mCurrentState.getStackZLowVariable().get().doubleValue());

        // --------------------------------------------------------------------
        // build a queue

        for (int lImageCounter = 0; lImageCounter
                < lNumberOfImagesToTake; lImageCounter++)
        {
            // acuqire an image per light sheet + one more
            for (int l = 0; l
                    < getLightSheetMicroscope().getNumberOfLightSheets() + 1; l++)
            {
                mCurrentState.applyAcquisitionStateAtStackPlane(lQueue,
                        lImageCounter);

                // configure light sheets accordingly
                for (int k = 0; k
                        < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
                {
                    lQueue.setI(k, l == getLightSheetMicroscope().getNumberOfLightSheets());
                }
                if (l < getLightSheetMicroscope().getNumberOfLightSheets()) {
                    lQueue.setI(l, true);
                }

                lQueue.addCurrentStateToQueue();
            }
        }

        // back to initial position
        goToInitialPosition(getLightSheetMicroscope(),
                lQueue,
                mCurrentState.getStackZLowVariable().get().doubleValue(),
                mCurrentState.getStackZLowVariable().get().doubleValue());

        lQueue.setTransitionTime(0.5);
        lQueue.setFinalisationTime(0.005);

        for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
        {
            StackMetaData
                    lMetaData =
                    lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

            lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                    AcquisitionType.TimeLapseHybridInterleavedOpticsPrefused);
            lMetaData.addEntry(MetaDataView.Camera, c);

            lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

            lMetaData.addEntry(MetaDataChannel.Channel,  "hybrid_interleaved_opticsprefused");
        }
        lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), mCurrentState.getStackZStepVariable().get().doubleValue());
        lQueue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                pTimePoint);

        lQueue.finalizeQueue();

        // acquire!
        boolean lPlayQueueAndWait = false;
        try
        {
            mTimeStampBeforeImaging = System.nanoTime();
            lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWait(lQueue,
                    100 + lQueue
                            .getQueueLength(),
                    TimeUnit.SECONDS);

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
        }

        if (!lPlayQueueAndWait)
        {
            System.out.print("Error while imaging");
            return false;
        }

        // Store results in the DataWarehouse
        HybridInterleavedOpticsPrefusedImageDataContainer lContainer = new HybridInterleavedOpticsPrefusedImageDataContainer(getLightSheetMicroscope());
        for (int d = 0 ; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
        {
            StackInterface lStack = getLightSheetMicroscope().getCameraStackVariable(
                    d).get();


            putStackInContainer("C" + d + "hybrid_interleaved_opticsprefused", lStack, lContainer);
        }
        getLightSheetMicroscope().getDataWarehouse().put("hybrid_interleaved_opticsprefused_raw_" + pTimePoint, lContainer);

        return true;
    }

    @Override
    public HybridInterleavedOpticsPrefusedAcquisitionInstruction copy() {
        return new HybridInterleavedOpticsPrefusedAcquisitionInstruction(getLightSheetMicroscope());
    }

}
