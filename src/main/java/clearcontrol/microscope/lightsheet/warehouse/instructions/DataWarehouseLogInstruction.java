package clearcontrol.microscope.lightsheet.warehouse.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

/**
 * DataWarehouseLogInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class DataWarehouseLogInstruction extends
                                         DataWarehouseInstructionBase implements LoggingFeature
{
  private DataWarehouse mDataWarehouse;
  private LightSheetTimelapse mTimelapse;
  private final LightSheetMicroscope mLightSheetMicroscope;

  public DataWarehouseLogInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Log content of the DataWarehouse",
          pLightSheetMicroscope.getDataWarehouse());
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  @Override
  public boolean initialize()
  {

    mDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
    mTimelapse = mLightSheetMicroscope.getTimelapse();
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    int i = 0;
    mTimelapse.log("DataWareHouse contains:");
    for (DataContainerInterface container : mDataWarehouse.getContainers(DataContainerInterface.class))
    {
      info("[" + i + "] \t" + container);
      mTimelapse.log("[" + i + "] \t" + container);
      i++;
    }
    return false;
  }

  @Override
  public DataWarehouseLogInstruction copy()
  {
    return new DataWarehouseLogInstruction(mLightSheetMicroscope);
  }
}
