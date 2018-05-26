package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * DropAllContainersOfTypeInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DropAllContainersOfTypeInstruction extends
        InstructionBase implements
        SchedulerInterface,
        LoggingFeature
{
    Class mContainerClassToDrop;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public DropAllContainersOfTypeInstruction(Class pContainerClassToDrop)
    {
        super("Memory: Recycle all containers of type " + pContainerClassToDrop.getSimpleName());
        mContainerClassToDrop = pContainerClassToDrop;
    }

    @Override public boolean initialize()
    {
        return false;
    }

    @Override public boolean enqueue(long pTimePoint)
    {
        if (mMicroscope instanceof LightSheetMicroscope) {
            DataWarehouse lWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();
            while (true) {
                StackInterfaceContainer lContainer = lWarehouse.getOldestContainer(mContainerClassToDrop);
                if (lContainer == null) {
                    break;
                }
                lWarehouse.disposeContainer(lContainer);
            }
        } else {
            warning("I need a LightSheetMicroscope!");
        }
        return false;
    }
}

