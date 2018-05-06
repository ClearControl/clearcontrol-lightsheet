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
    private DataWarehouse mDataWarehouse;
    private LightSheetTimelapse mTimelapse;
    private final LightSheetMicroscope mLightSheetMicroscope;

    public DataWarehouseLogScheduler(LightSheetMicroscope pLightSheetMicroscope) {
        super("State: Log content of the DataWarehouse");
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override
    public boolean initialize() {

        mDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
        mTimelapse = mLightSheetMicroscope.getTimelapse();
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
