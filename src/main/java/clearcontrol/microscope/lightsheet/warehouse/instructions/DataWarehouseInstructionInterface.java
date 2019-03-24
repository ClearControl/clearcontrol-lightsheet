package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

/**
 * DataWarehouseInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public interface DataWarehouseInstructionInterface extends
        InstructionInterface
{
  DataWarehouse getDataWarehouse();
  public abstract Class[] getProducedContainerClasses();
  public abstract Class[] getConsumedContainerClasses();
}
