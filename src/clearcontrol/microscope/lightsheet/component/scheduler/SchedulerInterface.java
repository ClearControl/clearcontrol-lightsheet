package clearcontrol.microscope.lightsheet.component.scheduler;

import clearcontrol.core.device.name.NameableInterface;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.MicroscopeInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface SchedulerInterface extends NameableInterface
{
  // todo: define an API so that the scheduler can tell its owner how often it would like to be called


  boolean doExperiment(long pTimePoint);

  Variable<Boolean> getActiveVariable();

  public void setMicroscope(MicroscopeInterface pMicroscope);
}
