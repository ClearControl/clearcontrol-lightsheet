package clearcontrol.microscope.lightsheet.configurationstate.gui;

import clearcontrol.core.variable.Variable;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class ConfigurationStateLabel extends Label
{
  private final Variable<String> mStringVariable;
  private ConfigurationStateLabel mThis;

  public ConfigurationStateLabel(final String pLabelName,
                                 final String pInitialValue)
  {
    super(pInitialValue);
    mThis = this;

    mStringVariable = new Variable<String>(pLabelName, pInitialValue)
    {
      @Override
      public String setEventHook(final String pOldValue,
                                 final String pNewValue)
      {
        if (!pNewValue.equals(mThis.getText()))
        {
          Platform.runLater(new Runnable()
            {
              @Override public void run()
              {
                mThis.setText(pNewValue);
              }
            });
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
