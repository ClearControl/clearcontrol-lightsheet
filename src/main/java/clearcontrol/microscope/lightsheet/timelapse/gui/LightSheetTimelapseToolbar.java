package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.instructions.ExecutableInstructionList;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.gui.InstructionListBuilderGUI;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouseUtilities;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
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
      final String defaultStyle = "-fx-background-color: #EEEEEE;";
      final String defaultSelectedStyle = "-fx-background-color: #CCCCCC; -fx-text-fill: #000000;";

      final String imageProducerStyle = "-fx-background-color: #DDFFDD;";
      final String imageProducerSelectedStyle = "-fx-background-color: #BBFFBB; -fx-text-fill: #000000;";

      final String imageConsumerStyle = "-fx-background-color: #FFDDDD;";
      final String imageConsumerSelectedStyle = "-fx-background-color: #FFBBBB; -fx-text-fill: #000000;";

      final String imageProcessorStyle = "-fx-background-color: #FFFFDD;";
      final String imageProcessorSelectedStyle = "-fx-background-color: #FFFFBB; -fx-text-fill: #000000;";

      final String imageAnalyserStyle = "-fx-background-color: #DDFFFF;";
      final String imageAnalyserSelectedStyle = "-fx-background-color: #BBFFFF; -fx-text-fill: #000000;";

      ExecutableInstructionList<LightSheetMicroscope> list = pLightSheetTimelapse.getCurrentProgram();
      final InstructionListBuilderGUI<LightSheetMicroscope> lSchedulerChecklistGridPane = new InstructionListBuilderGUI<LightSheetMicroscope>(list);
      lSchedulerChecklistGridPane.getCurrentProgramListView().setCellFactory(param -> new ListCell<InstructionInterface>() {
        @Override
        protected void updateItem(InstructionInterface item, boolean empty) {
          super.updateItem(item, empty);

          if (empty || item == null) {
            setText(null);
            setStyle(null);
          } else if (item instanceof InstructionInterface) {
            //info("Selected in: " + lSchedulerChecklistGridPane.getSelectedInstruction());
            boolean isSelected = (lSchedulerChecklistGridPane.getSelectedInstruction() == item);
            //info("Selected: " + isSelected);

             if (!(item instanceof DataWarehouseInstructionBase)) {
              setText(item.toString());
              setStyle(isSelected?defaultSelectedStyle:defaultStyle);
            } else {
              setText(item.toString());
              DataWarehouseInstructionBase instruction = (DataWarehouseInstructionBase) item;
              Class[] consumedContainers = instruction.getConsumedContainerClasses();
              // todo: this might be a temporary workaround. The classes should know by themselfes if they have
              //       auto-recycling on or not.
              if (!(instruction instanceof AutoRecyclerInstructionInterface)) {
                consumedContainers = new Class[0];
              }
              Class[] producedContainers = instruction.getProducedContainerClasses();


              if (DataWarehouseUtilities.containsImageContainer(producedContainers)) {// image producer
                if (DataWarehouseUtilities.containsImageContainer(consumedContainers)) {// image consumer and producer, aka processor
                  setStyle(isSelected ? imageProcessorSelectedStyle : imageProcessorStyle);
                } else { // image producer only
                  setStyle(isSelected ? imageProducerSelectedStyle : imageProducerStyle);
                }
              } else if (DataWarehouseUtilities.containsImageContainer(consumedContainers)) { // image consumer only
                if (producedContainers.length > 0) { // image consumer, producing something else; aka analyser
                  setStyle(isSelected ? imageAnalyserSelectedStyle : imageAnalyserStyle);
                } else { // image consumer only
                  setStyle(isSelected ? imageConsumerSelectedStyle : imageConsumerStyle);
                }
              } else {
                setStyle(isSelected ? defaultSelectedStyle : defaultStyle);
              }
              if (instruction.getDescription() != null && instruction.getDescription().startsWith("DEPRECATED:")) {
                setStyle(getStyle() + " -fx-strikethrough: true;");
              }
            }
          }
        }
      });

      pLightSheetTimelapse.getLastExecutedSchedulerIndexVariable().addSetListener((pCurrentValue, pNewValue) -> {
        {
          info("Timelapse is changing");

          ListView programListView = lSchedulerChecklistGridPane.getCurrentProgramListView();
          Integer index = (Integer) pLightSheetTimelapse.getLastExecutedSchedulerIndexVariable().get();
          if (index >= 0 && index < programListView.getItems().size()) {
            Object object = programListView.getItems().get(index);
            info("object : " + object);
            programListView.getSelectionModel().select(index.intValue());
            lSchedulerChecklistGridPane.refreshPropertiesScrollPane();
            programListView.refresh();

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
                System.out.println("refreshing GUI");
                ((InstructionListBuilderGUI) lSchedulerChecklistGridPane).getTitledPane().setText("Schedule {" + String.format("%.1f", duration)  + " ms}");
              });
            }
          }
        }
      });


      GridPane.setColumnSpan(lSchedulerChecklistGridPane, 3);
      GridPane.setRowSpan(lSchedulerChecklistGridPane, 2);
      add(lSchedulerChecklistGridPane, 0, mRow);


      { // instruction kind legend
        CustomGridPane legend = new CustomGridPane();

        String legendStyle = " -fx-border-width: 3px, 3px, 3px, 3px;";

        Label producerLegend = new Label("Image producer\n[] -> [I]");
        producerLegend.setStyle(imageProducerStyle + legendStyle);
        legend.add(producerLegend, 0,0 );

        Label processorLegend = new Label("Image processor\n[I] -> [I]");
        processorLegend.setStyle(imageProcessorStyle + legendStyle);
        legend.add(processorLegend, 0,1 );

        Label analyserLegend = new Label("Image analyser\n[I] -> [A]");
        analyserLegend.setStyle(imageAnalyserStyle + legendStyle);
        legend.add(analyserLegend, 0,2 );

        Label consumerLegend = new Label("Image consumer\n[I] -> []");
        consumerLegend.setStyle(imageConsumerStyle + legendStyle);
        legend.add(consumerLegend, 0,3 );

        add(legend, 3, mRow, 2, 1);
        mRow++;
      }
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
