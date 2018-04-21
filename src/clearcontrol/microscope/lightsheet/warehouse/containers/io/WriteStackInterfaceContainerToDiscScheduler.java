package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.imaging.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class WriteStackInterfaceContainerToDiscScheduler extends
                                                         SchedulerBase implements
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
  public WriteStackInterfaceContainerToDiscScheduler(String pDeviceName, Class pContainerClass, String[] pImageKeys, String pChannelName)
  {
    super(pDeviceName);
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

    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("I need a LightSheetMicroscope!");
      return false;
    }


    LightSheetTimelapse lTimelapse = (LightSheetTimelapse) mMicroscope.getDevice(TimelapseInterface.class, 0);
    FileStackSinkInterface
        lFileStackSinkInterface =
        lTimelapse.getCurrentFileStackSinkVariable().get();

    DataWarehouse lDataWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();

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