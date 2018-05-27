package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

public class ViewFusedStackInstruction extends LightSheetMicroscopeInstructionBase implements
        LoggingFeature
{

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ViewFusedStackInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Visualisation: View fused stack", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
        FusedImageDataContainer lContainer = lDataWarehouse.getOldestContainer(FusedImageDataContainer.class);
        if (lContainer == null || !lContainer.isDataComplete()) {
            return false;
        }

        Stack3DDisplay lDisplay = (Stack3DDisplay) getLightSheetMicroscope().getDevice(Stack3DDisplay.class, 0);
        if (lDisplay == null) {
            return false;
        }

        lDisplay.getInputStackVariable().set(lContainer.get("fused"));

        return true;
    }

    @Override
    public ViewFusedStackInstruction copy() {
        return new ViewFusedStackInstruction(getLightSheetMicroscope());
    }
}
