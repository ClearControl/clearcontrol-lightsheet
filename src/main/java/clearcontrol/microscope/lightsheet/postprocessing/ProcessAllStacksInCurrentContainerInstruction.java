package clearcontrol.microscope.lightsheet.postprocessing;

import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;

/**
 * ProcessAllStacksInCurrentContainerInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public abstract class ProcessAllStacksInCurrentContainerInstruction extends DataWarehouseInstructionBase {

    public ProcessAllStacksInCurrentContainerInstruction(String instructionName, DataWarehouse dataWarehouse) {
        super(instructionName, dataWarehouse);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        StackInterfaceContainer container =
                getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);

        StackInterfaceContainer resultContainer = new StackInterfaceContainer(pTimePoint) {
            @Override
            public boolean isDataComplete() {
                return true;
            }
        };

        for (String key : container.keySet())
        {
            StackInterface stack = container.get(key);
            StackInterface resultStack = processStack(stack);
            resultContainer.put(key, resultStack);
        }

        getDataWarehouse().put("wrangled_" + pTimePoint, resultContainer);

        return true;
    }

    protected abstract StackInterface processStack(StackInterface stack);
}
