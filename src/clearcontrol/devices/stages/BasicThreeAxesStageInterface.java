package clearcontrol.devices.stages;

import clearcontrol.core.variable.Variable;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface BasicThreeAxesStageInterface
{
  boolean moveXBy(double pDistance, boolean pWaitToFinish );
  boolean moveYBy(double pDistance, boolean pWaitToFinish );
  boolean moveZBy(double pDistance, boolean pWaitToFinish );

  Variable<Double> getXPositionVariable();
  Variable<Double> getYPositionVariable();
  Variable<Double> getZPositionVariable();
}
