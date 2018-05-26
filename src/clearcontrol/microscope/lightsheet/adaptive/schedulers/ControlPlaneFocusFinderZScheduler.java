package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ControlPlaneFocusFinderZScheduler extends SchedulerBase implements
                                                               SchedulerInterface,
                                                               LoggingFeature
{
  private final int mControlPlaneIndex;
  private final int mDetectionArmIndex;

  private Variable<Boolean>
      mResetAllTheTime = new Variable<Boolean>("resetAllTheTime", false);

  boolean mNeedsReset = true;

  public ControlPlaneFocusFinderZScheduler(int pDetectionArmIndex, int pControlPlaneIndex)
  {
    super("Adaptation: Focus finder Z for C" + pDetectionArmIndex + "LxCPI" + pControlPlaneIndex);
    mDetectionArmIndex = pDetectionArmIndex;
    mControlPlaneIndex = pControlPlaneIndex;
  }

  @Override public boolean initialize()
  {
    mNeedsReset = true;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (mMicroscope instanceof LightSheetMicroscope)
    {
      LightSheetMicroscope lLightSheetMicroscope =
          (LightSheetMicroscope) mMicroscope;

      int
          lNumberOfControlPlanes =
          ((InterpolatedAcquisitionState) (lLightSheetMicroscope.getAcquisitionStateManager()
                                                                .getCurrentState()))
              .getNumberOfControlPlanes();

      for (int lLightSheetIndex = 0; lLightSheetIndex < lLightSheetMicroscope.getNumberOfLightSheets(); lLightSheetIndex++)
      {
        FocusFinderZScheduler
            lFocusFinder = new FocusFinderZScheduler(lLightSheetIndex, mDetectionArmIndex, mControlPlaneIndex);
        lFocusFinder.setMicroscope(lLightSheetMicroscope);
        lFocusFinder.initialize();
        lFocusFinder.mNeedsReset = mNeedsReset;
        lFocusFinder.enqueue(pTimePoint); // this method returns success; we ignore it and continue focussing
      }

      mNeedsReset = false;
      return true;
    }
    return false;
  }
}
