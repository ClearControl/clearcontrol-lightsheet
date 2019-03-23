package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * Deprecated because replacable with ViewStack3DInstruction
 */
@Deprecated
public class ViewSingleLightSheetStackInstruction extends
                                                  LightSheetMicroscopeInstructionBase
                                                  implements
                                                  LoggingFeature
{

  private final int mDetectionArmIndex;
  private final int mLightSheetIndex;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public ViewSingleLightSheetStackInstruction(int pDetectionArmIndex,
                                              int pLightSheetIndex,
                                              LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Visualisation: View C" + pDetectionArmIndex
          + "L"
          + pLightSheetIndex
          + " stack",
          pLightSheetMicroscope);
    mDetectionArmIndex = pDetectionArmIndex;
    mLightSheetIndex = pLightSheetIndex;
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse =
                                 getLightSheetMicroscope().getDataWarehouse();
    StackInterfaceContainer lContainer =
                                       lDataWarehouse.getOldestContainer(StackInterfaceContainer.class);
    if (lContainer == null || !lContainer.isDataComplete())
    {
      return false;
    }

    Stack3DDisplay lDisplay =
                            (Stack3DDisplay) getLightSheetMicroscope().getDevice(Stack3DDisplay.class,
                                                                                 0);
    if (lDisplay == null)
    {
      return false;
    }

    lDisplay.getInputStackVariable()
            .set(lContainer.get("C" + mDetectionArmIndex
                                + "L"
                                + mLightSheetIndex));

    return true;
  }

  @Override
  public ViewSingleLightSheetStackInstruction copy()
  {
    return new ViewSingleLightSheetStackInstruction(mDetectionArmIndex,
                                                    mLightSheetIndex,
                                                    getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "DEPRECATED: View a stack acquired from a single camera/lightsheet pair.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[0];
  }
}
