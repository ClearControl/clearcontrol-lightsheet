package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.microscope.MicroscopeInterface;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.timelapse.gui.TimelapseToolbar;

import java.util.ArrayList;

/**
 * Lightsheet Timelapse toolbar
 *
 * @author royer
 */
public class LightSheetTimelapseToolbar extends TimelapseToolbar
{
  /**
   * Instanciates a lightsheet timelapse toolbar.
   * 
   * @param pLightSheetTimelapse
   *          timelapse device
   */
  public LightSheetTimelapseToolbar(LightSheetTimelapse pLightSheetTimelapse)
  {
    super(pLightSheetTimelapse);

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

      String[] lFilters = {"Acquisition:", "Adaptation:", "Fusion:", "IO:", "Laser:", "Memory:", "State:", "Timing:", "Visualisation:", ""};

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
      lListView.setMaxWidth(Double.MAX_VALUE);
      lListView.setMinWidth(300);

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
          for (int i : lListView.getSelectionModel()
                                .getSelectedIndices()
                                .sorted())
          {
            lSchedulerList.remove(i - count);
            count++;
          }
          lListView.setItems(FXCollections.observableArrayList(
              lSchedulerList));
        });
        GridPane.setValignment(lMinusButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lMinusButton, 1, lRow);
        lRow++;
      }

      lRow = 0;
      {
        Label lLabel = new Label("Add instruction");
        lSchedulerChecklistGridPane.add(lLabel, 3, lRow);
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
        lSchedulerChecklistGridPane.add(lPlusButton, 4, lRow + i);
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

}
