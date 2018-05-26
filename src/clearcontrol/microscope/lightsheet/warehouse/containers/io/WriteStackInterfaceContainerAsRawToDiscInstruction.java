package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;

/**
 * This generalised IO Scheduler writes all images in a
 * StackInterfaceContainer of a given Class to disc.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteStackInterfaceContainerAsRawToDiscInstruction extends
        LightSheetMicroscopeInstruction implements
                                                                       LoggingFeature
{
  Class mContainerClass;
  String[] mImageKeys = null;
  String mChannelName = null;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
  public WriteStackInterfaceContainerAsRawToDiscInstruction(String pDeviceName, Class pContainerClass, String[] pImageKeys, String pChannelName, LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
    mContainerClass = pContainerClass;
    mImageKeys = pImageKeys;
    if (pChannelName != null && pChannelName.length() > 0)
    {
      mChannelName = pChannelName;
    }
  }

  @Override public boolean initialize()
  {
    return false;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    LightSheetTimelapse lTimelapse = (LightSheetTimelapse) getLightSheetMicroscope().getDevice(TimelapseInterface.class, 0);
    FileStackSinkInterface
        lFileStackSinkInterface =
        lTimelapse.getCurrentFileStackSinkVariable().get();

    DataWarehouse lDataWarehouse = ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();

    StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(mContainerClass);
    if (lContainer == null) {
      warning("No " + mContainerClass.getCanonicalName() + " found for saving");
      return false;
    }

    for (String key : mImageKeys)
    {
      StackInterface lStack = lContainer.get(key);
      if (mChannelName != null)
      {
        saveStack(lFileStackSinkInterface, mChannelName, lStack);
      } else {
        saveStack(lFileStackSinkInterface, key, lStack);
      }
    }
    return true;
  }

  private void saveStack(FileStackSinkInterface lSinkInterface, String pChannelName, StackInterface lStack){
    ElapsedTime.measureForceOutput(this + " stack saving",
                                   () -> lSinkInterface.appendStack(
                                       pChannelName,
                                       lStack));

  }
}
