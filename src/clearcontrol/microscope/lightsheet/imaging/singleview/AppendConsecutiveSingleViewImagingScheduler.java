package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.instructions.implementations.MeasureTimeScheduler;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionScheduler;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerScheduler;
import clearcontrol.stack.StackInterface;

import java.util.ArrayList;

/**
 * AppendConsecutiveSingleViewImagingScheduler appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveSingleViewImagingScheduler extends SchedulerBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;
    private final int mLightSheetIndex;
    private final int mDetectionArmIndex;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveSingleViewImagingScheduler(int pDetectionArmIndex,int pLightSheetIndex, int pNumberOfImages, double pIntervalInSeconds) {
        super("Smart: Append a single view scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the schedulers"  );
        mNumberOfImages = pNumberOfImages;
        mIntervalInSeconds = pIntervalInSeconds;
        mLightSheetIndex = pLightSheetIndex;
        mDetectionArmIndex = pDetectionArmIndex;
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

        String timeMeasurementKey = "sequential_" + System.currentTimeMillis();

        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        for (int i = 0; i < mNumberOfImages; i ++) {
            schedule.add(index, new MeasureTimeScheduler(timeMeasurementKey));
            index++;
            schedule.add(index, new SingleViewAcquisitionScheduler(mDetectionArmIndex, mLightSheetIndex));
            index++;
            schedule.add(index, new WriteSingleLightSheetImageAsRawToDiscScheduler(mDetectionArmIndex, mLightSheetIndex));
            index++;
            schedule.add(index, new HalfStackMaxProjectionScheduler<StackInterfaceContainer>(StackInterfaceContainer.class,true));
            index++;
            schedule.add(index, new HalfStackMaxProjectionScheduler<StackInterfaceContainer>(StackInterfaceContainer.class,false));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerScheduler(StackInterface.class));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeScheduler(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }
}
