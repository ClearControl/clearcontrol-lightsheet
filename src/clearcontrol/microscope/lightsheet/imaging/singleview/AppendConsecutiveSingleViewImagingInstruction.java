package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerInstruction;
import clearcontrol.stack.StackInterface;

import java.util.ArrayList;

/**
 * AppendConsecutiveSingleViewImagingInstruction appends a list of imaging, fusion and io schedulers at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveSingleViewImagingInstruction extends InstructionBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;
    private final int mLightSheetIndex;
    private final int mDetectionArmIndex;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveSingleViewImagingInstruction(int pDetectionArmIndex, int pLightSheetIndex, int pNumberOfImages, double pIntervalInSeconds) {
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
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new SingleViewAcquisitionInstruction(mDetectionArmIndex, mLightSheetIndex));
            index++;
            schedule.add(index, new WriteSingleLightSheetImageAsRawToDiscInstruction(mDetectionArmIndex, mLightSheetIndex));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,true));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,false));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(StackInterface.class));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }
}
