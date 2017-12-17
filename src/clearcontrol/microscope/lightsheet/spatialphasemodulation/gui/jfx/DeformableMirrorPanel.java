package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx;

import javafx.application.Platform;

import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.var.customvarpanel.CustomVariablePane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.DeformableMirrorDevice;

import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class DeformableMirrorPanel extends CustomVariablePane
{
  DeformableMirrorDevice mDeformableMirrorDevice;

  public DeformableMirrorPanel(DeformableMirrorDevice pDeformableMirrorDevice)
  {
    super();
    addTab("");

    mDeformableMirrorDevice = pDeformableMirrorDevice;

    DenseMatrix64F lMatrixReference =
                                    mDeformableMirrorDevice.getMatrixReference()
                                                           .get();

    int count = 0;
    for (int x = 0; x < lMatrixReference.numCols; x++)
    {
      for (int y = 0; y < lMatrixReference.numRows; y++)
      {

        BoundedVariable<Double> lMatrixElementVariable =
                                                       new BoundedVariable<Double>("matrix_"
                                                                                   + x
                                                                                   + "_"
                                                                                   + y,
                                                                                   lMatrixReference.data[count]);

        final int position = count;
        lMatrixElementVariable.addSetListener(new VariableSetListener<Double>()
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
              }
            });
          }
        });

        addNumberTextFieldForVariable("", lMatrixElementVariable);
        /*
        NumberVariableTextField<Double> lField =
                                               new NumberVariableTextField<Double>(lMatrixElementVariable.getName(),
                                                                                   lMatrixElementVariable,
                                                                                   lMatrixElementVariable.getMin(),
                                                                                   lMatrixElementVariable.getMax(),
                                                                                   lMatrixElementVariable.getGranularity());
        this.add(lField.getTextField(), x, y);*/

      }
    }
  }

}
