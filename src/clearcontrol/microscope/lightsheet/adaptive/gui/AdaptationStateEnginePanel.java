package clearcontrol.microscope.lightsheet.adaptive.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.gui.ControlPlaneStatePanel;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.HasControlPlaneState;
import clearcontrol.microscope.lightsheet.configurationstate.HasName;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class AdaptationStateEnginePanel extends
                                                 CustomGridPane
{
  AdaptationStateEngine pAdaptationStateEngine;
  AdaptiveEngine mAdaptiveEngine;
  LightSheetMicroscope mLightSheetMicroscope;
  InterpolatedAcquisitionState mInterpolatedAcquisitionState;

  int mRow = 0;

  /**
   * Instantiates a panel given an adaptive engine
   *
   * @param pAdaptationStateEngine
   *          adaptor
   */
  public AdaptationStateEnginePanel(AdaptationStateEngine pAdaptationStateEngine)
  {
    super();
    mAdaptiveEngine = pAdaptationStateEngine.getAdaptiveEngine();
    mLightSheetMicroscope = pAdaptationStateEngine.getLightSheetMicroscope();
    mInterpolatedAcquisitionState = pAdaptationStateEngine.getInterpolatedAcquisitionState();

    for (Object pAdaptationModuleInterface : mAdaptiveEngine.getModuleList()) {
      if (pAdaptationModuleInterface instanceof HasControlPlaneState) {
        buildModuleStatePanel((HasControlPlaneState)pAdaptationModuleInterface);
      }
    }
  }

  private void buildModuleStatePanel(HasControlPlaneState pHasControlPlaneState) {

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();
    int lNumberOfControlPlanes = mInterpolatedAcquisitionState.getNumberOfControlPlanes();

    ControlPlaneStatePanel
        lControlPlaneStatePanel = new ControlPlaneStatePanel(pHasControlPlaneState, lNumberOfLightSheets, lNumberOfControlPlanes);

    String lName = pHasControlPlaneState.toString();
    if (pHasControlPlaneState instanceof HasName) {
      lName = ((HasName) pHasControlPlaneState).getName();
    }


    TitledPane lTitledPane = new TitledPane("Adaption " + lName + " state",
                                            lControlPlaneStatePanel);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    GridPane.setColumnSpan(lTitledPane, 3);
    add(lTitledPane, 0, mRow);
    mRow++;
  }
}