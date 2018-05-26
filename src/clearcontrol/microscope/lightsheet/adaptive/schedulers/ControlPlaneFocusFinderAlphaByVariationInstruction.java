package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class ControlPlaneFocusFinderAlphaByVariationInstruction extends InstructionBase implements
        InstructionInterface,
                                                               LoggingFeature
{
  private final int mControlPlaneIndex;
  private final int mDetectionArmIndex;

  public ControlPlaneFocusFinderAlphaByVariationInstruction(int pDetectionArmIndex, int pControlPlaneIndex)
  {
    super("Adaptation: Focus finder alpha for C" + pDetectionArmIndex + "LxCPI" + pControlPlaneIndex);
    mDetectionArmIndex = pDetectionArmIndex;
    mControlPlaneIndex = pControlPlaneIndex;
  }

  @Override public boolean initialize()
  {
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
        FocusFinderAlphaByVariationInstruction
            lFocusFinder = new FocusFinderAlphaByVariationInstruction(lLightSheetIndex, mDetectionArmIndex, mControlPlaneIndex);
        lFocusFinder.setMicroscope(lLightSheetMicroscope);
        lFocusFinder.initialize();
        lFocusFinder.enqueue(pTimePoint); // this method returns success; we ignore it and continue focussing
      }

      return true;
    }
    return false;
  }
}
