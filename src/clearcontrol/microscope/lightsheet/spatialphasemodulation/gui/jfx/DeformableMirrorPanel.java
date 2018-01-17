package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors.*;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.ImagePane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceBase;

import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.ejml.data.DenseMatrix64F;

import java.io.File;
import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class DeformableMirrorPanel extends CustomGridPane
                                   implements LoggingFeature
{
  SpatialPhaseModulatorDeviceBase mSpatialPhaseModulatorDevice;

  ImagePane mPreviewPane;
  ImagePane mPreviewPaneScaled;
  Label mPreviewScaledLabel;

  //GraphicsContext mGraphicsContext;
  ArrayList<MatrixUpdateReceiver> mListMatrixUpdateReceivers = new ArrayList<>();

  Variable<DenseMatrix64F> mEditorMatrixVariable;

  private File mMirrorModeDirectory =
      MachineConfiguration.get()
                          .getFolder("MirrorModes");

  public DeformableMirrorPanel(SpatialPhaseModulatorDeviceBase pAbstractDeformableMirrorDevice)
  {
    super();

    int lRow = 0;

    mSpatialPhaseModulatorDevice = pAbstractDeformableMirrorDevice;

    DenseMatrix64F
        lMatrixReference =
        mSpatialPhaseModulatorDevice.getMatrixReference().get();

    // Preview
    final int lPreviewWidth = 100;
    final int lPreviewHeight = 100;

    mEditorMatrixVariable =
        new Variable<DenseMatrix64F>("editorMatrix", lMatrixReference.copy());
    mEditorMatrixVariable.addSetListener((DenseMatrix64F lOldMatrix, DenseMatrix64F lNewMatrix) -> {
      updateVisualisation(lNewMatrix, lPreviewWidth, lPreviewHeight);
      updateEditors(lNewMatrix);
    });


    {
      mPreviewPane = new ImagePane(lPreviewWidth, lPreviewHeight);
      this.add(mPreviewPane, 0, lRow, 1, 4);
    }

    {
      mPreviewPaneScaled = new ImagePane(lPreviewWidth, lPreviewHeight);
      this.add(mPreviewPaneScaled, 1, lRow, 1, 4);
    }

    {
      Label lPreviewLabel = new Label("Full range");
      lPreviewLabel.setAlignment(Pos.CENTER);
      this.add(lPreviewLabel, 0, 4, 1, 1);
    }

    {
      mPreviewScaledLabel = new Label("Range");
      mPreviewScaledLabel.setAlignment(Pos.CENTER);
      this.add(mPreviewScaledLabel, 1, 4, 1, 1);
    }

    CheckBox lFlipXYCheckBox = new CheckBox("Flip XY");
    this.add(lFlipXYCheckBox, 4, 0);
    CheckBox lFlipVerticalCheckBox = new CheckBox("Flip vertical");
    this.add(lFlipVerticalCheckBox, 5, 0);
    CheckBox lFlipHorizontalCheckBox = new CheckBox("Flip horizontal");
    this.add(lFlipHorizontalCheckBox, 6, 0);


    {
      Button lSendToDeviceButton = new Button("Send to mirror");
      Font lFont = lSendToDeviceButton.getFont();
      lFont = Font.font(lFont.getFamily(), FontWeight.BOLD, lFont.getSize());
      lSendToDeviceButton.setFont(lFont);
      lSendToDeviceButton.setMaxWidth(Double.MAX_VALUE);
      lSendToDeviceButton.setOnAction((actionEvent) -> {

        DenseMatrix64F lSourceMatrix = mEditorMatrixVariable.get().copy();
        DenseMatrix64F lTargetMatrix = mEditorMatrixVariable.get().copy();
        if (lFlipXYCheckBox.isSelected())
        {
          TransformMatrices.flipSquareMatrixXY(lSourceMatrix,
                                                     lTargetMatrix);
          lSourceMatrix = lTargetMatrix.copy();
        }
        if (lFlipVerticalCheckBox.isSelected())
        {
          TransformMatrices.flipSquareMatrixVertical(lSourceMatrix,
                                                     lTargetMatrix);
          lSourceMatrix = lTargetMatrix.copy();
        }
        if (lFlipHorizontalCheckBox.isSelected())
        {
          TransformMatrices.flipSquareMatrixHorizontal(lSourceMatrix,
                                                     lTargetMatrix);
          lSourceMatrix = lTargetMatrix.copy();
        }
        mSpatialPhaseModulatorDevice.getMatrixReference().set(lTargetMatrix);


        info("Asking to set the dm device to given values");

        boolean arrayOk = true;
        double[] data = lTargetMatrix.data;
        for (int i = 0; i< data.length; i++) {
          if (data[i] < -1 || data[i] > 1) {
            warning("Error: Matrix element " + i + " out of acceptable range! Change it and try again.");
            arrayOk = false;
          }
        }
        if (arrayOk) {
          mSpatialPhaseModulatorDevice.getMatrixReference().set(lTargetMatrix);
        }
      });
      this.add(lSendToDeviceButton, 3, 0);
      lRow++;
    }

    {
      // reset
      Button lZeroButton = new Button("Reset to zero");
      lZeroButton.setOnAction((actionEvent) -> {
        DenseMatrix64F
            lEmptyMatrix =
            new DenseMatrix64F(lMatrixReference.numRows,
                               lMatrixReference.numCols);

        mEditorMatrixVariable.set(lEmptyMatrix);
      });
      lZeroButton.setMaxWidth(Double.MAX_VALUE);
      this.add(lZeroButton, 3, lRow);
      lRow++;
    }

    ComboBox lExistingMirrorModesComboBox;
    {
      // load
      lExistingMirrorModesComboBox = new ComboBox(listExistingMirorModeFiles());
      add(lExistingMirrorModesComboBox, 2, lRow);

      Button lLoadMirrorModeBytton = new Button("Load");
      lLoadMirrorModeBytton.setMaxWidth(Double.MAX_VALUE);
      lLoadMirrorModeBytton.setOnAction((e) -> {
        try
        {
          DenseMatrix64F lMatrix = new DenseMatrix64F(lMatrixReference.numRows, lMatrixReference.numCols);
          new DenseMatrix64FReader(getFile(lExistingMirrorModesComboBox.getValue().toString()), lMatrix).read();
          mEditorMatrixVariable.set(lMatrix);
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      });

      add(lLoadMirrorModeBytton, 3, lRow);
      lRow++;

    }

    {
      // save
      Variable<String> lFileNameVariable = new Variable<String>("filename", "DM_");

      TextField lFileNameTextField =
          new TextField(lFileNameVariable.get());
      lFileNameTextField.setMaxWidth(Double.MAX_VALUE);
      lFileNameTextField.textProperty()
                                   .addListener((obs, o, n) -> {
                                     String lName = n.trim();
                                     if (!lName.isEmpty())
                                       lFileNameVariable.set(lName);
                                   });
      add(lFileNameTextField, 2, lRow);

      Button lSaveMirrorModeButton = new Button("Save");
      lSaveMirrorModeButton.setAlignment(Pos.CENTER);
      lSaveMirrorModeButton.setMaxWidth(Double.MAX_VALUE);
      lSaveMirrorModeButton.setOnAction((e) -> {
        try
        {
          new DenseMatrix64FWriter(getFile(lFileNameVariable.get()), mEditorMatrixVariable.get()).write();
          updateEditors(mEditorMatrixVariable.get());
          lExistingMirrorModesComboBox.setItems(listExistingMirorModeFiles());
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      });
      GridPane.setColumnSpan(lSaveMirrorModeButton, 1);
      add(lSaveMirrorModeButton, 3, lRow);
      lRow++;
    }
    lRow++;


    TabPane lTabPane = new TabPane();
    add(lTabPane, 0, lRow, 9, 1);

    {
      // Single zernike editor
      Tab lSingleZernikeTab = new Tab("Single Zernike mode");
      ZernikeModeEditor
          lZernikeModeEditor = new ZernikeModeEditor(mEditorMatrixVariable);
      lSingleZernikeTab.setContent(lZernikeModeEditor);
      lTabPane.getTabs().add(lSingleZernikeTab);
    }

    {
      // Linear combination editor
      Tab lZernikeLinearCombinationTab = new Tab("Linear combination of Zernike modes");
      ZernikeModesLinearCombinationEditor
          lEditor = new ZernikeModesLinearCombinationEditor(mEditorMatrixVariable, getExistingMirrorModeList(), mMirrorModeDirectory);
      lZernikeLinearCombinationTab.setContent(lEditor);
      lTabPane.getTabs().add(lZernikeLinearCombinationTab);
    }

    {
      // Zernike controller
      Tab lZernikeControllerTab = new Tab("Zernike controller");
      ZernikeVisualController
          lZernikeVisualController = new ZernikeVisualController(mEditorMatrixVariable);
      lZernikeControllerTab.setContent(lZernikeVisualController);
      lTabPane.getTabs().add(lZernikeControllerTab);
      lTabPane.getSelectionModel().select(lZernikeControllerTab);
    }

    {
      // Matrix editor
      Tab lZernikeEditorTab = new Tab("Matrix editor");
      DenseMatrixEditor lMatrixEditor = new DenseMatrixEditor(mEditorMatrixVariable);
      lZernikeEditorTab.setContent(lMatrixEditor);
      mListMatrixUpdateReceivers.add(lMatrixEditor);
      lTabPane.getTabs().add(lZernikeEditorTab);
    }



    // execute first drawing
    mEditorMatrixVariable.set(mEditorMatrixVariable.get());
  }

  private void updateVisualisation(DenseMatrix64F pNewMatrix, int pPreviewWidth, int pPreviewHeight) {
    System.out.println("refreshVisualisation");

    LookUpTable lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();

    mPreviewPane.setImage(new DenseMatrixImage(pNewMatrix, lLookUpTable));

    double lMin = TransformMatrices.getMinOfMatrix(pNewMatrix);
    double lMax = TransformMatrices.getMaxOfMatrix(pNewMatrix);

    double lFactor = 1.0 / Math.max(Math.abs(lMin), Math.abs(lMax));
    DenseMatrix64F lScaledMatrix = TransformMatrices.multiply(pNewMatrix, lFactor);
    mPreviewPaneScaled.setImage(new DenseMatrixImage(lScaledMatrix, lLookUpTable));
    mPreviewScaledLabel.setText("-" + String.format("%.2f", 1.0 / lFactor) + " ... " + String.format("%.2f", 1.0 / lFactor));

    //mGraphicsContext.drawImage(new DenseMatrixImage(pNewMatrix, lLookUpTable), 0, 0, pPreviewWidth, pPreviewHeight);
  }

  private void updateEditors(DenseMatrix64F pMatrix) {
    for (MatrixUpdateReceiver lReceiver : mListMatrixUpdateReceivers) {
      lReceiver.updateMatrix(pMatrix);
    }
  }



  private File getFile(String pName)
  {
    return new File(mMirrorModeDirectory, pName + ".json");
  }

  private ObservableList<String> listExistingMirorModeFiles()
  {
    ArrayList<String> filenames = getExistingMirrorModeList();
    ObservableList<String> list =     FXCollections.observableArrayList(filenames);
    return list;
  }

  ArrayList<String> mExistingTemplateFileList = new ArrayList<String>();
  private ArrayList<String> getExistingMirrorModeList() {
    File folder = mMirrorModeDirectory;

    mExistingTemplateFileList.clear();
    for (File file : folder.listFiles()) {
      if (!file.isDirectory() && file.getAbsolutePath().endsWith(".json")) {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 5);

        mExistingTemplateFileList.add(fileName);
      }
    }

    return mExistingTemplateFileList;
  }


}
