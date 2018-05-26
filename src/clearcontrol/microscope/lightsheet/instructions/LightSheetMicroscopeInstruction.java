package clearcontrol.microscope.lightsheet.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;

/**
 * LightSheetMicroscopeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public abstract class LightSheetMicroscopeInstruction extends InstructionBase {
    private final LightSheetMicroscope mLightSheetMicroscope;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
    public LightSheetMicroscopeInstruction(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope) {
        super(pDeviceName);
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    public LightSheetMicroscope getLightSheetMicroscope() {
        return mLightSheetMicroscope;
    }
}
