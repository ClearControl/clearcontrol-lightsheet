package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx;

import static clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices.computeZernickeTransformMatrix;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.alpao.AlpaoDMDevice;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class AlpaoDMPanel extends CustomGridPane
                          implements LoggingFeature
{
  AlpaoDMDevice mSpatialPhaseModulatorDevice;

  public AlpaoDMPanel(AlpaoDMDevice pAbstractDeformableMirrorDevice)
  {
    super();

    mSpatialPhaseModulatorDevice = pAbstractDeformableMirrorDevice;

    DenseMatrix64F lMatrixReference =
                                    mSpatialPhaseModulatorDevice.getMatrixReference()
                                                                .get();
    System.out.println("Matrix GET to " + lMatrixReference);

    BoundedVariable<Double>[][] lVariableMatrix =
                                                new BoundedVariable[lMatrixReference.numCols][lMatrixReference.numRows];

    int count = 0;
    for (int x = 0; x < lMatrixReference.numCols; x++)
    {
      for (int y = 0; y < lMatrixReference.numRows; y++)
      {

        lVariableMatrix[x][y] =
                              new BoundedVariable<Double>("matrix_"
                                                          + x
                                                          + "_"
                                                          + y,
                                                          lMatrixReference.data[count]);

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
                lMatrixReference.data[position] = pNewValue;
                info("Setting " + position + " = " + pNewValue);
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

      }
    }

    Button lExecuteButton = new Button("Zero");
    lExecuteButton.setOnAction((actionEvent) -> {

      DenseMatrix64F lMatrix =
                             mSpatialPhaseModulatorDevice.getMatrixReference()
                                                         .get();
      mSpatialPhaseModulatorDevice.getMatrixReference().set(lMatrix);

    });
    this.add(lExecuteButton, lMatrixReference.numCols, 0);

    Button lZeroButton = new Button("Reset");
    lZeroButton.setOnAction((actionEvent) -> {
      mSpatialPhaseModulatorDevice.zero();
    });
    this.add(lZeroButton, lMatrixReference.numCols, 1);

    int lRow = lMatrixReference.numRows + 1;

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

    DenseMatrix64F lFullMatrix =
                               computeZernickeTransformMatrix(lMatrixReference.numRows);
    lFullMatrix.print();

    Button lZernikeMomentsButton =
                                 new Button("Calculate Zernike moments");
    lZernikeMomentsButton.setOnAction((actionEvent) -> {
      final DenseMatrix64F lInputVector =
                                        new DenseMatrix64F(lFullMatrix.numRows,
                                                           1);

      final DenseMatrix64F lShapeVector =
                                        new DenseMatrix64F(lFullMatrix.numRows,
                                                           1);

      lInputVector.set(lMVariable.get()
                       + lMatrixReference.numCols * lNVariable.get(),
                       1);

      CommonOps.mult(lFullMatrix, lInputVector, lShapeVector);
      for (int x = 0; x < lMatrixReference.numCols; x++)
      {
        for (int y = 0; y < lMatrixReference.numRows; y++)
        {
          double lZernikeValue = lFullMatrix.get(
                                                 y
                                                 * lMatrixReference.numCols
                                                 + x);
          // ZernikePolynomials.computeZnmxy(lNVariable.get(), lMVariable.get(),
          // x - centerX, y - centerY);
          lVariableMatrix[x][y].set(lZernikeValue);
        }
      }
    });
    GridPane.setColumnSpan(lZernikeMomentsButton, 2);
    this.add(lZernikeMomentsButton, 0, lRow);

    lRow++;

  }

}
