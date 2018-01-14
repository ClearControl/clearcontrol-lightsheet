package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.DeformableMirrorPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.SimpleZernikeDecomposer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialMatrix;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.ejml.data.DenseMatrix64F;
import org.python.antlr.ast.Str;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class ZernikeModesLinearCombinationEditor extends GridPane implements
                                                               LoggingFeature

{
  Variable<DenseMatrix64F> mMatrixVariable;
  ArrayList<String> mExistingMatrixTemplates;
  File mMatrixTemplateFolder;


  public ZernikeModesLinearCombinationEditor(Variable<DenseMatrix64F> pMatrixVariable, ArrayList<String> pExistingMatrixTemplates, File pMatrixTemplateFolder)
  {
    mMatrixVariable = pMatrixVariable;
    mExistingMatrixTemplates = pExistingMatrixTemplates;
    mMatrixTemplateFolder = pMatrixTemplateFolder;

    TextArea lTextArea = new TextArea();


    int lRow = 0;
    {

      Button lZernikeMomentsButton =
          new Button("Decompose Zernike moments (experimental)");
      lZernikeMomentsButton.setOnAction((actionEvent) -> {
        String lCompositionCode = new SimpleZernikeDecomposer(pMatrixVariable.get()).getCompositionCode();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Experimental decomposer result");
        alert.setHeaderText("The experimental decomposer found the following solution.\nDo you want to accept it?");
        alert.setContentText(lCompositionCode);

        LookUpTable lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();
        DenseMatrix64F lMatrix = drawMatrixFromText(lCompositionCode);
        //DenseMatrixImage lImage = new DenseMatrixImage(lMatrix, lLookUpTable);

        int lWidth = 100;
        int lHeight = 100;

        Canvas lCanvas = new Canvas( lWidth, lHeight);
        GraphicsContext lGraphicsContext = lCanvas.getGraphicsContext2D();
        lGraphicsContext.drawImage(new DenseMatrixImage(lMatrix, lLookUpTable), 0, 0, lWidth, lHeight);

        alert.setGraphic(lCanvas);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
          lTextArea.setText(lCompositionCode);
        }
      });
      this.add(lZernikeMomentsButton, 1, lRow);

    }

    {
      lTextArea.setFont(Font.font("Courier New"));
      lTextArea.setText(helpText());
      lTextArea.setOnKeyReleased(new EventHandler<KeyEvent>()
      {
        @Override public void handle(KeyEvent event)
        {
          update(lTextArea.getText());
        }
      });
      lTextArea.setMinWidth(300);
      lTextArea.setMinHeight(300);
      add(lTextArea, 0, lRow);
      lRow++;
    }
  }

  private String helpText() {
    return
        "# Useage:\n" +
        "# ZernikeMode factor\n" +
        "#\n" +
        "# Examples:\n" +
        "# Z[0, 0] 0.5\n"+
        "# -2 2 0.49\n"+
        "# templateFile 0.01\n";
  }

  private void update(String text)
  {
    DenseMatrix64F lMatrix = drawMatrixFromText(text);
    if (lMatrix != null)
    {
      mMatrixVariable.set(lMatrix);
    }
  }

  private DenseMatrix64F drawMatrixFromText(String text) {
    ArrayList<DenseMatrix64F> lMatrixList = new ArrayList<>();

    int lineCount = 0;

    try
    {
      String[] rows = text.split("\n");

      for (String row : rows)
      {
        row = row.trim();

        lineCount ++;
        if (row.trim().startsWith("//") || row.trim().startsWith("#")) {
          continue;
        }
        if (row.startsWith("Z") && row.contains("[") && row.contains(
            "]"))
        {
          String[] temp = row.split("\\]");
          double factor = Double.valueOf(temp[1].trim());

          temp = temp[0].split("\\[" );
          temp = temp[1].split("," );

          double m = Double.valueOf(temp[0]);
          double n = Double.valueOf(temp[1]);

          System.out.println("m " + m);
          System.out.println("n " + n);
          System.out.println("f " + factor);

          lMatrixList.add(createZermikeModeMatrix((int)m, (int)n, factor));
          continue;
        }

        String spacedRow = row;
        spacedRow = spacedRow.replace("\t", " ");

        while (spacedRow.contains("  ")){
          spacedRow = spacedRow.replace("  ", " ");
        }

        String[] splitRow = spacedRow.split(" ");
        if (splitRow.length == 3) {
          double m = Double.valueOf(splitRow[0]);
          double n = Double.valueOf(splitRow[1]);
          double factor = Double.valueOf(splitRow[2]);
          lMatrixList.add(createZermikeModeMatrix((int)m, (int)n, factor));
          continue;
        }

        if (splitRow.length == 2) {
          double factor = Double.valueOf(splitRow[1]);

          String searchedFilename = splitRow[0];
          for (String filename : mExistingMatrixTemplates) {
            if (searchedFilename.equals(filename)) {
              DenseMatrix64F lReferenceMatrix = mMatrixVariable.get();
              DenseMatrix64F lMatrix = new DenseMatrix64F(lReferenceMatrix.numCols, lReferenceMatrix.numRows);

              File lTemplateFile = new File(mMatrixTemplateFolder, filename + ".json");

              if (new DenseMatrix64FReader(lTemplateFile, lMatrix).read())
              {
                lMatrixList.add(lMatrix);
                continue;
              }
            }
          }
        }

      }
    } catch (Exception e) {
      info("Erorr parsing line " + lineCount + ": " + e.getMessage());
      return null;
    }

    if (lMatrixList.size() > 0)
    {
      DenseMatrix64F lSumMatrix = TransformMatrices.sum(lMatrixList);
      return lSumMatrix;
    }
    return null;
  }

  private DenseMatrix64F createZermikeModeMatrix(int m,
                                       int n,
                                       double factor)
  {
    DenseMatrix64F lReferenceMatrix = mMatrixVariable.get();
    DenseMatrix64F lMatrix = new ZernikePolynomialsDenseMatrix64F(lReferenceMatrix.numCols, lReferenceMatrix.numRows, m, n);
    for (int i = 0; i < lMatrix.getNumElements(); i++)
    {
      lMatrix.times(i, factor);
    }
    return lMatrix;
  }


}
