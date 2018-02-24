package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public abstract class AbstractAcquistionScheduler extends SchedulerBase implements
                                                                        SchedulerInterface,
                                                                        LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   */
  public AbstractAcquistionScheduler(String pDeviceName)
  {
    super(pDeviceName);
  }

  protected LightSheetMicroscope mLightSheetMicroscope;
  protected InterpolatedAcquisitionState mCurrentState;
  protected LightSheetTimelapse mTimelapse;

  @Override public boolean initialize()
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("" + this + " needs a lightsheet microscope!");
      return false;
    }

    mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    mCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();
    mTimelapse = mLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0);

    return true;
  }

  protected void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lLightsheetWidth,
                                   double lLightsheetHeight,
                                   double lLightsheetX,
                                   double lLightsheetY,
                                   double lIlluminationZStart,
                                   double lDetectionZZStart)
  {

    for (int l = 0; l
                    < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIW(l, lLightsheetWidth);
      lQueue.setIH(l, lLightsheetHeight);
      lQueue.setIX(l, lLightsheetX);
      lQueue.setIY(l, lLightsheetY);

      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d
                    < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);

    } lQueue.addCurrentStateToQueue();
  }

}
