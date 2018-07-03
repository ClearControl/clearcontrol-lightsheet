package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscInstructionBase;

public class WriteSingleLightSheetImageAsTifToDiscInstruction extends
        WriteStackInterfaceContainerAsTifToDiscInstructionBase
{
    private int mDetectionArmIndex;
    private int mLightSheetIndex;

    public WriteSingleLightSheetImageAsTifToDiscInstruction(int pDetectionArmIndex, int pLightSheetIndex, LightSheetMicroscope pLightSheetMicroscope)
    {
        super("IO: Write C" + pDetectionArmIndex + "L" + pLightSheetIndex + " tif to disc", StackInterfaceContainer.class, new String[] {"C" + pDetectionArmIndex + "L" + pLightSheetIndex}, null, pLightSheetMicroscope);
        mDetectionArmIndex = pDetectionArmIndex;
        mLightSheetIndex = pLightSheetIndex;
    }

    @Override
    public WriteSingleLightSheetImageAsTifToDiscInstruction copy() {
        return new WriteSingleLightSheetImageAsTifToDiscInstruction(mDetectionArmIndex, mLightSheetIndex, getLightSheetMicroscope());
    }
}