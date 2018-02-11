package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.SimpleZernikeDecomposer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.ejml.data.DenseMatrix64F;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This editor allows the user to enter a linear combination of
 * Zernike modes. Also saved templates of Zernike modes are allowed.
 *
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
        // Decompose the matrix and ask the user if the result should
        // be kept.
        String lCompositionCode = new SimpleZernikeDecomposer(pMatrixVariable.get()).getCompositionCode();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Experimental decomposer result");
        alert.setHeaderText("The experimental decomposer found the following solution.\nDo you want to accept it?");
        alert.setContentText(lCompositionCode);

        LookUpTable lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();
        DenseMatrix64F lMatrix = drawMatrixFromText(lCompositionCode);

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
        "# Z1 (0/ 0) 0.5\n" +
        "# -2 2 0.49\n" +
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

  /**
   * This function transforms a given text in the following format
   * to a Matrix containing the described Zernike modes.
   *
   * Examples:
   *
   * Z[0, 0] 0.33
   * -2 2 0.33
   * TemplateFile 0.33
   *
   * todo: Create a reusable class in which can to that. It should
   * have hight code quality.
   *
   * @param pText
   * @return
   */
  private DenseMatrix64F drawMatrixFromText(String pText) {
    ArrayList<DenseMatrix64F> lMatrixList = new ArrayList<>();

    int lLineCount = 0;

    try
    {
      String[] lRows = pText.split("\n");

      for (String lCurrentRow : lRows)
      {
        lCurrentRow = lCurrentRow.trim();

        lLineCount ++;
        if (lCurrentRow.trim().startsWith("//") || lCurrentRow.trim().startsWith("#")) {
          continue;
        }
        if (lCurrentRow.startsWith("Z") && lCurrentRow.contains("[") && lCurrentRow.contains(
            "]"))
        {
          String[] temp = lCurrentRow.split("\\]");
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

        if (lCurrentRow.startsWith("Z") && lCurrentRow.contains("(") && lCurrentRow.contains(
            ")"))
        {

          String[] temp = lCurrentRow.split("\\)");
          double factor = Double.valueOf(temp[1].trim());

          temp = temp[0].split("\\(" );
          temp = temp[1].split("/" );

          double m = Double.valueOf(temp[0]);
          double n = Double.valueOf(temp[1]);

          System.out.println("m " + m);
          System.out.println("n " + n);
          System.out.println("f " + factor);

          lMatrixList.add(createZermikeModeMatrix((int)m, (int)n, factor));
          continue;
        }

        // deal with white spaces
        String lRowWithSpaces = lCurrentRow;
        lRowWithSpaces = lRowWithSpaces.replace("\t", " ");

        while (lRowWithSpaces.contains("  ")){
          lRowWithSpaces = lRowWithSpaces.replace("  ", " ");
        }

        String[] lColumns = lRowWithSpaces.split(" ");
        if (lColumns.length == 3) {
          double m = Double.valueOf(lColumns[0]);
          double n = Double.valueOf(lColumns[1]);
          double factor = Double.valueOf(lColumns[2]);
          lMatrixList.add(createZermikeModeMatrix((int)m, (int)n, factor));
          continue;
        }

        if (lColumns.length == 2) {
          double lFactor = Double.valueOf(lColumns[1]);

          String lSearchedFilename = lColumns[0];
          for (String lExistingMirrorTemplateFilename : mExistingMatrixTemplates) {
            if (lSearchedFilename.equals(lExistingMirrorTemplateFilename)) {
              DenseMatrix64F lReferenceMatrix = mMatrixVariable.get();
              DenseMatrix64F lMatrix = new DenseMatrix64F(lReferenceMatrix.numCols, lReferenceMatrix.numRows);

              File lTemplateFile = new File(mMatrixTemplateFolder, lExistingMirrorTemplateFilename + ".json");

              if (new DenseMatrix64FReader(lTemplateFile, lMatrix).read())
              {
                lMatrixList.add(TransformMatrices.multiply(lMatrix, lFactor));
                continue;
              }
            }
          }
        }

      }
    } catch (Exception e) {
      info("Erorr parsing line " + lLineCount + ": " + e.getMessage());
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
