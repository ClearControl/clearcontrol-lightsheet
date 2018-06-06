package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * WriteSpecificStackToSpecificRawFolderInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class WriteSpecificStackToSpecificRawFolderInstruction extends WriteStackInterfaceContainerAsRawToDiscInstructionBase {

    Variable<String> mSourceStackKeyVariable = new Variable<String>("Stack key to save", "C0L0");
    Variable<String> mTargetRawFolderNameVariable = new Variable<String>("Foldername", "C0L0");

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pTargetRawFolderName
     * @param pLightSheetMicroscope
     */
    public WriteSpecificStackToSpecificRawFolderInstruction(String pSourceStackKey, String pTargetRawFolderName, LightSheetMicroscope pLightSheetMicroscope) {
        super("IO: Write '" + pSourceStackKey + "' as '" + pTargetRawFolderName + "' RAW folder ot disc", StackInterfaceContainer.class, new String[]{pSourceStackKey}, pTargetRawFolderName, pLightSheetMicroscope);
        mSourceStackKeyVariable.set(pSourceStackKey);
        mTargetRawFolderNameVariable.set(pTargetRawFolderName);

        mSourceStackKeyVariable.addSetListener(new VariableSetListener<String>() {
            @Override
            public void setEvent(String pCurrentValue, String pNewValue) {
                mImageKeys = new String[]{pNewValue};
            }
        });
        mTargetRawFolderNameVariable.addSetListener(new VariableSetListener<String>() {
            @Override
            public void setEvent(String pCurrentValue, String pNewValue) {
                mChannelName = pNewValue;
            }
        });
    }

    @Override
    public WriteSpecificStackToSpecificRawFolderInstruction copy() {
        return new WriteSpecificStackToSpecificRawFolderInstruction(mSourceStackKeyVariable.get(), mTargetRawFolderNameVariable.get(), getLightSheetMicroscope());
    }

    public Variable<String> getSourceStackKeyVariable() {
        return mSourceStackKeyVariable;
    }

    public Variable<String> getTargetRawFolderNameVariable() {
        return mTargetRawFolderNameVariable;
    }

    @Override
    public String toString() {
        return "IO: Write '" + mSourceStackKeyVariable.get() + "' as '" + mTargetRawFolderNameVariable.get() + "' RAW folder ot disc";
    }
}
