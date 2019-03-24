package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

public interface AutoRecyclerInstructionInterface extends DataWarehouseInstructionInterface {
    default void autoRecycle() {
        Class[] containers = getConsumedContainerClasses();
        DataWarehouse warehouse = getDataWarehouse();

        for (Class clazz : containers) {
            DataContainerInterface container = warehouse.getOldestContainer(clazz);
            warehouse.disposeContainer(container);
        }
    }
}
