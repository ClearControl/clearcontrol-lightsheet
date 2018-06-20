package clearcontrol.microscope.lightsheet.component.lightsheet.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;

public class ChangeLightSheetHeightInstruction extends LightSheetMicroscopeInstructionBase {


    private final double mLightSheetHeight;

    public ChangeLightSheetHeightInstruction(LightSheetMicroscope pLightSheetMicroscope, double pLightSheetHeight) {
        super("Adaptation: Change light sheet hieght to " + pLightSheetHeight, pLightSheetMicroscope);
        mLightSheetHeight = pLightSheetHeight;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getDevice(AcquisitionStateManager.class, 0).getCurrentState();
        for (int cpi = 0; cpi < lState.getNumberOfControlPlanes(); cpi++) {
            for (int l = 0; l < lState.getNumberOfLightSheets(); l++) {
                lState.getInterpolationTables().set(LightSheetDOF.IH, cpi, l, mLightSheetHeight);
            }
        }
        return true;
    }

    @Override
    public ChangeLightSheetWidthInstruction copy() {
        return new ChangeLightSheetWidthInstruction(getLightSheetMicroscope(), mLightSheetHeight);
    }
}