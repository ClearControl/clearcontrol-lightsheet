package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.tables.InterpolationTables;

/**
 * The XWingRapidAutoFocusInstruction uses adaptation of the central plane to
 * configure all control planes. This might be good initial starting point for
 * adaptation.
 *
 * Todo: This is XWing specific code and should move to its repository at some
 * point.
 *
 * Derecated: use AutoFocusSinglePlaneInstruction
 *
 * Author: @haesleinhuepf 05 2018
 */
@Deprecated
public class XWingRapidAutoFocusInstruction extends
                                            LightSheetMicroscopeInstructionBase
{

  public XWingRapidAutoFocusInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: XWingScope rapid autofocus Z and alpha",
          pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState lAcquisitionState =
                                                   (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                                                                                                           .getCurrentState();

    // XWing specific
    int lOptimizeCamera0towardsCPI = 5;
    int lOptimizeCamera1towardsCPI = 2;

    ControlPlaneFocusFinderZInstruction lCamera0ZFocusScheduler =
                                                                new ControlPlaneFocusFinderZInstruction(0,
                                                                                                        lOptimizeCamera0towardsCPI,
                                                                                                        getLightSheetMicroscope());
    ControlPlaneFocusFinderZInstruction lCamera1ZFocusScheduler =
                                                                new ControlPlaneFocusFinderZInstruction(1,
                                                                                                        lOptimizeCamera1towardsCPI,
                                                                                                        getLightSheetMicroscope());
    ControlPlaneFocusFinderAlphaByVariationInstruction lCamera0AlphaScheduler =
                                                                              new ControlPlaneFocusFinderAlphaByVariationInstruction(0,
                                                                                                                                     lOptimizeCamera0towardsCPI,
                                                                                                                                     getLightSheetMicroscope());
    ControlPlaneFocusFinderAlphaByVariationInstruction lCamera1AlphaScheduler =
                                                                              new ControlPlaneFocusFinderAlphaByVariationInstruction(1,
                                                                                                                                     lOptimizeCamera1towardsCPI,
                                                                                                                                     getLightSheetMicroscope());

    InstructionInterface[] lSchedulers = new InstructionInterface[]
    { lCamera0AlphaScheduler,
      lCamera1AlphaScheduler,
      lCamera0ZFocusScheduler,
      lCamera1ZFocusScheduler };

    for (InstructionInterface lScheduler : lSchedulers)
    {
      lScheduler.initialize();
      lScheduler.enqueue(pTimePoint);
    }

    // Copy configuration to other control planes
    for (int lLightSheetIndex =
                              0; lLightSheetIndex < getLightSheetMicroscope().getNumberOfLightSheets(); lLightSheetIndex++)
    {
      for (int cpi =
                   0; cpi < lAcquisitionState.getNumberOfControlPlanes(); cpi++)
      {
        InterpolationTables it =
                               lAcquisitionState.getInterpolationTables();

        int lCopySourceControlPlane =
                                    cpi < 4 ? lOptimizeCamera1towardsCPI
                                            : lOptimizeCamera0towardsCPI;

        for (LightSheetDOF lDOF : new LightSheetDOF[]
        { LightSheetDOF.IZ, LightSheetDOF.IA })
        {
          it.set(lDOF,
                 cpi,
                 lLightSheetIndex,
                 it.get(lDOF,
                        lCopySourceControlPlane,
                        lLightSheetIndex));
        }
      }
    }

    return true;
  }

  @Override
  public XWingRapidAutoFocusInstruction copy()
  {
    return new XWingRapidAutoFocusInstruction(getLightSheetMicroscope());
  }
}
