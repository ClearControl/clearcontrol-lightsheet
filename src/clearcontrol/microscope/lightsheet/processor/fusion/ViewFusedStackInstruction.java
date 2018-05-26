package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

public class ViewFusedStackInstruction extends InstructionBase implements
        LoggingFeature
{

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ViewFusedStackInstruction() {
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
