package clearcontrol.microscope.lightsheet.instructions;

import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;

/**
 * LightSheetMicroscopeInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public abstract class LightSheetMicroscopeInstructionBase extends
        DataWarehouseInstructionBase
{
  private final LightSheetMicroscope mLightSheetMicroscope;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName
   *          device name
   */
  public LightSheetMicroscopeInstructionBase(String pDeviceName,
                                             LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope.getDataWarehouse());
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }
}
