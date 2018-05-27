package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.HalfStackMaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropOldestStackInterfaceContainerInstruction;

import java.util.ArrayList;

/**
 * AppendConsecutiveHyperDriveImagingInstruction appends a list of imaging, fusion and io instructions at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveHybridImagingInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private final int mNumberOfImages;
    private final double mFirstHalfIntervalInSeconds;
    private final double mSecondHalfIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveHybridImagingInstruction(int pNumberOfImages, double pFirstHalfIntervalInSeconds, double pSecondHalfIntervalInSeconds, LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Append a Hybrid (Hyperdrive, opticsprefused) scan with " + pNumberOfImages + " images every (" + pFirstHalfIntervalInSeconds + ", " + pSecondHalfIntervalInSeconds + ") s to the instructions", pLightSheetMicroscope);
        mNumberOfImages = pNumberOfImages;
        mFirstHalfIntervalInSeconds = pFirstHalfIntervalInSeconds;
        mSecondHalfIntervalInSeconds = pSecondHalfIntervalInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        String timeMeasurementKey = "HyperDrive_" + System.currentTimeMillis();

                LightSheetTimelapse lTimelapse = getLightSheetMicroscope().getTimelapse();
        ArrayList<InstructionInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int numberOfImagesFirstHalf = mNumberOfImages / 2;
        int numberOfImagesSecondHalf = mNumberOfImages - numberOfImagesFirstHalf;

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        // while the first half, images are only taken
        for (int i = 0; i < numberOfImagesFirstHalf; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionInstruction(getLightSheetMicroscope()));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mFirstHalfIntervalInSeconds * 1000)));
            index++;
        }
        // while the second half, one image is taken and two are fused/saved
        for (int i = 0; i < numberOfImagesSecondHalf; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionInstruction(getLightSheetMicroscope()));
            index++;

            schedule.add(index, new OpticsPrefusedFusionInstruction(getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(OpticsPrefusedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;
            schedule.add(index, new WriteFusedImageAsRawToDiscInstruction("opticsprefused", getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;

            if (i < numberOfImagesFirstHalf) {
                schedule.add(index, new OpticsPrefusedFusionInstruction(getLightSheetMicroscope()));
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerInstruction(OpticsPrefusedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
                index++;
                schedule.add(index, new WriteFusedImageAsRawToDiscInstruction("opticsprefused", getLightSheetMicroscope()));
                index++;
                schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true, getLightSheetMicroscope()));
                index++;
                schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false, getLightSheetMicroscope()));
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
                index++;
            }

            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mSecondHalfIntervalInSeconds * 1000)));
            index++;

        }
        return true;
    }

    @Override
    public AppendConsecutiveHybridImagingInstruction copy() {
        return new AppendConsecutiveHybridImagingInstruction(mNumberOfImages, mFirstHalfIntervalInSeconds, mSecondHalfIntervalInSeconds, getLightSheetMicroscope());
    }
}
