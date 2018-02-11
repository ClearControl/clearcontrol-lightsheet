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
 *
 * This is a package private class. Don't use it, prefer ZernikePolynomialsDenseMatrix64F instead.
 *
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
@Deprecated
class ZernikePolynomialMatrix implements LoggingFeature
{
  int m;
  int n;

  double centerX;
  double centerY;

  double squaredMaximumRadius;

  int width;
  int height;


  final static double tolerance = 0.0000001;

  public ZernikePolynomialMatrix(int width, int height, int m, int n)
  {
    this.width = width;
    this.height = height;

    if (width != height)
    {
      warning("Matrix width and height should be equal");
    }
    if (n < m)
    {
      warning("n is supposed to be larger or equal to m!");
    }

    // the tolerance here helps to prevent NaN results
    centerX = (width - 1) / 2.0 + tolerance;
    centerY = (height - 1) / 2.0 + tolerance;

    this.m = m;
    this.n = n;

    squaredMaximumRadius = Math.pow(width / 2.0, 2.0);
  }

  public double get(int x, int y)
  {
    double
        radialDistanceSquared =
        (Math.pow((double) x - centerX, 2) + Math.pow((double) y
                                                      - centerY, 2))
        / squaredMaximumRadius;

    double t = 0;
    t = Math.atan((y - centerY) / (x - centerX));

    if ((x - centerX) >= 0)
    {
      t = Math.PI + t;
    }

    if (radialDistanceSquared <= 1)
    {
      double
          pol =
          ZernikePolynomials.computeZnmr2t(n,
                                           m,
                                           radialDistanceSquared,
                                           t);
      return pol;
    }
    return 0;
  }

  public double[][] getFullMatrix()
  {
    double[][] result = new double[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        result[x][y] = get(x, y);
      }
    }
    return result;
  }

  public int getM() {
    return m;
  }
  public int getN() {
    return n;
  }
}
