package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.HalfStackMaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropOldestStackInterfaceContainerInstruction;
import clearcontrol.stack.StackInterface;

import java.util.ArrayList;

/**
 * AppendConsecutiveSingleViewImagingInstruction appends a list of imaging, fusion and io instructions at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveSingleViewImagingInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;
    private final int mLightSheetIndex;
    private final int mDetectionArmIndex;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveSingleViewImagingInstruction(int pDetectionArmIndex, int pLightSheetIndex, int pNumberOfImages, double pIntervalInSeconds, LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Append a single view scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the instructions", pLightSheetMicroscope);
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
        String timeMeasurementKey = "sequential_" + System.currentTimeMillis();

        LightSheetTimelapse lTimelapse = getLightSheetMicroscope().getTimelapse();
        ArrayList<InstructionInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        for (int i = 0; i < mNumberOfImages; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new SingleViewAcquisitionInstruction(mDetectionArmIndex, mLightSheetIndex, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new WriteSingleLightSheetImageAsRawToDiscInstruction(mDetectionArmIndex, mLightSheetIndex, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,true, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,false, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(StackInterface.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }

    @Override
    public AppendConsecutiveSingleViewImagingInstruction copy(){
        return new AppendConsecutiveSingleViewImagingInstruction(mDetectionArmIndex, mLightSheetIndex, mNumberOfImages, mIntervalInSeconds, getLightSheetMicroscope());
    }
}
