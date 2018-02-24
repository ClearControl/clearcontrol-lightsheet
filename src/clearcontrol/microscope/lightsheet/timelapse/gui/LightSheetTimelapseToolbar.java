package clearcontrol.microscope.lightsheet.timelapse.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
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
      CustomGridPane lSchedulerChecklistGridPane = new CustomGridPane();

      TitledPane lTitledPane =
          new TitledPane("Schedule",
                         lSchedulerChecklistGridPane);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 4);
      add(lTitledPane, 0, mRow);
      mRow++;

      ArrayList<SchedulerInterface> lSchedulerList = pLightSheetTimelapse.getListOfActivatedSchedulers();
      ListView<SchedulerInterface> lListView = new ListView<SchedulerInterface>();
      lListView.setItems(FXCollections.observableArrayList (lSchedulerList));

      lSchedulerChecklistGridPane.add(lListView, 0, 0, 1, 4);



      {
        Button lMinusButton = new Button("^");
        lMinusButton.setOnAction((e) -> {
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
        lSchedulerChecklistGridPane.add(lMinusButton, 1, 0);
      }

      {
        Button lMinusButton = new Button("v");
        lMinusButton.setOnAction((e) -> {
          int count = 0;
          int i = lListView.getSelectionModel().getSelectedIndex();
          if (i >= 0 && i < lSchedulerList.size() - 2)
          {
            SchedulerInterface lSchedulerInterface = lSchedulerList.get(i);
            lSchedulerList.remove(i);
            lSchedulerList.add(i + 1, lSchedulerInterface);
            lListView.setItems(FXCollections.observableArrayList(
                lSchedulerList));
          }
        });
        lSchedulerChecklistGridPane.add(lMinusButton, 1, 1);
      }


      {
        Button lMinusButton = new Button("-");
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
        lSchedulerChecklistGridPane.add(lMinusButton, 1, 3);
      }

      {
        ArrayList<SchedulerInterface> lAvailableSchedulersList = pLightSheetTimelapse.getListOfAvailableSchedulers();
        ComboBox<SchedulerInterface> lAvailableSchedulers = new ComboBox<>();
        lAvailableSchedulers.setItems(FXCollections.observableArrayList(lAvailableSchedulersList));
        lSchedulerChecklistGridPane.add(lAvailableSchedulers, 0, 4);

        Button lPlusButton = new Button("+");
        lPlusButton.setOnAction((e) -> {
          int lSelectedIndexInMainList = lListView.getSelectionModel().getSelectedIndex();
          if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lSchedulerList.size();
          int lSelectedIndexInAddList = lAvailableSchedulers.getSelectionModel().getSelectedIndex();
          lSchedulerList.add(lSelectedIndexInMainList, lAvailableSchedulersList.get(lSelectedIndexInAddList));
          lListView.setItems(FXCollections.observableArrayList(
              lSchedulerList));
        });
        lSchedulerChecklistGridPane.add(lPlusButton, 1, 4);
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
        lTitledPane.setExpanded(true);
        GridPane.setColumnSpan(lTitledPane, 4);
        add(lTitledPane, 0, mRow);
        mRow++;
      }
    }

  }

}
