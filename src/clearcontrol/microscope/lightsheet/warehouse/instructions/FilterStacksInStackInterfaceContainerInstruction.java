package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

import java.util.ArrayList;

/**
 * The FilterStacksInStackInterfaceContainerInstruction allows removing stacks from a StackInterfaceContainer which
 * don't match a certain pattern.
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class FilterStacksInStackInterfaceContainerInstruction extends DataWarehouseInstructionBase implements PropertyIOableInstructionInterface {

    private Variable<String> filter = new Variable<String>("Filter (must contain one of the comma-separated)");

    public FilterStacksInStackInterfaceContainerInstruction(DataWarehouse pDataWarehouse) {
        super("Memory: Filter stacks in container", pDataWarehouse);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        StackInterfaceContainer container = getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);
        ArrayList<String> listKeysToRemove = new ArrayList<String>();

        String[] filters = filter.get().split(",");
        for (String key : container.keySet()) {
            boolean containsAny = false;
            for (String mustContain : filters) {
                if (key.contains(mustContain)) {
                    containsAny = true;
                    break;
                }
            }
            if (!containsAny) {
                listKeysToRemove.add(key);
            }
        }

        for (String keyToRemove : listKeysToRemove) {
            container.remove(keyToRemove);
        }

        return false;
    }

    @Override
    public FilterStacksInStackInterfaceContainerInstruction copy() {
        FilterStacksInStackInterfaceContainerInstruction copied = new FilterStacksInStackInterfaceContainerInstruction(getDataWarehouse());
        copied.filter.set(filter.get());
        return copied;
    }

    public Variable<String> getFilter() {
        return filter;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {getFilter()};
    }
}
