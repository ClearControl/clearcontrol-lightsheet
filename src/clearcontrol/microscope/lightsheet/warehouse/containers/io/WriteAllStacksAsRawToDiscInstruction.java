package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * WriteAllStacksAsRawToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class WriteAllStacksAsRawToDiscInstruction extends WriteStackInterfaceContainerAsRawToDiscInstructionBase {
    /**
     * INstanciates a virtual device with a given name
     *
     * @param pContainerClass
     * @param pLightSheetMicroscope
     */
    public WriteAllStacksAsRawToDiscInstruction(Class pContainerClass, LightSheetMicroscope pLightSheetMicroscope) {
        super("IO: Write all stacks in " + pContainerClass.getSimpleName() + " as RAW to disc", pContainerClass, null, null, pLightSheetMicroscope);
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();
        StackInterfaceContainer container = lDataWarehouse.getOldestContainer(mContainerClass);

        mImageKeys = new String[container.keySet().size()];
        int count = 0;
        for (String key : container.keySet()) {
            mImageKeys[count] = key;
            count++;
        }

        return super.enqueue(pTimePoint);
    }

    @Override
    public WriteAllStacksAsRawToDiscInstruction copy() {
        return new WriteAllStacksAsRawToDiscInstruction(mContainerClass, getLightSheetMicroscope());
    }
}
