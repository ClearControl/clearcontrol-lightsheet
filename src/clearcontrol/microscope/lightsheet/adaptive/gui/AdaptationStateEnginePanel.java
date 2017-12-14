package clearcontrol.microscope.lightsheet.adaptive.gui;

import clearcontrol.core.device.name.ReadOnlyNameableInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.gui.ControlPlaneStatePanel;
import clearcontrol.microscope.lightsheet.adaptive.controlplanestate.HasControlPlaneState;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.configurationstate.CanBeActive;
import clearcontrol.microscope.lightsheet.configurationstate.ConfigurationState;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.gui.VariableLabel;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.Timer;

/**
 * Todo: this class may be too XWing specific and should move to its
 * repository eventually.
 *
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


  /**
   * Instantiates a panel given an adaptive engine
   *
   * @param pAdaptationStateEngine
   *          adaptor
   */
  public AdaptationStateEnginePanel(AdaptationStateEngine pAdaptationStateEngine)
  {
    super();
    int lRow = 0;

    mAdaptiveEngine = pAdaptationStateEngine.getAdaptiveEngine();
    mLightSheetMicroscope = pAdaptationStateEngine.getLightSheetMicroscope();
    mInterpolatedAcquisitionState = pAdaptationStateEngine.getInterpolatedAcquisitionState();

    CustomGridPane lCustomGridPane = new CustomGridPane();

    CalibrationEngine lCalibrationEngine = mLightSheetMicroscope.getDevice(
        CalibrationEngine.class, 0);

    ConfigurationStatePanel
        lConfigurationStatePanel = new ConfigurationStatePanel(lCalibrationEngine.getModuleList(), mLightSheetMicroscope.getNumberOfLightSheets());

    TitledPane lTitledPane = new TitledPane("Calibration state",
                                            lConfigurationStatePanel);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    //GridPane.setColumnSpan(lTitledPane, 3);
    lCustomGridPane.add(lTitledPane, 1, lRow);
    lRow++;


    for (Object pAdaptationModuleInterface : mAdaptiveEngine.getModuleList()) {
      if (pAdaptationModuleInterface instanceof HasControlPlaneState) {
        TitledPane lModuleStatePanel = buildModuleStatePanel((HasControlPlaneState)pAdaptationModuleInterface);
        lCustomGridPane.add(lModuleStatePanel, 1, lRow);
        lRow++;
      }
    }
    GridPane.setRowSpan(lCustomGridPane, 2);
    add(lCustomGridPane, 1, 0);

    add(buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(0)), 2, 1);
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 0)
    {
      add(buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(1)),
          2,
          0);
    }
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 1)
    {
      add(buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(2)),
          0,
          0);
    }
    if (mLightSheetMicroscope.getNumberOfLightSheets() > 2)
    {
      add(buildLightSheetPanel(mLightSheetMicroscope.getLightSheet(3)),
          0,
          1);
    }
  }

  private TitledPane buildLightSheetPanel(LightSheetInterface pLightSheetInterface) {

    CustomGridPane lCustomGridPane = new CustomGridPane();

    int lRow = 0;
    lCustomGridPane.add(new Label("X function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getXFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Y function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getYFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Z function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getZFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Alpha function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getAlphaFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Beta function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getBetaFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Width function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getWidthFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Height function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getHeightFunction()), 0, lRow++);
    lCustomGridPane.add(new Label("Power function"), 0, lRow++);
    lCustomGridPane.add(buildVariableLabel("", pLightSheetInterface.getPowerFunction()), 0, lRow++);



    TitledPane lTitledPane = new TitledPane("Light sheet " + pLightSheetInterface.getName() + " state",
                                            lCustomGridPane);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);
    //GridPane.setColumnSpan(lTitledPane, 3);
    return lTitledPane;
  }

  private VariableLabel buildVariableLabel(String name, Variable variable) {
    VariableLabel lVariableLabel = new VariableLabel(name, variable.get().toString());

    variable.addSetListener(new VariableSetListener()
    {
      @Override public void setEvent(Object pCurrentValue,
                                     Object pNewValue)
      {
        Platform.runLater(new Runnable()
        {
          @Override public void run()
          {
            lVariableLabel.setText(pNewValue.toString());
            lVariableLabel.setStyle("-fx-border-color:red;");
            Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(2500),
                (ae) -> {
                  lVariableLabel.setStyle("");
                }));
            timeline.play();
          }
        });

      }
    });
    return lVariableLabel;
  }

  private TitledPane buildModuleStatePanel(HasControlPlaneState pHasControlPlaneState) {

    int lNumberOfLightSheets = mLightSheetMicroscope.getNumberOfLightSheets();
    int lNumberOfControlPlanes = mInterpolatedAcquisitionState.getNumberOfControlPlanes();

    ControlPlaneStatePanel
        lControlPlaneStatePanel = new ControlPlaneStatePanel(pHasControlPlaneState, lNumberOfLightSheets, lNumberOfControlPlanes);

    String lName = pHasControlPlaneState.toString();
    if (pHasControlPlaneState instanceof ReadOnlyNameableInterface) {
      lName = ((ReadOnlyNameableInterface) pHasControlPlaneState).getName();
    }


    TitledPane lTitledPane = new TitledPane("Adaptation " + lName + " state",
                                            lControlPlaneStatePanel);
    lTitledPane.setAnimated(false);
    lTitledPane.setExpanded(true);

    if (pHasControlPlaneState instanceof CanBeActive && !((CanBeActive)pHasControlPlaneState).isActive()) {
      lTitledPane.setExpanded(false);
    }
    //GridPane.setColumnSpan(lTitledPane, 3);
    return lTitledPane;
  }
}