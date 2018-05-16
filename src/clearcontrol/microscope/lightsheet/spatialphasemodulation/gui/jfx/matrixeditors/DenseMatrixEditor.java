package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.ejml.data.DenseMatrix64F;

/**
 * This editor class shows an array of text fields allowing the user
 * to edit a matrix manually.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
@Deprecated
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

    int lRow = mMatrixReference.numRows + 1;


    Button lFlipHorizontalButton =
        new Button("Flip horizontal");
    lFlipHorizontalButton.setOnAction((actionEvent) -> {
      DenseMatrix64F lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.flipSquareMatrixHorizontal(lMatrixCopy, mMatrixReference);
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lFlipHorizontalButton, 2);
    lFlipHorizontalButton.setMaxWidth(Double.MAX_VALUE);
    this.add(lFlipHorizontalButton, 0, lRow);


    Button lFlipVerticalButton =
        new Button("Flip vertical");
    lFlipVerticalButton.setOnAction((actionEvent) -> {
      DenseMatrix64F lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.flipSquareMatrixVertical(lMatrixCopy, mMatrixReference);
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lFlipVerticalButton, 2);
    lFlipVerticalButton.setMaxWidth(Double.MAX_VALUE);
    this.add(lFlipVerticalButton, 2, lRow);


    Button lFlipXYButton =
        new Button("Flip XY");
    lFlipXYButton.setOnAction((actionEvent) -> {
      DenseMatrix64F lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.flipSquareMatrixXY(lMatrixCopy, mMatrixReference);
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lFlipXYButton, 3);
    lFlipXYButton.setMaxWidth(Double.MAX_VALUE);
    this.add(lFlipXYButton, 4, lRow);


    Button lRotateClockwise =
        new Button("Rotate clockwise");
    lRotateClockwise.setOnAction((actionEvent) -> {
      DenseMatrix64F lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.rotateClockwise(lMatrixCopy, mMatrixReference);
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lRotateClockwise, 2);
    lRotateClockwise.setMaxWidth(Double.MAX_VALUE);
    this.add(lRotateClockwise, 7, lRow);


    Button lRotateCounterClockwiseButton =
        new Button("Rotate counter clockwise");
    lRotateCounterClockwiseButton.setOnAction((actionEvent) -> {
      DenseMatrix64F lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.flipSquareMatrixXY(lMatrixCopy, mMatrixReference);
      lMatrixCopy = mMatrixReference.copy();
      TransformMatrices.flipSquareMatrixHorizontal(lMatrixCopy, mMatrixReference);
      mMatrixVariable.set(mMatrixReference);
    });
    GridPane.setColumnSpan(lRotateCounterClockwiseButton, 2);
    lRotateCounterClockwiseButton.setMaxWidth(Double.MAX_VALUE);
    this.add(lRotateCounterClockwiseButton, 9, lRow);

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

    Button lZernikeMomentsButton =
        new Button("Mutliply");
    lZernikeMomentsButton.setOnAction((actionEvent) -> {
      mMatrixVariable.set(TransformMatrices.multiply(mMatrixReference, lFactorVariable.get()));
    });
    GridPane.setColumnSpan(lZernikeMomentsButton, 2);
    this.add(lZernikeMomentsButton, 2, lRow);
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
