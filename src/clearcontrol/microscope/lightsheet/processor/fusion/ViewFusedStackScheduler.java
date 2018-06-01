package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.StackDisplayInterface;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

public class ViewFusedStackScheduler extends SchedulerBase implements
        LoggingFeature
{

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ViewFusedStackScheduler() {
        super("Visualisation: View fused stack");
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            return false;
        }
        DataWarehouse lDataWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();
        FusedImageDataContainer lContainer = lDataWarehouse.getOldestContainer(FusedImageDataContainer.class);
        if (lContainer == null || !lContainer.isDataComplete()) {
            return false;
        }

        Stack3DDisplay lDisplay = (Stack3DDisplay) mMicroscope.getDevice(Stack3DDisplay.class, 0);
        if (lDisplay == null) {
            return false;
        }

        lDisplay.getInputStackVariable().set(lContainer.get("fused"));

        return true;
    }
}