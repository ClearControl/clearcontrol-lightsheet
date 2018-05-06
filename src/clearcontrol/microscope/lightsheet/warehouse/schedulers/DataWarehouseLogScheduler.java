package clearcontrol.microscope.lightsheet.warehouse.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

/**
 * DataWarehouseLogScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DataWarehouseLogScheduler extends SchedulerBase {
    private final DataWarehouse mDataWarehouse;
    private final LightSheetTimelapse mTimelapse;

    public DataWarehouseLogScheduler(LightSheetMicroscope pLightSheetMicroscope) {
        super("State: Log content of the DataWarehouse");
        mDataWarehouse = pLightSheetMicroscope.getDataWarehouse();
        mTimelapse = pLightSheetMicroscope.getTimelapse();
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        int i = 0;
        mTimelapse.log("DataWareHouse contains:");
        for (DataContainerInterface container : mDataWarehouse.getContainers(DataContainerInterface.class)) {
            mTimelapse.log("[" + i + "] \t" + container);
            i++;
        }
        return false;
    }
}
