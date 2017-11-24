package clearcontrol.microscope.lightsheet.calibrator.gui;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.adaptive.modules.AdaptationModuleInterface;
import clearcontrol.microscope.lightsheet.calibrator.modules.CalibrationModuleInterface;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.onoffarray.OnOffArrayPane;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Calibration Engine Toolbar
 *
 * @author royer
 */
public class CalibrationEngineToolbar extends CustomGridPane
    implements LoggingFeature
{

  /**
   * Instanciates a calibration engine toolbar
   * 
   * @param pCalibrationEngine
   *          calubrator
   */
  public CalibrationEngineToolbar(CalibrationEngine pCalibrationEngine)
  {
    super();
    // this.setStyle("-fx-background-color: yellow;");
    // mGridPane.setStyle("-fx-border-color: blue;");

    for (int i = 0; i < 3; i++)
    {
      ColumnConstraints lColumnConstraints = new ColumnConstraints();
      lColumnConstraints.setPercentWidth(33);
      getColumnConstraints().add(lColumnConstraints);
    }

    int lRow = 0;

    {
      Button lStartCalibration = new Button("Calibrate");
      lStartCalibration.setAlignment(Pos.CENTER);
      lStartCalibration.setMaxWidth(Double.MAX_VALUE);
      lStartCalibration.setOnAction((e) -> {
        pCalibrationEngine.startTask();
      });
      GridPane.setColumnSpan(lStartCalibration, 2);
      GridPane.setHgrow(lStartCalibration, Priority.ALWAYS);
      add(lStartCalibration, 0, lRow);

      lRow++;
    }

    {
      Label lLabel = new Label("Z -> A -> XY -> P -> Z");
      GridPane.setColumnSpan(lLabel, 3);
      add(lLabel, 0, lRow);
      lRow++;
    }

    {
      Button lStopCalibration = new Button("Stop");
      lStopCalibration.setAlignment(Pos.CENTER);
      lStopCalibration.setMaxWidth(Double.MAX_VALUE);
      lStopCalibration.setOnAction((e) -> {
        pCalibrationEngine.stopTask();
      });
      GridPane.setColumnSpan(lStopCalibration, 2);
      GridPane.setHgrow(lStopCalibration, Priority.ALWAYS);
      add(lStopCalibration, 0, lRow);

      lRow++;
    }

    {
      ProgressIndicator lCalibrationProgressIndicator =
                                                      new ProgressIndicator(0.0);
      lCalibrationProgressIndicator.setMaxWidth(Double.MAX_VALUE);
      lCalibrationProgressIndicator.setStyle(".percentage { visibility: hidden; }");
      GridPane.setRowSpan(lCalibrationProgressIndicator, 3);
      add(lCalibrationProgressIndicator, 2, 0);

      pCalibrationEngine.getProgressVariable()
                        .addEdgeListener((n) -> {
                          Platform.runLater(() -> {
                            lCalibrationProgressIndicator.setProgress(pCalibrationEngine.getProgressVariable()
                                                                                        .get());
                          });
                        });

    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      addCheckBoxForCalibrationModule("Z ",
                                      pCalibrationEngine.getCalibrateZVariable(),
                                      0,
                                      lRow);
      addCheckBoxForCalibrationModule("XY",
                                      pCalibrationEngine.getCalibrateXYVariable(),
                                      0,
                                      lRow );

      lRow ++;
      addCheckBoxForCalibrationModule("A ",
                                      pCalibrationEngine.getCalibrateAVariable(),
                                      1,
                                      lRow);
      addCheckBoxForCalibrationModule("P ",
                                      pCalibrationEngine.getCalibratePVariable(),
                                      1,
                                      lRow);

      lRow ++;
      addCheckBoxForCalibrationModule("W ",
                                      pCalibrationEngine.getCalibrateWVariable(),
                                      1,
                                      lRow);

      lRow ++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      OnOffArrayPane lCalibrateLightSheetOnOffPane =
                                                   new OnOffArrayPane();

      int lNumberOfLightSheets =
                               pCalibrationEngine.getLightSheetMicroscope()
                                                 .getNumberOfLightSheets();
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        lCalibrateLightSheetOnOffPane.addSwitch("LS" + l,
                                                pCalibrationEngine.getCalibrateLightSheetOnOff(l));
      }

      GridPane.setColumnSpan(lCalibrateLightSheetOnOffPane, 3);
      add(lCalibrateLightSheetOnOffPane, 0, lRow);

      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, lRow);
      lRow++;
    }

    {
      TextField lCalibrationDataNameTextField =
                                              new TextField(pCalibrationEngine.getCalibrationDataNameVariable()
                                                                              .get());
      lCalibrationDataNameTextField.setMaxWidth(Double.MAX_VALUE);
      lCalibrationDataNameTextField.textProperty()
                                   .addListener((obs, o, n) -> {
                                     String lName = n.trim();
                                     if (!lName.isEmpty())
                                       pCalibrationEngine.getCalibrationDataNameVariable()
                                                         .set(lName);

                                   });
      GridPane.setColumnSpan(lCalibrationDataNameTextField, 3);
      GridPane.setFillWidth(lCalibrationDataNameTextField, true);
      GridPane.setHgrow(lCalibrationDataNameTextField,
                        Priority.ALWAYS);
      add(lCalibrationDataNameTextField, 0, lRow);

      lRow++;
    }

    {
      Button lSaveCalibration = new Button("Save");
      lSaveCalibration.setAlignment(Pos.CENTER);
      lSaveCalibration.setMaxWidth(Double.MAX_VALUE);
      lSaveCalibration.setOnAction((e) -> {
        try
        {
          pCalibrationEngine.save();
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      });
      GridPane.setColumnSpan(lSaveCalibration, 1);
      add(lSaveCalibration, 0, lRow);

      Button lLoadCalibration = new Button("Load");
      lLoadCalibration.setAlignment(Pos.CENTER);
      lLoadCalibration.setMaxWidth(Double.MAX_VALUE);
      lLoadCalibration.setOnAction((e) -> {
        try
        {
          pCalibrationEngine.load();
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      });
      GridPane.setColumnSpan(lLoadCalibration, 1);
      add(lLoadCalibration, 1, lRow);

      Button lResetCalibration = new Button("Reset");
      lResetCalibration.setAlignment(Pos.CENTER);
      lResetCalibration.setMaxWidth(Double.MAX_VALUE);
      lResetCalibration.setOnAction((e) -> {
        pCalibrationEngine.reset();
      });
      GridPane.setColumnSpan(lResetCalibration, 1);
      add(lResetCalibration, 2, lRow);

      lRow++;
    }



    {
      TabPane lTabPane = new TabPane();
      TitledPane lTitledPane = new TitledPane("Parameters", lTabPane);
      lTitledPane.setAnimated(false);

      ArrayList<CalibrationModuleInterface>
          lModuleList = pCalibrationEngine.getModuleList();


      for (CalibrationModuleInterface lCalibrationModule : lModuleList)
      {
        try
        {
          Class<?> lCalibrationModuleClass =
              lCalibrationModule.getClass();
          String lCalibrationModuleClassName =
              lCalibrationModuleClass.getSimpleName();
          String lCalibrationModulePanelClassName =
              lCalibrationModuleClass.getPackage()
                                    .getName()
              + ".gui."
              + lCalibrationModuleClassName
              + "Panel";
          info("Searching for class %s as panel for calibration module %s \n",
               lCalibrationModulePanelClassName,
               lCalibrationModuleClassName);
          Class<?> lClass =
              Class.forName(lCalibrationModulePanelClassName);
          Constructor<?> lConstructor =
              lClass.getConstructor(lCalibrationModule.getClass());
          Node lPanel =
              (Node) lConstructor.newInstance(lCalibrationModule);

          Tab lTab = new Tab(lCalibrationModule.getName());
          lTab.setClosable(false);
          lTab.setContent(lPanel);
          lTabPane.getTabs().add(lTab);

        }
        catch (ClassNotFoundException e)
        {
          warning("Cannot find panel for module %s \n",
                  lCalibrationModule.getClass().getSimpleName());
          // e.printStackTrace();
        }
        catch (Throwable e)
        {
          e.printStackTrace();
        }
      }
      GridPane.setColumnSpan(lTitledPane, 3);
      add(lTitledPane, 0, lRow);
      lRow++;
    }

    {



      CalibrationStatePanel lCalibrationStatePanel = new CalibrationStatePanel(pCalibrationEngine);
      //GridPane.setColumnSpan(lCalibrationStatePanel, 4);
      //add(lCalibrationStatePanel,0, lRow);
      //lRow++;

      TitledPane lTitledPane = new TitledPane("Calibration state", lCalibrationStatePanel);
      lTitledPane.setAnimated(false);
      lTitledPane.setCollapsible(false);
      GridPane.setColumnSpan(lTitledPane, 4);
      add(lTitledPane, 0, lRow);
      lRow++;
    }

  }

  private void addCheckBoxForCalibrationModule(String pName,
                                               Variable<Boolean> lCalibrateVariable,
                                               int pColumn,
                                               int pRow)
  {
    CustomGridPane lGroupGridPane = new CustomGridPane(0, 3);

    VariableCheckBox lCheckBox =
                               new VariableCheckBox(pName,
                                                    lCalibrateVariable);

    lCheckBox.getLabel().setAlignment(Pos.CENTER_LEFT);
    lCheckBox.getLabel().setMaxWidth(Double.MAX_VALUE);

    lCheckBox.getCheckBox().setAlignment(Pos.CENTER_RIGHT);
    lCheckBox.getCheckBox().setMaxWidth(Double.MAX_VALUE);

    lGroupGridPane.add(lCheckBox.getLabel(), 0, 0);
    lGroupGridPane.add(lCheckBox.getCheckBox(), 1, 0);

    lGroupGridPane.setMaxWidth(Double.MAX_VALUE);

    add(lGroupGridPane, pColumn, pRow);

    lCalibrateVariable.setCurrent();
  }
}
