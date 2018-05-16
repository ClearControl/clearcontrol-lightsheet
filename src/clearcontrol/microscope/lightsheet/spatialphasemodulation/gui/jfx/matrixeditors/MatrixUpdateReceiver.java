package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors;

import org.ejml.data.DenseMatrix64F;

/**
 * This interface should be implemented by all matrix editors which allow
 * reading/visualising a matrix.
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
@Deprecated
public interface MatrixUpdateReceiver
{
  void updateMatrix(DenseMatrix64F pMatrix);
}
