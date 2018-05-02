package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleReader;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.microscope.MicroscopeInterface;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.timelapse.gui.TimelapseToolbar;
import org.ejml.data.DenseMatrix64F;

import java.io.File;
import java.util.ArrayList;

/**
 * Lightsheet Timelapse toolbar
 *
 * @author royer
 */
public class LightSheetTimelapseToolbar extends TimelapseToolbar
{
  final LightSheetTimelapse mLightSheetTimelapse;


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

    mLightSheetTimelapse = pLightSheetTimelapse;

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

      String[] lFilters = {"Acquisition:", "Adaptation:", "Fusion:", "IO:", "Laser:", "Memory:", "Post-processing:", "State:", "Timing:", "Visualisation:", "FilterWheel:"};

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


      ArrayList<SchedulerInterface> lSchedulerList = pLightSheetTimelapse.getListOfActivatedSchedulers();
      ListView<SchedulerInterface> lListView = new ListView<SchedulerInterface>();
      lListView.setItems(FXCollections.observableArrayList(lSchedulerList));
      //lListView.setPrefWidth(Double.MAX_VALUE);
      //lListView.setMaxWidth(Double.MAX_VALUE);
      //lListView.setMinHeight(35);
      //lListView.setMaxHeight(Double.MAX_VALUE);
      lListView.setMinWidth(450);

      lSchedulerChecklistGridPane.add(lListView, 0, lRow, 1, lFilters.length);

      {
        Button lMoveUpButton = new Button("^");
        lMoveUpButton.setMinWidth(35);
        lMoveUpButton.setMinHeight(35);
        lMoveUpButton.setOnAction((e) -> {
          int i = lListView.getSelectionModel().getSelectedIndex();
          if (i > 1)
          {
            SchedulerInterface lSchedulerInterface = lSchedulerList.get(i);
            lSchedulerList.remove(i);
            lSchedulerList.add(i - 1, lSchedulerInterface);
            lListView.setItems(FXCollections.observableArrayList(
                lSchedulerList));
          }
        });
        lSchedulerChecklistGridPane.add(lMoveUpButton, 1, lRow);
        lRow++;
      }

      {
        Button lMoveDownButton = new Button("v");
        lMoveDownButton.setMinWidth(35);
        lMoveDownButton.setMinHeight(35);
        lMoveDownButton.setOnAction((e) -> {
          int count = 0;
          int i = lListView.getSelectionModel().getSelectedIndex();
          if (i >= 0 && i < lSchedulerList.size() - 1)
          {
            SchedulerInterface lSchedulerInterface = lSchedulerList.get(i);
            lSchedulerList.remove(i);
            lSchedulerList.add(i + 1, lSchedulerInterface);
            lListView.setItems(FXCollections.observableArrayList(
                lSchedulerList));
          }
        });
        lSchedulerChecklistGridPane.add(lMoveDownButton, 1, lRow);
        lRow++;
      }


      {
        Button lMinusButton = new Button("-");
        lMinusButton.setMinWidth(35);
        lMinusButton.setMinHeight(35);
        lMinusButton.setOnAction((e) -> {
          int count = 0;
          int lSelectedIndex = lListView.getSelectionModel().getSelectedIndex();
          for (int i : lListView.getSelectionModel()
                                .getSelectedIndices()
                                .sorted())
          {
            lSchedulerList.remove(i - count);
            count++;
          }
          lListView.setItems(FXCollections.observableArrayList(
              lSchedulerList));
          lListView.getSelectionModel().select(lSelectedIndex);
        });
        GridPane.setValignment(lMinusButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lMinusButton, 1, lRow);
        lRow++;
      }

      lRow = lFilters.length + 1;
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
              lListView.setItems(FXCollections.observableArrayList(
                  lSchedulerList));
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






      lRow = 0;
      {
        Label lLabel = new Label("Add instruction");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;
      }


      for (int i = 0; i < lFilters.length; i++)
      {
        ArrayList<SchedulerInterface> lAvailableSchedulersList = pLightSheetTimelapse.getListOfAvailableSchedulers(lFilters[i]);
        ComboBox<SchedulerInterface> lAvailableSchedulers = new ComboBox<>();
        lAvailableSchedulers.setItems(FXCollections.observableArrayList(lAvailableSchedulersList));
        lAvailableSchedulers.getSelectionModel().select(0);
        lAvailableSchedulers.setMaxWidth(Double.MAX_VALUE);
        lAvailableSchedulers.setMinHeight(35);
        lAvailableSchedulers.setMinWidth(300);
        lSchedulerChecklistGridPane.add(lAvailableSchedulers, 3, lRow + i);

        Button lPlusButton = new Button("+");
        lPlusButton.setMinWidth(35);
        lPlusButton.setMinHeight(35);
        lPlusButton.setOnAction((e) -> {
          int lSelectedIndexInMainList = lListView.getSelectionModel().getSelectedIndex();
          if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lSchedulerList.size();
          int lSelectedIndexInAddList = lAvailableSchedulers.getSelectionModel().getSelectedIndex();
          lSchedulerList.add(lSelectedIndexInMainList, lAvailableSchedulersList.get(lSelectedIndexInAddList));
          lListView.setItems(FXCollections.observableArrayList(
              lSchedulerList));
        });
        lSchedulerChecklistGridPane.add(lPlusButton, 2, lRow + i);
      }
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



      ArrayList<SchedulerInterface>
          lSchedulerInterfaceList = pLightSheetTimelapse.getMicroscope().getDevices(SchedulerInterface.class);

      int lRow = 0;
      for (SchedulerInterface lSchedulerInterface : lSchedulerInterfaceList) {
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
