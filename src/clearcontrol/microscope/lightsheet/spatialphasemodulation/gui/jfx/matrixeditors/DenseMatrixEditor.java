package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import org.ejml.data.DenseMatrix64F;

/**
 * This editor class shows an array of text fields allowing the user
 * to edit a matrix manually.
 *
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

  private boolean mSendingUpdatesMutex = false;
  private boolean mReceivingUpdatesMutex = false;
  private void sendUpdates() {
    if (mSendingUpdatesMutex || mReceivingUpdatesMutex) {
      return;
    }
    mSendingUpdatesMutex = true;
    mMatrixVariable.set(mMatrixReference);
    mSendingUpdatesMutex = false;
  }

  @Override
  public void updateMatrix(DenseMatrix64F pMatrix) {
    if (mSendingUpdatesMutex || mReceivingUpdatesMutex) {
      return;
    }
    mReceivingUpdatesMutex = true;
    for (int x = 0; x < mMatrixReference.numCols; x++)
    {
      for (int y = 0; y < mMatrixReference.numRows; y++)
      {
        double lZernikeValue = pMatrix.get(x, y);
        lVariableMatrix[x][y].set(lZernikeValue);
      }
    }
    mReceivingUpdatesMutex = false;
  }
}
