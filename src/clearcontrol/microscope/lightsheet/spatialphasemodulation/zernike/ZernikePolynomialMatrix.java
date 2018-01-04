package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import clearcontrol.core.log.LoggingFeature;

/**
 * This class represents a Zernike Z^m_n mode matrix of size width*height
 * <p>
 * Usage:
 * <p>
 * mat = new ZernikePolynomialMatrix(width, height, m, n)
 * <p>
 * value = mat.get(x, y);
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class ZernikePolynomialMatrix implements LoggingFeature
{
  int m;
  int n;

  double centerX;
  double centerY;

  double squaredMaximumRadius;

  public ZernikePolynomialMatrix(int width, int height, int m, int n)
  {
    if (width != height)
    {
      warning("Matrix width and height should be equal");
    }
    if (n < m)
    {
      warning("n is supposed to be larger or equal to m!");
    }

    centerX = (width - 1) / 2.0;
    centerY = (height - 1) / 2.0;

    this.m = m;
    this.n = n;

    squaredMaximumRadius = Math.pow(width / 2.0, 2.0);
  }

  public double get(int x, int y)
  {
    double radialDistanceSquared =
                                 (Math.pow((double) x - centerX, 2)
                                  + Math.pow((double) y - centerY, 2))
                                   / squaredMaximumRadius;

    double t = 0;
    t = Math.atan((y - centerY) / (x - centerX));

    if ((x - centerX) >= 0)
    {
      t = Math.PI + t;
    }

    if (radialDistanceSquared <= 1)
    {
      double pol = ZernikePolynomials.computeZnmr2t(n,
                                                    m,
                                                    radialDistanceSquared,
                                                    t);
      return pol;
    }
    return 0;
  }
}
