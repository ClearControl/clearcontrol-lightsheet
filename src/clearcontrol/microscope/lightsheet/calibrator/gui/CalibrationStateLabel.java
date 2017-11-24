package clearcontrol.microscope.lightsheet.calibrator.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.gui.swing.JLabelString;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class CalibrationStateLabel extends Label
{
  private final Variable<String> mStringVariable;
  private CalibrationStateLabel mThis;

  public CalibrationStateLabel(final String pLabelName,
                      final String pInicialValue)
  {
    super(pInicialValue);
    mThis = this;

    mStringVariable = new Variable<String>(pLabelName, pInicialValue)
    {
      @Override
      public String setEventHook(final String pOldValue,
                                 final String pNewValue)
      {
        if (!pNewValue.equals(mThis.getText()))
        {
          mThis.setText(pNewValue);
        }
        return super.setEventHook(pOldValue, pNewValue);
      }
    };

  }

  public Variable<String> getStringVariable()
  {
    return mStringVariable;
  }
}
