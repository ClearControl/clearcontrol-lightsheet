package clearcontrol.devices.stages;

import clearcontrol.core.variable.Variable;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface BasicStageInterface
{
  boolean moveBy(double pDistance, boolean pWaitToFinish);

  Variable<Double> getPositionVariable();
}
