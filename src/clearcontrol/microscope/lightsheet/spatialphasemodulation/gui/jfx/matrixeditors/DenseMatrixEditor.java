package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialMatrix;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrixEditor extends GridPane implements
                                                LoggingFeature,
                                                MatrixUpdateReceiver
{
  private DenseMatrix64F mMatrixReference;
  private Variable<DenseMatrix64F> mMatrixVariable;

  public DenseMatrixEditor(Variable<DenseMatrix64F> lMatrixVariable)
  {
    mMatrixReference = lMatrixVariable.get();
    mMatrixVariable = lMatrixVariable;
    setup();
  }


  BoundedVariable<Double>[][] lVariableMatrix;

  private void setup()
  {

    lVariableMatrix =
        new BoundedVariable[mMatrixReference.numCols][mMatrixReference.numRows];

    int count = 0;
    for (int x = 0; x < mMatrixReference.numCols; x++)
    {
      for (int y = 0; y < mMatrixReference.numRows; y++)
      {

        lVariableMatrix[x][y] =
            new BoundedVariable<Double>("matrix_"
                                        + x
                                        + "_"
                                        + y,
                                        mMatrixReference.data[count]);

        final int position = count;
        lVariableMatrix[x][y].addSetListener(new VariableSetListener<Double>()
        {
          @Override
          public void setEvent(Double pCurrentValue, Double pNewValue)
          {
            Platform.runLater(new Runnable()
            {
              @Override
              public void run()
              {
                mMatrixReference.data[position] = pNewValue;
                info("Setting " + position + " = " + pNewValue);

                sendUpdates();

              }
            });
          }
        });

        NumberVariableTextField<Double> lField =
            new NumberVariableTextField<Double>(lVariableMatrix[x][y].getName(),
                                                lVariableMatrix[x][y],
                                                lVariableMatrix[x][y].getMin(),
                                                lVariableMatrix[x][y].getMax(),
                                                lVariableMatrix[x][y].getGranularity());
        this.add(lField.getTextField(), x, y);


        count++;
      }
    }
  }

  private boolean sendingUpdates = false;
  private boolean receivingUpdates = false;
  private void sendUpdates() {
    if (sendingUpdates || receivingUpdates) {
      return;
    }
    sendingUpdates = true;
    mMatrixVariable.set(mMatrixReference);
    sendingUpdates = false;
  }

  @Override
  public void updateMatrix(DenseMatrix64F pMatrix) {
    if (sendingUpdates || receivingUpdates) {
      return;
    }
    receivingUpdates = true;
    for (int x = 0; x < mMatrixReference.numCols; x++)
    {
      for (int y = 0; y < mMatrixReference.numRows; y++)
      {
        double lZernikeValue = pMatrix.get(x, y);
        lVariableMatrix[x][y].set(lZernikeValue);
      }
    }
    receivingUpdates = false;
  }
}
