package clearcontrol.deformablemirrors.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.VariableSetListener;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.deformablemirrors.DeformableMirrorDevice;
import clearcontrol.devices.slm.slms.devices.alpao.AlpaoDMDevice;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.application.Platform;
import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
public class DeformableMirrorPanel extends CustomGridPane
{
  DeformableMirrorDevice mDeformableMirrorDevice;

  public DeformableMirrorPanel(DeformableMirrorDevice pDeformableMirrorDevice) {
    super();

    mDeformableMirrorDevice = pDeformableMirrorDevice;

    AlpaoDMDevice lAlpaoDMDevice = mDeformableMirrorDevice.getAlpaoDMDevice();
    DenseMatrix64F
        lMatrixReference =
        lAlpaoDMDevice.getMatrixReference().get();

    int count = 0;
    for (int x = 0; x < lMatrixReference.numCols; x++) {
      for (int y = 0; y < lMatrixReference.numRows; y++) {

        BoundedVariable<Double>
            lMatrixElementVariable = new BoundedVariable<Double>("matrix_" + x + "_" + y,
                                                                 lMatrixReference.data[count]);

        final int position = count;
        lMatrixElementVariable.addSetListener(new VariableSetListener<Double>()
        {
          @Override public void setEvent(Double pCurrentValue,
                                         Double pNewValue)
          {
            Platform.runLater(new Runnable()
            {
              @Override public void run()
              {
                lMatrixReference.data[position] = pNewValue;
              }
            });
          }
        });

        NumberVariableTextField<Double>
            lField =
            new NumberVariableTextField<Double>(lMatrixElementVariable.getName(),
                                                lMatrixElementVariable,
                                                lMatrixElementVariable.getMin(),
                                                lMatrixElementVariable.getMax(),
                                                lMatrixElementVariable.getGranularity());
        this.add(lField.getTextField(), x, y);
      }
    }
  }



}
