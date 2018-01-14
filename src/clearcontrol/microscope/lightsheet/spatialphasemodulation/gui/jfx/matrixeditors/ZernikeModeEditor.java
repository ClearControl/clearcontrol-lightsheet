package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialMatrix;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class ZernikeModeEditor extends GridPane implements
                                                       LoggingFeature
{
  private Variable<DenseMatrix64F> mMatrixVariable;
  private DenseMatrix64F mMatrixReference;


  public ZernikeModeEditor(Variable<DenseMatrix64F> lMatrixVariable)
  {
    mMatrixVariable = lMatrixVariable;
    mMatrixReference = lMatrixVariable.get();

    int lRow = 0;

    BoundedVariable<Integer> lMVariable =
        new BoundedVariable<Integer>("m",
                                     0,
                                     Integer.MIN_VALUE,
                                     Integer.MAX_VALUE,
                                     1);

    NumberVariableTextField<Integer> lMField =
        new NumberVariableTextField<Integer>(lMVariable.getName(),
                                             lMVariable,
                                             lMVariable.getMin(),
                                             lMVariable.getMax(),
                                             lMVariable.getGranularity());

    this.add(lMField.getLabel(), 0, lRow);
    this.add(lMField.getTextField(), 1, lRow);
    lRow++;

    BoundedVariable<Integer> lNVariable =
        new BoundedVariable<Integer>("n",
                                     0,
                                     Integer.MIN_VALUE,
                                     Integer.MAX_VALUE,
                                     1);

    NumberVariableTextField<Integer> lNField =
        new NumberVariableTextField<Integer>(lNVariable.getName(),
                                             lNVariable,
                                             lNVariable.getMin(),
                                             lNVariable.getMax(),
                                             lNVariable.getGranularity());
    this.add(lNField.getLabel(), 0, lRow);
    this.add(lNField.getTextField(), 1, lRow);
    lRow++;

    BoundedVariable<Double> lFactorVariable =
        new BoundedVariable<Double>("factor",
                                     1.0,
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
    this.add(lFactorField.getTextField(), 1, lRow);
    lRow++;



    Button lZernikeMomentsButton =
        new Button("Calculate Zernike moments");
    lZernikeMomentsButton.setOnAction((actionEvent) -> {


      ZernikePolynomialMatrix lZernikePolynomialMatrix =
          new ZernikePolynomialMatrix(mMatrixReference.numCols,
                                      mMatrixReference.numRows,
                                      lMVariable.get(),
                                      lNVariable.get());

      for (int x = 0; x < mMatrixReference.numCols; x++)
      {
        for (int y = 0; y < mMatrixReference.numRows; y++)
        {
          mMatrixReference.set(x,y, lZernikePolynomialMatrix.get(x, y));
        }
      }
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lZernikeMomentsButton, 2);
    this.add(lZernikeMomentsButton, 0, lRow);

    lRow++;


  }
}