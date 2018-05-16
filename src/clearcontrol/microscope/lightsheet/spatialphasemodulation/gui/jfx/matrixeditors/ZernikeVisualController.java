package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.ImagePane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
@Deprecated
public class ZernikeVisualController extends GridPane implements
                                                  LoggingFeature
{
  public ZernikeVisualController(Variable<DenseMatrix64F> lMatrixVariable) {
    int lRow = 0;

    BoundedVariable<Double> lFactorVariable =
        new BoundedVariable<Double>("factor",
                                    0.1,
                                    Double.MIN_VALUE,
                                    Double.MAX_VALUE,
                                    0.0001);

    NumberVariableTextField<Double> lFactorField =
        new NumberVariableTextField<Double>(lFactorVariable.getName(),
                                            lFactorVariable,
                                            lFactorVariable.getMin(),
                                            lFactorVariable.getMax(),
                                            lFactorVariable.getGranularity());

    this.add(lFactorField.getLabel(), 0, lRow);
    this.add(lFactorField.getTextField(), 1, lRow, 3, 1);
    lRow++;

    int lMaxM = 6;

    for (int n = 0; n <= lMaxM; n++) {
      for (int m = -n; m <= n; m += 2) {
        {
          DenseMatrix64F
              lZernikeMatrix = TransformMatrices.multiply(
              new ZernikePolynomialsDenseMatrix64F(50, 50, m, n), -1);
          DenseMatrixImage
              lMatrixImage =
              new DenseMatrixImage(lZernikeMatrix, new BlueCyanGreenYellowOrangeRedLUT());
          Button
              lButton =
              new Button("Z(" + m + "/" + n + ")",
                         new ImagePane(50, 50, lMatrixImage));

          final int lM = m;
          final int lN = n;

          lButton.setOnAction((actionEvent) -> {
            DenseMatrix64F
                lSmallZernikeMatrix = TransformMatrices.multiply(
                new ZernikePolynomialsDenseMatrix64F(lMatrixVariable.get().numCols, lMatrixVariable.get().numRows, lM, lN), -1);

                                lMatrixVariable.set(TransformMatrices.sum(lMatrixVariable.get(),
                                                                          TransformMatrices
                                                                              .multiply(
                                                                                  lSmallZernikeMatrix,
                                                                                  lFactorVariable
                                                                                      .get())));
                              }
          );

          lButton.setMinHeight(80);
          lButton.setContentDisplay(ContentDisplay.TOP);
          add(lButton, lMaxM + m, n);
        }

        {
          DenseMatrix64F
              lZernikeMatrix =
              new ZernikePolynomialsDenseMatrix64F(50, 50, m, n);
          DenseMatrixImage
              lMatrixImage =
              new DenseMatrixImage(lZernikeMatrix, new BlueCyanGreenYellowOrangeRedLUT());
          Button
              lButton =
              new Button("Z(" + m + "/" + n + ")",
                         new ImagePane(50, 50, lMatrixImage));
          final int lM = m;
          final int lN = n;

          lButton.setOnAction((actionEvent) -> {
                                DenseMatrix64F
                                    lSmallZernikeMatrix =
                                    new ZernikePolynomialsDenseMatrix64F(lMatrixVariable.get().numCols, lMatrixVariable.get().numRows, lM, lN);

                                lMatrixVariable.set(TransformMatrices.sum(lMatrixVariable.get(),
                                                                          TransformMatrices
                                                                              .multiply(
                                                                                  lSmallZernikeMatrix,
                                                                                  lFactorVariable
                                                                                      .get())));
                              }
          );

          lButton.setMinHeight(80);
          lButton.setContentDisplay(ContentDisplay.TOP);
          add(lButton, lMaxM + m + 1, n);
        }




      }
    }
    //






  }

}
