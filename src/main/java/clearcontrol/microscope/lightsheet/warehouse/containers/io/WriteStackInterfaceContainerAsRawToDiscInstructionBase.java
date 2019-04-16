package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;

/**
 * This generalised IO instruction writes all images in a
 * StackInterfaceContainer of a given Class to disc.
 *
 * @author haesleinhuepf April 2018
 */
public abstract class WriteStackInterfaceContainerAsRawToDiscInstructionBase extends
                                                                             LightSheetMicroscopeInstructionBase
                                                                             implements
                                                                             LoggingFeature, AutoRecyclerInstructionInterface
{
  protected Class mContainerClass;
  protected String[] mImageKeys = null;
  protected String mChannelName = null;

  protected FileStackSinkInterface fileStackSinkInterface = null;

  protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after saving", true);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName
   *          device name
   */
  public WriteStackInterfaceContainerAsRawToDiscInstructionBase(String pDeviceName,
                                                                Class pContainerClass,
                                                                String[] pImageKeys,
                                                                String pChannelName,
                                                                LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
    mContainerClass = pContainerClass;
    mImageKeys = pImageKeys;
    if (pChannelName != null && pChannelName.length() > 0)
    {
      mChannelName = pChannelName;
    }
  }

  @Override
  public boolean initialize()
  {
    LightSheetTimelapse lTimelapse =
            (LightSheetTimelapse) getLightSheetMicroscope().getDevice(TimelapseInterface.class,
                    0);

    fileStackSinkInterface =
            lTimelapse.getCurrentFileStackSinkVariable()
                    .get();

    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse =
                                 ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();

    StackInterfaceContainer lContainer =
                                       lDataWarehouse.getOldestContainer(mContainerClass);
    if (lContainer == null)
    {
      warning("No " + mContainerClass.getCanonicalName()
              + " found for saving");
      return false;
    }

    for (String key : mImageKeys)
    {
      StackInterface lStack = lContainer.get(key);
      if (mChannelName != null)
      {
        saveStack(fileStackSinkInterface, mChannelName, lStack);
      }
      else
      {
        saveStack(fileStackSinkInterface, key, lStack);
      }
    }
    return true;
  }

  private void saveStack(FileStackSinkInterface lSinkInterface,
                         String pChannelName,
                         StackInterface lStack)
  {
    ElapsedTime.measureForceOutput(this + " stack saving",
                                   () -> lSinkInterface.appendStack(pChannelName,
                                                                    lStack));

  }

  public Variable<Boolean> getRecycleSavedContainers() {
    return recycleSavedContainers;
  }

  @Override
  public void autoRecycle() {
    if (recycleSavedContainers.get()) {
      AutoRecyclerInstructionInterface.super.autoRecycle();
    }
  }


  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    if (!recycleSavedContainers.get()) {
      return new Class[0];
    }
    return new Class[]{StackInterfaceContainer.class};
  }
}
