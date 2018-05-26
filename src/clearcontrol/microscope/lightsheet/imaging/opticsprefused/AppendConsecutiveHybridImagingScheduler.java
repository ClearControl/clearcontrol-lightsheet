package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.instructions.implementations.MeasureTimeScheduler;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionScheduler;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscScheduler;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerScheduler;

import java.util.ArrayList;

/**
 * AppendConsecutiveHyperDriveImagingScheduler appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveHybridImagingScheduler extends SchedulerBase implements LoggingFeature {

    private final int mNumberOfImages;
    private final double mFirstHalfIntervalInSeconds;
    private final double mSecondHalfIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveHybridImagingScheduler(int pNumberOfImages, double pFirstHalfIntervalInSeconds, double pSecondHalfIntervalInSeconds) {
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
            schedule.add(index, new MeasureTimeScheduler(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionScheduler());
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeScheduler(timeMeasurementKey, (long)(mFirstHalfIntervalInSeconds * 1000)));
            index++;
        }
        // while the second half, one image is taken and two are fused/saved
        for (int i = 0; i < numberOfImagesSecondHalf; i ++) {
            schedule.add(index, new MeasureTimeScheduler(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionScheduler());
            index++;

            schedule.add(index, new OpticsPrefusedFusionScheduler());
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerScheduler(OpticsPrefusedImageDataContainer.class));
            index++;
            schedule.add(index, new WriteFusedImageAsRawToDiscScheduler("opticsprefused"));
            index++;
            schedule.add(index, new HalfStackMaxProjectionScheduler<FusedImageDataContainer>(FusedImageDataContainer.class,true));
            index++;
            schedule.add(index, new HalfStackMaxProjectionScheduler<FusedImageDataContainer>(FusedImageDataContainer.class,false));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerScheduler(FusedImageDataContainer.class));
            index++;

            if (i < numberOfImagesFirstHalf) {
                schedule.add(index, new OpticsPrefusedFusionScheduler());
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerScheduler(OpticsPrefusedImageDataContainer.class));
                index++;
                schedule.add(index, new WriteFusedImageAsRawToDiscScheduler("opticsprefused"));
                index++;
                schedule.add(index, new HalfStackMaxProjectionScheduler<FusedImageDataContainer>(FusedImageDataContainer.class,true));
                index++;
                schedule.add(index, new HalfStackMaxProjectionScheduler<FusedImageDataContainer>(FusedImageDataContainer.class,false));
                index++;
                schedule.add(index, new DropOldestStackInterfaceContainerScheduler(FusedImageDataContainer.class));
                index++;
            }

            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeScheduler(timeMeasurementKey, (long)(mSecondHalfIntervalInSeconds * 1000)));
            index++;

        }
        return true;
    }
}
