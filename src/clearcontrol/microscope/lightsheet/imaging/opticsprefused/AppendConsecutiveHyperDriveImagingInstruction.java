package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionInterface;
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
public class AppendConsecutiveHyperDriveImagingInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature, PropertyIOableInstructionInterface {

    private final BoundedVariable<Integer> mNumberOfImages = new BoundedVariable<Integer>("Number of images", 100);
    private final BoundedVariable<Double> mIntervalInSeconds = new BoundedVariable<Double>("Frame delay in s", 15.0);


    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveHyperDriveImagingInstruction(int pNumberOfImages, double pIntervalInSeconds, LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Append a HyperDrive scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the instructions", pLightSheetMicroscope);
        mNumberOfImages.set(pNumberOfImages);
        mIntervalInSeconds.set(pIntervalInSeconds);
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

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        for (int i = 0; i < mNumberOfImages.get(); i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new OpticsPrefusedAcquisitionInstruction(getLightSheetMicroscope()));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (int)(mIntervalInSeconds.get() * 1000)));
            index++;
        }
        for (int i = 0; i < mNumberOfImages.get(); i ++) {
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
        return true;
    }

    @Override
    public AppendConsecutiveHyperDriveImagingInstruction copy() {
        return new AppendConsecutiveHyperDriveImagingInstruction(mNumberOfImages.get(), mIntervalInSeconds.get(), getLightSheetMicroscope());
    }


    public BoundedVariable<Double> getIntervalInSeconds() {
        return mIntervalInSeconds;
    }

    public BoundedVariable<Integer> getNumberOfImages() {
        return mNumberOfImages;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                getIntervalInSeconds(),
                getNumberOfImages()
        };
    }
}
