package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
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
import scala.Int;

import java.util.ArrayList;

/**
 * AppendConsecutiveSingleViewImagingInstruction appends a list of imaging, fusion and io instructions at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveSingleViewImagingInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private final BoundedVariable<Integer> mNumberOfImages = new BoundedVariable<Integer>("Number of images", 100);
    private final BoundedVariable<Double> mIntervalInSeconds = new BoundedVariable<Double>("Frame delay in s", 15.0);

    private final BoundedVariable<Integer> mLightSheetIndex;
    private final BoundedVariable<Integer> mDetectionArmIndex;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveSingleViewImagingInstruction(int pDetectionArmIndex, int pLightSheetIndex, int pNumberOfImages, double pIntervalInSeconds, LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Append a single view scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the instructions", pLightSheetMicroscope);
        mNumberOfImages.set(pNumberOfImages);
        mIntervalInSeconds.set(pIntervalInSeconds);
        mLightSheetIndex = new BoundedVariable<Integer>("Light sheet", pLightSheetIndex, 0, pLightSheetMicroscope.getNumberOfLightSheets() - 1);
        mDetectionArmIndex = new BoundedVariable<Integer>("Detection arm", pDetectionArmIndex, 0, pLightSheetMicroscope.getNumberOfDetectionArms() - 1);
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
        for (int i = 0; i < mNumberOfImages.get(); i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new SingleViewAcquisitionInstruction(mDetectionArmIndex.get(), mLightSheetIndex.get(), getLightSheetMicroscope()));
            index++;
            schedule.add(index, new WriteSingleLightSheetImageAsRawToDiscInstruction(mDetectionArmIndex.get(), mLightSheetIndex.get(), getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,true, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,false, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(StackInterface.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (int)(mIntervalInSeconds.get() * 1000)));
            index++;
        }
        return true;
    }

    @Override
    public AppendConsecutiveSingleViewImagingInstruction copy(){
        return new AppendConsecutiveSingleViewImagingInstruction(mDetectionArmIndex.get(), mLightSheetIndex.get(), mNumberOfImages.get(), mIntervalInSeconds.get(), getLightSheetMicroscope());
    }


    public BoundedVariable<Double> getIntervalInSeconds() {
        return mIntervalInSeconds;
    }

    public BoundedVariable<Integer> getNumberOfImages() {
        return mNumberOfImages;
    }

    public BoundedVariable<Integer> getLightSheetIndex() {
        return mLightSheetIndex;
    }

    public BoundedVariable<Integer> getDetectionArmIndex() {
        return mDetectionArmIndex;
    }
}
