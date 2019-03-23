package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.instructions.ExecutableInstructionList;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.gui.InstructionListBuilderGUI;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.MicroscopeInterface;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.timelapse.gui.TimelapseToolbar;

/**
 * Lightsheet Timelapse toolbar
 *
 * @author royer
 */
public class LightSheetTimelapseToolbar extends TimelapseToolbar
                                        implements LoggingFeature
{
    LightSheetTimelapse mLightSheetTimelapse = null;


  TextArea debugTextArea = new TextArea();

  /**
   * Instanciates a lightsheet timelapse toolbar.
   * 
   * @param pLightSheetTimelapse
   *          timelapse device
   */
  public LightSheetTimelapseToolbar(LightSheetTimelapse pLightSheetTimelapse)
  {
    super(pLightSheetTimelapse);
    mLightSheetTimelapse = pLightSheetTimelapse;

    this.setAlignment(Pos.TOP_LEFT);

    //setPrefSize(400, 200);


    int[] lPercent = new int[]
    { 10, 40, 40, 10 };
    for (int i = 0; i < lPercent.length; i++)
    {
      ColumnConstraints lColumnConstraints = new ColumnConstraints();
      lColumnConstraints.setPercentWidth(lPercent[i]);
      getColumnConstraints().add(lColumnConstraints);
    }


    addStringField(pLightSheetTimelapse.getDatasetComment(), mRow);
    mRow++;

/*
    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, mRow);
      mRow++;
    }
*/
    {
      int lRow = 0;
      ExecutableInstructionList<LightSheetMicroscope> list = pLightSheetTimelapse.getCurrentProgram();
      final CustomGridPane lSchedulerChecklistGridPane = new InstructionListBuilderGUI<LightSheetMicroscope>(list);

      pLightSheetTimelapse.getLastExecutedSchedulerIndexVariable().addSetListener((pCurrentValue, pNewValue) -> {
        {
          info("Timelapse is changing");

          ListView programListView = ((InstructionListBuilderGUI) lSchedulerChecklistGridPane).getCurrentProgramListView();
          Integer index = (Integer) pLightSheetTimelapse.getLastExecutedSchedulerIndexVariable().get();
          if (index >= 0 && index < programListView.getItems().size()) {
            Object object = programListView.getItems().get(index);
            info("object : " + object);
            programListView.getSelectionModel().select(index.intValue());
            programListView.refresh();
            //debugTextArea.setText(((LightSheetMicroscope)pLightSheetTimelapse.getMicroscope()).getDataWarehouse().debugText());
            if ((Integer)pCurrentValue > (Integer)pNewValue) {
              double sumDuration = 0;
              for (Object o : programListView.getItems()) {
                if (o instanceof InstructionInterface) {
                  Double duration = ((InstructionInterface) o).getDuration();
                  if (duration != null) {
                    sumDuration += duration;
                  }
                }
              }
              final double duration = sumDuration;
              Platform.runLater(() -> {
                ((InstructionListBuilderGUI) lSchedulerChecklistGridPane).getTitledPane().setText("Schedule {" + String.format("%.1f", duration)  + " ms}");
              });
            }
          }
        }
      });

      GridPane.setColumnSpan(lSchedulerChecklistGridPane, 3);
      add(lSchedulerChecklistGridPane, 0, mRow);

      CustomGridPane pane = new CustomGridPane();

      pane.add(debugTextArea, 0, 0);

      TitledPane debugPane = new TitledPane("Debug", pane);
      GridPane.setFillHeight(debugPane, true);
      GridPane.setFillWidth(debugPane, true);
      add(debugPane, 3, mRow);
      mRow++;
    }

    CustomGridPane lAdvancedOptionsGridPane =
                                            buildAdvancedOptionsGripPane();
    lAdvancedOptionsGridPane.addSeparator();
    int lRow = lAdvancedOptionsGridPane.getLastUsedRow();

    {
      MicroscopeInterface lMicroscopeInterface =
                                               pLightSheetTimelapse.getMicroscope();
      AdaptiveEngine lAdaptiveEngine =
                                     (AdaptiveEngine) lMicroscopeInterface.getDevice(AdaptiveEngine.class,
                                                                                     0);

      if (lAdaptiveEngine != null)
      {
        int lNumberOfLightSheets = 1;
        if (lMicroscopeInterface instanceof LightSheetMicroscope)
        {
          lNumberOfLightSheets =
                               ((LightSheetMicroscope) lMicroscopeInterface).getNumberOfLightSheets();
        }

        ConfigurationStatePanel lConfigurationStatePanel =
                                                         new ConfigurationStatePanel(lAdaptiveEngine.getModuleList(),
                                                                                     lNumberOfLightSheets);

        TitledPane lTitledPane =
                               new TitledPane("Adaptation state",
                                              lConfigurationStatePanel);
        lTitledPane.setAnimated(false);
        lTitledPane.setExpanded(false);
        GridPane.setColumnSpan(lTitledPane, 4);
        add(lTitledPane, 0, mRow);
        mRow++;
      }
    }

  }


}
