package clearcontrol.microscope.lightsheet.imaging.interleaved;

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
 * The AppendConsecutiveInterleavedImagingInstruction appends a list of imaging, fusion and io instructions at the current position
 * in the timelapse
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class AppendConsecutiveInterleavedImagingInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {
    private final int mNumberOfImages;
    private final double mIntervalInSeconds;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public AppendConsecutiveInterleavedImagingInstruction(int pNumberOfImages, double pIntervalInSeconds, LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Append an interleaved scan with " + pNumberOfImages + " images every " + pIntervalInSeconds + " s to the instructions", pLightSheetMicroscope);
        mNumberOfImages = pNumberOfImages;
        mIntervalInSeconds = pIntervalInSeconds;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        String timeMeasurementKey = "interleaved_" + System.currentTimeMillis();

        LightSheetTimelapse lTimelapse = getLightSheetMicroscope().getTimelapse();
        ArrayList<InstructionInterface> schedule = lTimelapse.getListOfActivatedSchedulers();

        int index = (int)lTimelapse.getLastExecutedSchedulerIndexVariable().get() + 1;
        for (int i = 0; i < mNumberOfImages; i ++) {
            schedule.add(index, new MeasureTimeInstruction(timeMeasurementKey));
            index++;
            schedule.add(index, new InterleavedAcquisitionInstruction(getLightSheetMicroscope()));
            index++;
            schedule.add(index, new InterleavedFusionInstruction(getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(InterleavedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;
            schedule.add(index, new WriteFusedImageAsRawToDiscInstruction("interleaved", getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false, getLightSheetMicroscope()));
            index++;
            schedule.add(index, new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class, getLightSheetMicroscope().getDataWarehouse()));
            index++;
            schedule.add(index, new PauseUntilTimeAfterMeasuredTimeInstruction(timeMeasurementKey, (long)(mIntervalInSeconds * 1000)));
            index++;
        }
        return true;
    }

    @Override
    public AppendConsecutiveInterleavedImagingInstruction copy() {
        return new AppendConsecutiveInterleavedImagingInstruction(mNumberOfImages, mIntervalInSeconds, getLightSheetMicroscope());
    }
}
