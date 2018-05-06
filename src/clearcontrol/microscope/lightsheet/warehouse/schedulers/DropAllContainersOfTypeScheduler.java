package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * DropAllContainersOfTypeScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DropAllContainersOfTypeScheduler extends
        SchedulerBase implements
        SchedulerInterface,
        LoggingFeature
{
    Class mContainerClassToDrop;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public DropAllContainersOfTypeScheduler(Class pContainerClassToDrop)
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

