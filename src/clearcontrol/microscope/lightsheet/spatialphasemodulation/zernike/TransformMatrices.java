package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;
import java.util.List;

public class TransformMatrices
{
  public static DenseMatrix64F computeZernickeTransformMatrix(int pSquareImageWidthHeight)
  {
    int lZernikeVector = pSquareImageWidthHeight
                         * pSquareImageWidthHeight;
    DenseMatrix64F lDenseMatrix64F =
                                   new DenseMatrix64F(lZernikeVector,
                                                      lZernikeVector);

    for (int j = 0; j < pSquareImageWidthHeight; j++)
      for (int i = 0; i < pSquareImageWidthHeight; i++)
        for (int v = 0; v < pSquareImageWidthHeight; v++)
          for (int u = 0; u < pSquareImageWidthHeight; u++)
          {
            final double x = (2.0 / pSquareImageWidthHeight)
                             * (u - (pSquareImageWidthHeight - 1.0)
                                    / 2);
            final double y = (2.0 / pSquareImageWidthHeight)
                             * (v - (pSquareImageWidthHeight - 1.0)
                                    / 2);

            double lZernikeValue = ZernikePolynomials.computeZijxy(i,
                                                                   j,
                                                                   x,
                                                                   y);

            // normalization:
            /*lZernikeValue /= sqrt(((j - i) == 0 ? 2 : 1) * PI
            																/ (2 * (i + j) + 2));/**/

            // if (i == 0 && j == 0)
            // lZernikeValue = 1.0 / pSquareImageWidthHeight;

            setMatrixToMatrixLinearMap(lDenseMatrix64F,
                                       pSquareImageWidthHeight,
                                       i,
                                       j,
                                       u,
                                       v,
                                       lZernikeValue);
          }

    return lDenseMatrix64F;
  }

  public static DenseMatrix64F computeCosineTransformMatrix(int pSquareImageWidthHeight)
  {
    int lZernikeVector = pSquareImageWidthHeight
                         * pSquareImageWidthHeight;
    DenseMatrix64F lDenseMatrix64F =
                                   new DenseMatrix64F(lZernikeVector,
                                                      lZernikeVector);

    double lPiFactor = Math.PI / pSquareImageWidthHeight;

    for (int j = 0; j < pSquareImageWidthHeight; j++)
      for (int i = 0; i < pSquareImageWidthHeight; i++)
        for (int v = 0; v < pSquareImageWidthHeight; v++)
          for (int u = 0; u < pSquareImageWidthHeight; u++)
          {
            double lCosineValue = cos(lPiFactor * (u + 0.5) * i)
                                  * cos(lPiFactor * (v + 0.5) * j);
            if (i == 0)
              lCosineValue *= 1 / sqrt(2.0);

            if (j == 0)
              lCosineValue *= 1 / sqrt(2.0);

            lCosineValue *= 2.0 / pSquareImageWidthHeight;

            setMatrixToMatrixLinearMap(lDenseMatrix64F,
                                       pSquareImageWidthHeight,
                                       i,
                                       j,
                                       u,
                                       v,
                                       lCosineValue);
          }

    return normalize(lDenseMatrix64F);
  }

  public static DenseMatrix64F normalize(DenseMatrix64F pDenseMatrix64F)
  {
    for (int col = 0; col < pDenseMatrix64F.getNumCols(); col++)
    {
      double lNorm = 0;
      for (int row = 0; row < pDenseMatrix64F.getNumRows(); row++)
      {
        double lValue = pDenseMatrix64F.get(row, col);
        lNorm += lValue * lValue;
      }

      lNorm = sqrt(lNorm);

      for (int row = 0; row < pDenseMatrix64F.getNumRows(); row++)
      {
        double lValue = pDenseMatrix64F.get(row, col);
        lValue = lValue / lNorm;
        pDenseMatrix64F.set(row, col, lValue);
      }
    }

    return pDenseMatrix64F;
  }

  private static void setMatrixToMatrixLinearMap(DenseMatrix64F pDenseMatrix64F,
                                                 int pSquareImageWidthHeight,
                                                 int i,
                                                 int j,
                                                 int u,
                                                 int v,
                                                 double pZernikeValue)
  {
    final int lColumnOffset = j * pSquareImageWidthHeight + i;
    final int lRowOffset = v * pSquareImageWidthHeight + u;
    pDenseMatrix64F.set(lRowOffset, lColumnOffset, pZernikeValue);
  }

  public static DenseMatrix64F multiply(DenseMatrix64F pMatrix, double pFactor) {
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(pMatrix.numCols, pMatrix.numRows);
    for (int y = 0; y < lResultMatrix.numRows; y++)
    {
      for (int x = 0; x < lResultMatrix.numCols; x++)
      {
        lResultMatrix.set(y, x, pMatrix.get(y, x) * pFactor);
      }
    }
    return lResultMatrix;
  }

  public static DenseMatrix64F sum(List<DenseMatrix64F> pMatrixList) {
    if (pMatrixList.size() == 0) {
      return null;
    }
    DenseMatrix64F lReferenceMatrix = pMatrixList.get(0);
    DenseMatrix64F lSumMatrix = new DenseMatrix64F(lReferenceMatrix.numCols, lReferenceMatrix.numRows);

    for (DenseMatrix64F lMatrix : pMatrixList) {
      for (int y = 0; y < lMatrix.numRows; y++)
      {
        for (int x = 0; x < lMatrix.numCols; x++)
        {
          lSumMatrix.add(y, x, lMatrix.get(y, x));
        }
      }
    }
    return lSumMatrix;
  }

  public static double meanSquaredError(DenseMatrix64F pMatrix1, DenseMatrix64F pMatrix2) {
    if (pMatrix1.numRows != pMatrix2.numRows) {
      return Double.NaN;
    }
    if (pMatrix1.numCols != pMatrix2.numCols) {
      return Double.NaN;
    }

    double sum = 0;
    for (int y = 0; y < pMatrix1.numRows; y++)
    {
      for (int x = 0; x < pMatrix1.numCols; x++)
      {
        sum += (Math.pow(pMatrix1.get(y,x) - pMatrix2.get(y, x),2) );
      }
    }

    return sum / pMatrix1.numCols / pMatrix1.numRows;
  }

  public static boolean matricesEqual(DenseMatrix64F pMatrix1, DenseMatrix64F pMatrix2, double pTolerance) {
    if (pMatrix1.numRows != pMatrix2.numRows) {
      return false;
    }
    if (pMatrix1.numCols != pMatrix2.numCols) {
      return false;
    }

    for (int y = 0; y < pMatrix1.numRows; y++)
    {
      for (int x = 0; x < pMatrix1.numCols; x++)
      {
        if (Math.abs(pMatrix1.get(y,x) - pMatrix2.get(y, x)) > pTolerance) {
          return false;
        }
      }
    }
    return true;
  }
}
