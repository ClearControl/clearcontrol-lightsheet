package clearcontrol.microscope.lightsheet.extendeddepthoffocus.gui;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.devices.cameras.gui.CameraResolutionGrid;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.gui.jfx.var.textfield.StringVariableTextField;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.EDFImagingEngine;
import eu.hansolo.enzo.simpleindicator.SimpleIndicator;

/**
 * This is the user interface for imaging exetended depth of field
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) October 2017
 */
@Deprecated
public class EDFImagingEngineToolbar extends CustomGridPane
{
  public EDFImagingEngineToolbar(EDFImagingEngine pDepthOfFieldImagingEngine)
  {
    int lRow = 0;

    {
      SimpleIndicator lAcquisitionStateIndicator =
                                                 new SimpleIndicator();
      lAcquisitionStateIndicator.indicatorStyleProperty()
                                .set(SimpleIndicator.IndicatorStyle.RED);
      pDepthOfFieldImagingEngine.getIsRunningVariable()
                                .addSetListener((o, n) -> {
                                  lAcquisitionStateIndicator.onProperty()
                                                            .set(n);
                                });

      lAcquisitionStateIndicator.setMinSize(50, 50);

      GridPane.setRowSpan(lAcquisitionStateIndicator, 2);

      add(lAcquisitionStateIndicator, 0, 0);
    }

    {
      Button lStart = new Button("Start");
      lStart.setAlignment(Pos.CENTER);
      lStart.setMaxWidth(Double.MAX_VALUE);
      lStart.setOnAction((e) -> {
        pDepthOfFieldImagingEngine.startTask();
      });
      // GridPane.setColumnSpan(lStart, 2);
      GridPane.setHgrow(lStart, Priority.ALWAYS);
      add(lStart, 1, lRow);

      lRow++;
    }

    {
      Button lStop = new Button("Stop");
      lStop.setAlignment(Pos.CENTER);
      lStop.setMaxWidth(Double.MAX_VALUE);
      lStop.setOnAction((e) -> {
        pDepthOfFieldImagingEngine.stopTask();
      });
      // GridPane.setColumnSpan(lStop, 2);
      GridPane.setHgrow(lStop, Priority.ALWAYS);
      add(lStop, 1, lRow);

      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 2);
      add(lSeparator, 0, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("Result stack/sequence properties");
      add(lLabel, 0, lRow);
      lRow++;
    }
    {
      addDoubleField(pDepthOfFieldImagingEngine.getFirstZ(), lRow);
      lRow++;
    }
    {
      addDoubleField(pDepthOfFieldImagingEngine.getLastZ(), lRow);
      lRow++;
    }

    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfStackSlicesVariable(),
                      lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfIterations(),
                      lRow);
      lRow++;
    }

    /*
    {
      addCheckbox(pDepthOfFieldImagingEngine.getDetectionArmFixedVariable(), lRow);
      lRow++;
    }
    */

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 2);
      add(lSeparator, 0, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("EDF stack properties");
      add(lLabel, 0, lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfISamples(),
                      lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getNumberOfDSamples(),
                      lRow);
      lRow++;
    }
    {
      addDoubleField(pDepthOfFieldImagingEngine.getMinimumRange(),
                     lRow);
      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 2);
      add(lSeparator, 0, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("Imaging settings");
      add(lLabel, 0, lRow);
      lRow++;
    }

    {
      addDoubleField(pDepthOfFieldImagingEngine.getExposureTimeForEDFInSeconds(),
                     lRow);
      lRow++;
    }

    {
      addDoubleField(pDepthOfFieldImagingEngine.getExposureTimeForStacksInSeconds(),
                     lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getLightSheetMinIndex(),
                      lRow);
      lRow++;
    }
    {
      addIntegerField(pDepthOfFieldImagingEngine.getLightSheetMaxIndex(),
                      lRow);
      lRow++;
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 2);
      add(lSeparator, 0, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("File storage settings");
      add(lLabel, 0, lRow);
      lRow++;
    }
    {
      VariableFileChooser lRootFolderChooser =
                                             new VariableFileChooser("Folder:",
                                                                     pDepthOfFieldImagingEngine.getRootFolderVariable(),
                                                                     true);
      GridPane.setColumnSpan(lRootFolderChooser.getLabel(),
                             Integer.valueOf(1));
      GridPane.setColumnSpan(lRootFolderChooser.getTextField(),
                             Integer.valueOf(2));
      GridPane.setColumnSpan(lRootFolderChooser.getButton(),
                             Integer.valueOf(1));
      this.add(lRootFolderChooser.getLabel(), 0, lRow);
      this.add(lRootFolderChooser.getTextField(), 1, lRow);
      this.add(lRootFolderChooser.getButton(), 3, lRow);

      lRow++;
    }

    {
      StringVariableTextField lPostFixTextField =
                                                new StringVariableTextField("Name:",
                                                                            pDepthOfFieldImagingEngine.getDataSetNamePostfixVariable());
      /*ClassComboBoxVariable
          lStackSinkComboBox = new ClassComboBoxVariable(pTimelapseInterface.getCurrentFileStackSinkTypeVariable(), pTimelapseInterface.getFileStackSinkTypeList(), 100);*/
      GridPane.setColumnSpan(lPostFixTextField.getLabel(),
                             Integer.valueOf(1));
      GridPane.setColumnSpan(lPostFixTextField.getTextField(),
                             Integer.valueOf(2));
      /*GridPane.setColumnSpan(lStackSinkComboBox, Integer.valueOf(1));*/
      this.add(lPostFixTextField.getLabel(), 0, lRow);
      this.add(lPostFixTextField.getTextField(), 1, lRow);
      /*this.add(lStackSinkComboBox, 3, lRow);*/

      lRow++;
    }

    {
      addCheckbox(pDepthOfFieldImagingEngine.getSaveEDFStacks(),
                  lRow);
      lRow++;
    }

    {
      addCheckbox(pDepthOfFieldImagingEngine.getSaveCameraStacks(),
                  lRow);
      lRow++;
    }

    {
      addCheckbox(pDepthOfFieldImagingEngine.getSaveFusedStacks(),
                  lRow);
      lRow++;
    }

    {

      CameraResolutionGrid.ButtonEventHandler lButtonHandler =
                                                             (w,
                                                              h) -> {
                                                               return event -> {
                                                                 pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                                                                           .setCameraWidthHeight(w,
                                                                                                                 h);
                                                               };
                                                             };

      final int lMaxCameraWidth =
                                pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                                          .getDevice(StackCameraDeviceInterface.class,
                                                                     0)
                                                          .getMaxWidthVariable()
                                                          .get()
                                                          .intValue();
      final int lMaxCameraHeight =
                                 pDepthOfFieldImagingEngine.getLightSheetMicroscope()
                                                           .getDevice(StackCameraDeviceInterface.class,
                                                                      0)
                                                           .getMaxHeightVariable()
                                                           .get()
                                                           .intValue();

      CameraResolutionGrid lGridPane =
                                     new CameraResolutionGrid(lButtonHandler,
                                                              lMaxCameraWidth,
                                                              lMaxCameraHeight);
      lGridPane.setAlignment(Pos.BASELINE_CENTER);
      GridPane.setHalignment(lGridPane, HPos.CENTER);
      GridPane.setHgrow(lGridPane, Priority.ALWAYS);
      GridPane.setColumnSpan(lGridPane, 3);
      GridPane.setRowSpan(lGridPane, 7);
      add(lGridPane, 2, 4);

      // setGridLinesVisible(true);

      lRow++;
    }

  }

}
