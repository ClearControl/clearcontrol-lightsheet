package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.MeasureTimeScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.PauseUntilTimeAfterMeasuredTimeScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionScheduler;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscScheduler;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerScheduler;

import java.util.ArrayList;

/**
 * AppendConsecutiveOpticsPrefusedImagingScheduler appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveOpticsPrefusedImagingScheduler extends SchedulerBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveOpticsPrefusedImagingScheduler(int pNumberOfImages, double pIntervalInSeconds) {
        super("Smart: Append an optics-prefused scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the schedulers"  );
        mNumberOfImages = pNumberOfImages;
        mIntervalInSeconds = pIntervalInSeconds;
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

        String timeMeasurementKey = "opticsprefused_" + System.currentTimeMillis();

        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        for (int i = 0; i < mNumberOfImages; i ++) {
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
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeScheduler(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }
}
