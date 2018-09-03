package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import org.ejml.data.DenseMatrix64F;

/**
 * The ZernikePolynomialsDenseMatrix64F represents a matrix containing a given
 * Zernike mode
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class ZernikePolynomialsDenseMatrix64F extends DenseMatrix64F
{
  private int m;
  private int n;

  public ZernikePolynomialsDenseMatrix64F(int width,
                                          int height,
                                          int m,
                                          int n)
  {
    super(new ZernikePolynomialMatrix(width,
                                      height,
                                      m,
                                      n).getFullMatrix());
    this.m = m;
    this.n = n;
  }

  public int getM()
  {
    return m;
  }

  public int getN()
  {
    return n;
  }
}
