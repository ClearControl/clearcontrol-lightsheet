package clearcontrol.microscope.lightsheet.configurationstate;

import java.util.EventListener;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public abstract class ConfigurationStateChangeListener implements
                                                     EventListener
{
  public abstract void configurationStateChanged(HasConfigurationState pHasConfigurationState);
}
