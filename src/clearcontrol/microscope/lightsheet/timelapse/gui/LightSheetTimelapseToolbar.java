package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleReader;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.microscope.MicroscopeInterface;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.timelapse.gui.TimelapseToolbar;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Lightsheet Timelapse toolbar
 *
 * @author royer
 */
public class LightSheetTimelapseToolbar extends TimelapseToolbar implements LoggingFeature
{
  LightSheetTimelapse mLightSheetTimelapse = null;

  ScrollPane mPropertiesScrollPane;
  ListView<InstructionInterface> mCurrentProgramScheduleListView;

  private File mProgramTemplateDirectory =
      MachineConfiguration.get()
                          .getFolder("ProgramTemplates");

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

    setPrefSize(400, 200);

    int[] lPercent = new int[]
            { 10, 40, 40, 10 };
    for (int i = 0; i < lPercent.length; i++)
    {
      ColumnConstraints lColumnConstraints = new ColumnConstraints();
      lColumnConstraints.setPercentWidth(lPercent[i]);
      getColumnConstraints().add(lColumnConstraints);
    }


    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, mRow);
      mRow++;
    }



    {

      VariableCheckBox lLegacyTimelapseAcquisitionCheckBox =
          new VariableCheckBox("Legacy timelapse acquisition",
                               pLightSheetTimelapse.getLegacyTimelapseAcquisitionVariable());

      GridPane.setHalignment(lLegacyTimelapseAcquisitionCheckBox.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lLegacyTimelapseAcquisitionCheckBox.getLabel(), 1);
      GridPane.setColumnSpan(lLegacyTimelapseAcquisitionCheckBox.getCheckBox(), 1);

      GridPane.setColumnSpan(lLegacyTimelapseAcquisitionCheckBox.getLabel(), 3);
      add(lLegacyTimelapseAcquisitionCheckBox.getCheckBox(), 0, mRow);
      add(lLegacyTimelapseAcquisitionCheckBox.getLabel(), 1, mRow);

      mRow++;
    }

    {
      int lRow = 0;
      CustomGridPane lSchedulerChecklistGridPane = new CustomGridPane();
      /*lPercent = new int[]
              { 45, 5, 45, 5};
      for (int i = 0; i < lPercent.length; i++)
      {
        ColumnConstraints lColumnConstraints = new ColumnConstraints();
        lColumnConstraints.setPercentWidth(lPercent[i]);
        lSchedulerChecklistGridPane.getColumnConstraints().add(lColumnConstraints);
      }*/


      TitledPane lTitledPane =
              new TitledPane("Schedule",
                      lSchedulerChecklistGridPane);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 4);
      add(lTitledPane, 0, mRow);
      mRow++;

      {
        Label lLabel = new Label("Current program");
        lSchedulerChecklistGridPane.add(lLabel, 0, lRow);
        lRow++;
      }


      ArrayList<InstructionInterface> lSchedulerList = pLightSheetTimelapse.getListOfActivatedSchedulers();
      mCurrentProgramScheduleListView = new ListView<InstructionInterface>();
      mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lSchedulerList));
      refreshPropertiesScrollPane();
      //mCurrentProgramScheduleListView.setPrefWidth(Double.MAX_VALUE);
      //mCurrentProgramScheduleListView.setMaxWidth(Double.MAX_VALUE);
      mCurrentProgramScheduleListView.setMinHeight(300);
      //mCurrentProgramScheduleListView.setMaxHeight(Double.MAX_VALUE);
      mCurrentProgramScheduleListView.setMinWidth(450);


      mCurrentProgramScheduleListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent mouseEvent) {
                                      /**
                                       * Dirty hack: Use Java reflections to discover a matching panel
                                       * TODO: find a better way of doing this, without reflections
                                       */
                                      if (mouseEvent.getClickCount() > 0) {
                                        refreshPropertiesScrollPane();
                                      }
                                    }
                                  });

      lSchedulerChecklistGridPane.add(mCurrentProgramScheduleListView, 0, lRow, 1, 9);

      {
        Button lMoveUpButton = new Button("^");
        lMoveUpButton.setTooltip(new Tooltip("Move up"));
        lMoveUpButton.setMinWidth(35);
        lMoveUpButton.setMinHeight(35);
        lMoveUpButton.setOnAction((e) -> {
          int i = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (i > 0)
          {
            InstructionInterface lInstructionInterface = lSchedulerList.get(i);
            lSchedulerList.remove(i);
            lSchedulerList.add(i - 1, lInstructionInterface);
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                lSchedulerList));
            refreshPropertiesScrollPane();
          }
        });
        lSchedulerChecklistGridPane.add(lMoveUpButton, 1, lRow);
        lRow++;
      }

      {
        Button lMoveDownButton = new Button("v");
        lMoveDownButton.setTooltip(new Tooltip("Move down"));
        lMoveDownButton.setMinWidth(35);
        lMoveDownButton.setMinHeight(35);
        lMoveDownButton.setOnAction((e) -> {
          int count = 0;
          int i = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (i >= 0 && i < lSchedulerList.size() - 1)
          {
            InstructionInterface lInstructionInterface = lSchedulerList.get(i);
            lSchedulerList.remove(i);
            lSchedulerList.add(i + 1, lInstructionInterface);
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                lSchedulerList));
            refreshPropertiesScrollPane();
          }
        });
        lSchedulerChecklistGridPane.add(lMoveDownButton, 1, lRow);
        lRow++;
      }


      {
        Button lMinusButton = new Button("-");
        lMinusButton.setTooltip(new Tooltip("Remove"));
        lMinusButton.setMinWidth(35);
        lMinusButton.setMinHeight(35);
        lMinusButton.setOnAction((e) -> {
          int count = 0;
          int lSelectedIndex = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          for (int i : mCurrentProgramScheduleListView.getSelectionModel()
                                .getSelectedIndices()
                                .sorted())
          {
            lSchedulerList.remove(i - count);
            count++;
          }
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
              lSchedulerList));
          mCurrentProgramScheduleListView.getSelectionModel().select(lSelectedIndex);
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lMinusButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lMinusButton, 1, lRow);
        lRow++;
      }

      {
        Button lUnselectButton = new Button("[]");
        lUnselectButton.setTooltip(new Tooltip("Unselect"));
        lUnselectButton.setMinWidth(35);
        lUnselectButton.setMinHeight(35);
        lUnselectButton.setOnAction((e) -> {
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                  lSchedulerList));
          mCurrentProgramScheduleListView.getSelectionModel().select(-1);
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lUnselectButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lUnselectButton, 1, lRow);
        lRow++;
      }


      {
        Button lCloneButton = new Button("++");
        lCloneButton.setTooltip(new Tooltip("Clone"));
        lCloneButton.setMinWidth(35);
        lCloneButton.setMinHeight(35);
        lCloneButton.setOnAction((e) -> {
          int lSelectedIndex = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (lSelectedIndex > -1) {
            lSchedulerList.add(lSelectedIndex, lSchedulerList.get(lSelectedIndex).copy());
          }
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                  lSchedulerList));
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lCloneButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lCloneButton, 1, lRow);
        lRow++;
      }

      /*
      {
        Button lLinkButton = new Button("->");
        lLinkButton.setTooltip(new Tooltip("Link (experimental)"));
        lLinkButton.setMinWidth(35);
        lLinkButton.setMinHeight(35);
        lLinkButton.setOnAction((e) -> {
          int lSelectedIndex = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (lSelectedIndex > -1) {
            lSchedulerList.add(lSelectedIndex, lSchedulerList.get(lSelectedIndex));
          }
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                  lSchedulerList));
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lLinkButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lLinkButton, 1, lRow);
        lRow++;
      }
      */




      lRow = 10;
      {
        ComboBox lExistingScheduleTemplates;
        {
          // load
          lExistingScheduleTemplates = new ComboBox(
              listExistingSchedulerTemplateFiles());
          lSchedulerChecklistGridPane.add(lExistingScheduleTemplates, 0, lRow);

          Button lLoadScheduleTemplateBytton = new Button("Load");
          lLoadScheduleTemplateBytton.setMaxWidth(Double.MAX_VALUE);
          lLoadScheduleTemplateBytton.setOnAction((e) -> {
            try
            {
              mLightSheetTimelapse.getListOfActivatedSchedulers().clear();
              new ScheduleReader(lSchedulerList,
                                 (LightSheetMicroscope) mLightSheetTimelapse.getMicroscope(), getFile(lExistingScheduleTemplates.getValue().toString())).read();
              mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                  lSchedulerList));
              refreshPropertiesScrollPane();
            }
            catch (Exception e1)
            {
              e1.printStackTrace();
            }
          });

          lSchedulerChecklistGridPane.add(lLoadScheduleTemplateBytton, 1, lRow, 2, 1);
          lRow++;

        }

        {
          // save
          Variable<String>
              lFileNameVariable = new Variable<String>("filename", "Program");

          TextField lFileNameTextField =
              new TextField(lFileNameVariable.get());
          lFileNameTextField.setMaxWidth(Double.MAX_VALUE);
          lFileNameTextField.textProperty()
                            .addListener((obs, o, n) -> {
                              String lName = n.trim();
                              if (!lName.isEmpty())
                                lFileNameVariable.set(lName);
                            });
          lSchedulerChecklistGridPane.add(lFileNameTextField, 0, lRow);

          Button lSaveScheduleButton = new Button("Save");
          lSaveScheduleButton.setAlignment(Pos.CENTER);
          lSaveScheduleButton.setMaxWidth(Double.MAX_VALUE);
          lSaveScheduleButton.setOnAction((e) -> {
            try
            {
              new ScheduleWriter(mLightSheetTimelapse.getListOfActivatedSchedulers(), getFile(lFileNameVariable.get())).write();
              lExistingScheduleTemplates.setItems(listExistingSchedulerTemplateFiles());
            }
            catch (Exception e1)
            {
              e1.printStackTrace();
            }
          });
          GridPane.setColumnSpan(lSaveScheduleButton, 1);
          lSchedulerChecklistGridPane.add(lSaveScheduleButton, 1, lRow, 2, 1);
          lRow++;
        }

      }




      String[] lFilters = {
              "Acquisition:",
              "Adaptation:",
              "Adaptive optics:",
              "Filter wheel:",
              "Fusion:",

              "IO:",
              "Laser:",
              "Memory:",
              "Post-processing:",
              "Smart:",

              "Timing:",
              "Visualisation:"};

      Node[]   lIcons = {
              MicroscopeNodeType.Acquisition.getIcon(),
              MicroscopeNodeType.AdaptiveOptics.getIcon(),
              MicroscopeNodeType.AdaptiveOptics.getIcon(),
              MicroscopeNodeType.FilterWheel.getIcon(),
              MicroscopeNodeType.Scripting.getIcon(),

              MicroscopeNodeType.Laser.getIcon(),
              MicroscopeNodeType.Scripting.getIcon(),
              MicroscopeNodeType.Scripting.getIcon(),
              MicroscopeNodeType.Scripting.getIcon(),
              MicroscopeNodeType.Scripting.getIcon(),

              MicroscopeNodeType.Scripting.getIcon(),
              MicroscopeNodeType.StackDisplay3D.getIcon()
      };


      lRow = 0;
      // properties panel
      {

        Label lLabel = new Label("Properties");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;

        mPropertiesScrollPane = new ScrollPane();
        mPropertiesScrollPane.setMinHeight(150);
        mPropertiesScrollPane.setMaxHeight(150);
        mPropertiesScrollPane.setMaxHeight(450);
        lSchedulerChecklistGridPane.add(mPropertiesScrollPane, 2, lRow, 2, 5);
        lRow++;

      }

      lRow = 7;
      {
        Label lLabel = new Label("Add instruction");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;

        TreeItem<String> rootItem = buildInstructionTree(pLightSheetTimelapse, lFilters, "", lIcons);
        TreeView<String> tree = new TreeView<String> (rootItem);

        Label lSearchLabel  = new Label("Search");
        lSchedulerChecklistGridPane.add(lSearchLabel, 2, lRow);

        TextField lSearchField = new TextField();
        lSchedulerChecklistGridPane.add(lSearchField, 3, lRow);
        lSchedulerChecklistGridPane.setOnKeyReleased((e) -> {
          info("keyreleased");
          tree.setRoot(buildInstructionTree(mLightSheetTimelapse, lFilters, lSearchField.getText(), lIcons));
        });
        lRow++;

        tree.setMinHeight(150);
        tree.setMinWidth(450);

        tree.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
          @Override
          public void handle(MouseEvent mouseEvent)
          {
            if(mouseEvent.getClickCount() == 2)
            {
              TreeItem<String> item = tree.getSelectionModel().getSelectedItem();
              System.out.println("Selected Text : " + item.getValue());
              if (item.getParent() != null && item.getParent().getValue().compareTo("Instructions") != 0 ) {
                int lSelectedIndexInMainList = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
                if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lSchedulerList.size();
                lSchedulerList.add(lSelectedIndexInMainList, pLightSheetTimelapse.getListOfAvailableSchedulers(item.getParent().getValue(), item.getValue()).get(0).copy());
                mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
                        lSchedulerList));
                if (mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() > -1) {
                  mCurrentProgramScheduleListView.getSelectionModel().select(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() - 1);
                }
                refreshPropertiesScrollPane();
              }
            }
          }
        });

        StackPane lStackPane = new StackPane();
        lStackPane.getChildren().add(tree);

        lSchedulerChecklistGridPane.add(lStackPane, 2, lRow, 2 , 1);
        //lRow+=5;

      }

      // Todo: remove following two blocks
//      lRow += 2;
//      {
//        Label lLabel = new Label("Add instruction (legacy UI)");
//        lSchedulerChecklistGridPane.add(lLabel, 0, lRow, 2, 1);
//        lRow++;
//      }
//
//
//      ArrayList<InstructionInterface> lAvailableSchedulersList = pLightSheetTimelapse.getListOfAvailableSchedulers();
//      if (lAvailableSchedulersList.size() > 0) {
//        ComboBox<InstructionInterface> lAvailableSchedulers = new ComboBox<>();
//        lAvailableSchedulers.setItems(FXCollections.observableArrayList(lAvailableSchedulersList));
//        lAvailableSchedulers.getSelectionModel().select(0);
//        lAvailableSchedulers.setMaxWidth(Double.MAX_VALUE);
//        lAvailableSchedulers.setMinHeight(35);
//        lAvailableSchedulers.setMinWidth(300);
//        lSchedulerChecklistGridPane.add(lAvailableSchedulers, 0, lRow);
//
//        Button lPlusButton = new Button("+");
//        lPlusButton.setMinWidth(35);
//        lPlusButton.setMinHeight(35);
//        lPlusButton.setOnAction((e) -> {
//          int lSelectedIndexInMainList = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
//          if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lSchedulerList.size();
//          int lSelectedIndexInAddList = lAvailableSchedulers.getSelectionModel().getSelectedIndex();
//          lSchedulerList.add(lSelectedIndexInMainList, lAvailableSchedulersList.get(lSelectedIndexInAddList));
//          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(
//                  lSchedulerList));
//          refreshPropertiesScrollPane();
//        });
//        lSchedulerChecklistGridPane.add(lPlusButton, 1, lRow);
//        lRow++;
//      }

    }

/*
    {
      CustomGridPane lSchedulerChecklistGridPane = new CustomGridPane();

      TitledPane lTitledPane =
          new TitledPane("Legacy schedule",
                         lSchedulerChecklistGridPane);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 4);
      add(lTitledPane, 0, mRow);
      mRow++;



      ArrayList<InstructionInterface>
          lSchedulerInterfaceList = pLightSheetTimelapse.getMicroscope().getDevices(InstructionInterface.class);

      int lRow = 0;
      for (InstructionInterface lSchedulerInterface : lSchedulerInterfaceList) {
        VariableCheckBox lSchedulerActiveCheckBox =
            new VariableCheckBox("", lSchedulerInterface.getActiveVariable());

        Label lSchedulerTitleLabel =
            new Label(lSchedulerInterface.getName());

        GridPane.setHalignment(lSchedulerActiveCheckBox.getCheckBox(),
                               HPos.RIGHT);
        GridPane.setColumnSpan(lSchedulerActiveCheckBox.getCheckBox(),
                               1);
        GridPane.setColumnSpan(lSchedulerTitleLabel, 3);

        lSchedulerChecklistGridPane.add(lSchedulerActiveCheckBox.getCheckBox(), 0, lRow);
        lSchedulerChecklistGridPane.add(lSchedulerTitleLabel, 1, lRow);
        lRow++;
      }





    }*/


    /*
    {
      VariableCheckBox lInterleavedAcquisition =
                                               new VariableCheckBox("",
                                                                    pLightSheetTimelapse.getInterleavedAcquisitionVariable());

      Label lInterleavedAcquisitionLabel =
                                         new Label("Interleaved acquisition");

      GridPane.setHalignment(lInterleavedAcquisition.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lInterleavedAcquisition.getCheckBox(),
                             1);
      GridPane.setColumnSpan(lInterleavedAcquisitionLabel, 3);

      add(lInterleavedAcquisition.getCheckBox(), 0, mRow);
      add(lInterleavedAcquisitionLabel, 1, mRow);
      mRow++;
    }*/

    CustomGridPane lAdvancedOptionsGridPane = buildAdvancedOptionsGripPane();
    lAdvancedOptionsGridPane.addSeparator();
    int lRow = lAdvancedOptionsGridPane.getLastUsedRow();

    {

      VariableCheckBox lEDFImagingCheckBox =
          new VariableCheckBox("-> Extended depth of field (EDF)",
                               pLightSheetTimelapse.getExtendedDepthOfFieldAcquisitionVariable());

      GridPane.setHalignment(lEDFImagingCheckBox.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lEDFImagingCheckBox.getLabel(), 1);
      GridPane.setColumnSpan(lEDFImagingCheckBox.getCheckBox(), 1);

      GridPane.setColumnSpan(lEDFImagingCheckBox.getLabel(), 3);
      lAdvancedOptionsGridPane.add(lEDFImagingCheckBox.getCheckBox(), 0, lRow);
      lAdvancedOptionsGridPane.add(lEDFImagingCheckBox.getLabel(), 1, lRow);

      lRow++;
    }


    {
      VariableCheckBox lFuseStacksCheckBox =
                                           new VariableCheckBox("Fuse stacks",
                                                                pLightSheetTimelapse.getFuseStacksVariable());

      GridPane.setHalignment(lFuseStacksCheckBox.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lFuseStacksCheckBox.getLabel(), 1);
      GridPane.setColumnSpan(lFuseStacksCheckBox.getCheckBox(), 1);

      GridPane.setColumnSpan(lFuseStacksCheckBox.getLabel(), 3);
      lAdvancedOptionsGridPane.add(lFuseStacksCheckBox.getCheckBox(), 0, lRow);
      lAdvancedOptionsGridPane.add(lFuseStacksCheckBox.getLabel(), 1, lRow);

      lRow++;
    }

    /*
    {
      VariableCheckBox lFuseStacksPerCameraOnlyCheckBox =
                                                        new VariableCheckBox("Fuse stacks per camera only",
                                                                             pLightSheetTimelapse.getFuseStacksPerCameraVariable());
    
      GridPane.setHalignment(lFuseStacksPerCameraOnlyCheckBox.getCheckBox(),
                             HPos.RIGHT);
      GridPane.setColumnSpan(lFuseStacksPerCameraOnlyCheckBox.getLabel(),
                             1);
      GridPane.setColumnSpan(lFuseStacksPerCameraOnlyCheckBox.getCheckBox(),
                             1);
    
      GridPane.setColumnSpan(lFuseStacksPerCameraOnlyCheckBox.getLabel(),
                             3);
      add(lFuseStacksPerCameraOnlyCheckBox.getCheckBox(), 0, mRow);
      add(lFuseStacksPerCameraOnlyCheckBox.getLabel(), 1, mRow);
    
      mRow++;
    }
    */

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

  private void refreshPropertiesScrollPane() {
    ArrayList<InstructionInterface> lSchedulerList = mLightSheetTimelapse.getListOfActivatedSchedulers();
    if (mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() > -1) {
      InstructionInterface lInstruction = lSchedulerList.get(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex());
      System.out.println("Selected: " + lSchedulerList.get(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex()));
      try {
        Class<?> lInstructionClass =
                lInstruction.getClass();
        String lInstructionClassName =
                lInstructionClass.getSimpleName();
        String lInstructionPanelClassName =
                lInstructionClass.getPackage()
                        .getName()
                        + ".gui."
                        + lInstructionClassName
                        + "Panel";
        info("Searching for class %s as panel for calibration module %s \n",
                lInstructionPanelClassName,
                lInstructionClassName);
        Class<?> lClass =
                Class.forName(lInstructionPanelClassName);
        Constructor<?> lConstructor =
                lClass.getConstructor(lInstruction.getClass());
        Node lPanel =
                (Node) lConstructor.newInstance(lInstruction);

        mPropertiesScrollPane.setContent(lPanel);
      } catch (ClassNotFoundException e) {
        warning("Cannot find panel for module %s \n",
                lInstruction.getClass().getSimpleName());
        // e.printStackTrace();
        mPropertiesScrollPane.setContent(null);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  @NotNull
  private TreeItem<String> buildInstructionTree(LightSheetTimelapse pLightSheetTimelapse, String[] lFilters, String pSearchFilter, Node[] lIcons) {
    TreeItem<String> rootItem = new TreeItem<String> ("Instructions", MicroscopeNodeType.Other.getIcon());
    rootItem.setExpanded(true);
    for (int i = 0; i < lFilters.length; i++)
    {
      ArrayList<InstructionInterface> lAvailableSchedulersList = pLightSheetTimelapse.getListOfAvailableSchedulers(lFilters[i], pSearchFilter);
      if (lAvailableSchedulersList.size() > 0) {

        TreeItem<String> item = new TreeItem<String>(lFilters[i].replace(":", ""), lIcons[i]);
        item.setExpanded(pSearchFilter.length() > 0);
        rootItem.getChildren().add(item);


        for (InstructionInterface lInstructionInterface : lAvailableSchedulersList) {
          TreeItem<String> schedulerItem = new TreeItem<String>(lInstructionInterface.getName().replace(lFilters[i], ""));
          item.getChildren().add(schedulerItem);
        }
      }
    }
    return rootItem;
  }

  private ObservableList<String> listExistingSchedulerTemplateFiles()
  {
    ArrayList<String> filenames = getScheduleTemplateNames();
    ObservableList<String> list =     FXCollections.observableArrayList(filenames);
    return list;
  }


  private File getFile(String pName)
  {
    return new File(mProgramTemplateDirectory, pName + ".txt");
  }

  ArrayList<String> mExistingTemplateFileList = new ArrayList<String>();
  private ArrayList<String> getScheduleTemplateNames() {
    File folder = mProgramTemplateDirectory;

    mExistingTemplateFileList.clear();
    for (File file : folder.listFiles()) {
      if (!file.isDirectory() && file.getAbsolutePath().endsWith(".txt")) {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        mExistingTemplateFileList.add(fileName);
      }
    }

    return mExistingTemplateFileList;
  }


}
