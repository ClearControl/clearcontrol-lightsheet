package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class ZernikePolynomialsDenseMatrix64F extends DenseMatrix64F
{
  public ZernikePolynomialsDenseMatrix64F(int width, int height, int m, int n) {
    super(new ZernikePolynomialMatrix(width, height, m, n).getFullMatrix());
  }
}
