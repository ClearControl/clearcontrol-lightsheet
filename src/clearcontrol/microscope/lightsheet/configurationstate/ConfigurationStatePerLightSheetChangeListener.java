package clearcontrol.microscope.lightsheet.configurationstate;

import java.util.EventListener;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public abstract class ConfigurationStatePerLightSheetChangeListener extends ConfigurationStateChangeListener implements
                                                          EventListener
{
  public abstract void configurationStateOfLightSheetChanged(HasConfigurationStatePerLightSheet pHasConfigurationStatePerLightSheet, int pLightSheetIndex);
}
