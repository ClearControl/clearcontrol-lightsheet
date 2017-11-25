package clearcontrol.microscope.lightsheet.adaptive;

import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationX;
import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationZ;
import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationZSlidingWindowDetectionArmSelection;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class AdaptationSetup
{
  /**
   * This static function allows to maintain setup code for Adaptation
   * modules in one place, which are used in several demos and main
   * XWing code
   *
   * This class may disappear again or refactored...
   */
  public static void setup(LightSheetMicroscope lLightSheetMicroscope, InterpolatedAcquisitionState lAcquisitionState) {
    int lNumberOfLightSheets = lLightSheetMicroscope.getNumberOfLightSheets();

    AdaptiveEngine<InterpolatedAcquisitionState>
        lAdaptiveEngine =
        lLightSheetMicroscope.addAdaptiveEngine(lAcquisitionState);
    lAdaptiveEngine.getRunUntilAllModulesReadyVariable().set(true);

    lAdaptiveEngine.add(new AdaptationZ(7,
                                        1.66,
                                        0.95,
                                        2e-5,
                                        0.010,
                                        0.5,
                                        lNumberOfLightSheets));
    lAdaptiveEngine.add(new AdaptationZSlidingWindowDetectionArmSelection(7,
                                                                          3,
                                                                          true,
                                                                          1.66,
                                                                          0.95,
                                                                          2e-5,
                                                                          0.010,
                                                                          0.5));
    lAdaptiveEngine.add(new AdaptationX(11,
                                        50,
                                        200,
                                        0.95,
                                        2e-5,
                                        0.010,
                                        0.5));
  }
}
