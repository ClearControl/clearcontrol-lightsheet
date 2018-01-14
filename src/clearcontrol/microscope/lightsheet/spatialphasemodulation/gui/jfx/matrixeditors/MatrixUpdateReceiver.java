package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface MatrixUpdateReceiver
{
  void updateMatrix(DenseMatrix64F pMatrix);
}
