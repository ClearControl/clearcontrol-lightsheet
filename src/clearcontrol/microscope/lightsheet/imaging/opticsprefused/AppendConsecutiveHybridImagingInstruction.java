package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerInstruction;

import java.util.ArrayList;

/**
 * AppendConsecutiveHyperDriveImagingInstruction appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveHybridImagingInstruction extends InstructionBase implements LoggingFeature {

    private final int mNumberOfImages;
    private final double mFirstHalfIntervalInSeconds;
    private final double mSecondHalfIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveHybridImagingInstruction(int pNumberOfImages, double pFirstHalfIntervalInSeconds, double pSecondHalfIntervalInSeconds) {
        super("Smart: Append a Hybrid (Hyperdrive, opticsprefused) scan with " + pNumberOfImages + " images every (" + pFirstHalfIntervalInSeconds + ", " + pSecondHalfIntervalInSeconds + ") s to the schedulers"  );
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
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        String timeMeasurementKey = "HyperDrive_" + System.currentTimeMillis();

                LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int numberOfImagesFirstHalf = mNumberOfImages / 2;
        int numberOfImagesSecondHalf = mNumberOfImages - numberOfImagesFirstHalf;

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        // while the first half, images are only taken
        for (int i = 0; i < numberOfImagesFirstHalf; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionInstruction());
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mFirstHalfIntervalInSeconds * 1000)));
            index++;
        }
        // while the second half, one image is taken and two are fused/saved
        for (int i = 0; i < numberOfImagesSecondHalf; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionInstruction());
            index++;

            schedule.add(index, new OpticsPrefusedFusionInstruction());
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(OpticsPrefusedImageDataContainer.class));
            index++;
            schedule.add(index, new WriteFusedImageAsRawToDiscInstruction("opticsprefused"));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class));
            index++;

            if (i < numberOfImagesFirstHalf) {
                schedule.add(index, new OpticsPrefusedFusionInstruction());
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerInstruction(OpticsPrefusedImageDataContainer.class));
                index++;
                schedule.add(index, new WriteFusedImageAsRawToDiscInstruction("opticsprefused"));
                index++;
                schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true));
                index++;
                schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false));
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class));
                index++;
            }

            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mSecondHalfIntervalInSeconds * 1000)));
            index++;

        }
        return true;
    }
}
