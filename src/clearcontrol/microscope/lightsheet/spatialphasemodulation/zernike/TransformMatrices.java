package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static javafx.scene.input.KeyCode.X;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.List;


/**
 * TransformMatrices is a class full of utilities for matrix manipulation
 *
 * @author royer
 * @author haesleinhuepf
 * @author debayansaha102
 */
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

  /**
   * Allows multiplying a matrix with a scalar
   * @param pMatrix matrix M
   * @param pFactor scalar s
   * @return M*s
   */
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

  /**
   * Allows multiplying a matrix element-wise with another matrix
   * @param pMatrix1 matrix M
   * @param pMatrix2 matrix N
   * @return M.*N
   */
  public static DenseMatrix64F multiplyElementWise(DenseMatrix64F pMatrix1, DenseMatrix64F pMatrix2) {
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(pMatrix1.numCols, pMatrix1.numRows);
    for (int y = 0; y < lResultMatrix.numRows; y++)
    {
      for (int x = 0; x < lResultMatrix.numCols; x++)
      {
        lResultMatrix.set(y, x, pMatrix1.get(y, x) * pMatrix2.get(y, x));
      }
    }
    return lResultMatrix;
  }


  /**
   * Allows summing a list of matrices
   * @param pMatrixList list
   * @return sum matrix of all list elements
   */
  public static DenseMatrix64F sum(List<DenseMatrix64F> pMatrixList) {
    if (pMatrixList.size() == 0) {
      return null;
    }
    DenseMatrix64F lReferenceMatrix = pMatrixList.get(0);
    DenseMatrix64F lSumMatrix = new DenseMatrix64F(lReferenceMatrix.numRows, lReferenceMatrix.numCols);

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

    /**
     * Allows summing a list of matrices
     * @param pMatrixArray list
     * @return sum matrix of all array elements
     */
  public static DenseMatrix64F sum(DenseMatrix64F... pMatrixArray)
  {
    ArrayList<DenseMatrix64F> lList = new ArrayList<DenseMatrix64F>();

    for (int i = 0; i < pMatrixArray.length; i++) {
      lList.add(pMatrixArray[i]);
    }
    return sum(lList);
  }

    /**
     * Returns the MSE of two matrices (element wise)
     * @param pMatrix1 Matrix M containing elements m_ij
     * @param pMatrix2 Matrix N containing elements n_ij
     * @return MSE = sum_ij(pow(m_ij - n_ij, 2))
     */
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

    /**
     * determines if two matrices are equal up to a given tolerance
     * @param pMatrix1 M  with elements m_ij
     * @param pMatrix2 N with elements n_ij
     * @param pTolerance t
     * @return true if abs(m_ij - n_ij) <= t for all matrix elements
     */
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

  public static boolean flipSquareMatrixVertical(DenseMatrix64F pSourceMatrix, DenseMatrix64F pTargetMatrix) {
    if (pSourceMatrix == pTargetMatrix ||
        pSourceMatrix.numRows != pTargetMatrix.numRows ||
        pSourceMatrix.numCols != pTargetMatrix.numCols ||
        pSourceMatrix.numRows != pSourceMatrix.numCols)
    {
      return false;
    }

    for (int y = 0; y < pSourceMatrix.numRows; y++)
    {
      for (int x = 0; x < pSourceMatrix.numCols; x++)
      {
        pTargetMatrix.set(pSourceMatrix.numRows - y - 1, x, pSourceMatrix.get(y, x));
      }
    }

    return false;
  }

  public static boolean flipSquareMatrixHorizontal(DenseMatrix64F pSourceMatrix, DenseMatrix64F pTargetMatrix) {
    if (pSourceMatrix == pTargetMatrix ||
        pSourceMatrix.numRows != pTargetMatrix.numRows ||
        pSourceMatrix.numCols != pTargetMatrix.numCols ||
        pSourceMatrix.numRows != pSourceMatrix.numCols)
    {
      return false;
    }

    for (int y = 0; y < pSourceMatrix.numRows; y++)
    {
      for (int x = 0; x < pSourceMatrix.numCols; x++)
      {
        pTargetMatrix.set(y, pSourceMatrix.numCols - x - 1, pSourceMatrix.get(y, x));
      }
    }

    return false;
  }

  public static boolean flipSquareMatrixXY(DenseMatrix64F pSourceMatrix, DenseMatrix64F pTargetMatrix) {
    if (pSourceMatrix == pTargetMatrix ||
        pSourceMatrix.numRows != pTargetMatrix.numRows ||
        pSourceMatrix.numCols != pTargetMatrix.numCols ||
        pSourceMatrix.numRows != pSourceMatrix.numCols)
    {
      return false;
    }

    for (int y = 0; y < pSourceMatrix.numRows; y++)
    {
      for (int x = 0; x < pSourceMatrix.numCols; x++)
      {
        pTargetMatrix.set(x, y, pSourceMatrix.get(y, x));
      }
    }

    return false;
  }

  public static void rotateClockwise(DenseMatrix64F pSourceMatrix,
                                     DenseMatrix64F pTargetMatrox)
  {
    TransformMatrices.flipSquareMatrixXY(pSourceMatrix, pTargetMatrox);
    pSourceMatrix = pTargetMatrox.copy();
    TransformMatrices.flipSquareMatrixVertical(pSourceMatrix, pTargetMatrox);
  }

  public static void rotateCounterClockwise(DenseMatrix64F pSourceMatrix,
                                     DenseMatrix64F pTargetMatrox)
  {
    TransformMatrices.flipSquareMatrixXY(pSourceMatrix, pTargetMatrox);
    pSourceMatrix = pTargetMatrox.copy();
    TransformMatrices.flipSquareMatrixHorizontal(pSourceMatrix, pTargetMatrox);
  }

  public static double getMaxOfMatrix(DenseMatrix64F pMatrix) {
    double[] lData = pMatrix.data;

    double lMax = Double.MIN_VALUE;
    for (double lValue : lData) {
      if (lValue > lMax) {
        lMax = lValue;
      }
    }
    return lMax;
  }

  public static double getMinOfMatrix(DenseMatrix64F pMatrix) {
    double[] lData = pMatrix.data;

    double lMin = Double.MAX_VALUE;
    for (double lValue : lData) {
      if (lValue < lMin) {
        lMin = lValue;
      }
    }
    return lMin;
  }

  /**
   * Performs Matrix Multiplication
   * @param pMatrix1 matrix M
   * @param pMatrix2 matrix N
   * @return M*N
   */
  public static DenseMatrix64F multiplyMatrix(DenseMatrix64F pMatrix1, DenseMatrix64F pMatrix2) {
    if(pMatrix1.numCols != pMatrix2.numRows){
      throw new IllegalArgumentException("The matrix dimensions are not compatible for multiplication (" + pMatrix1.numRows +"," + pMatrix1.numCols+"),(" + pMatrix2.numRows+","+pMatrix2.numCols+")");
    }
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(pMatrix1.numRows, pMatrix2.numCols);
    for (int y = 0; y < lResultMatrix.numRows; y++)
    {
      for (int x = 0; x < lResultMatrix.numCols; x++)
      {
        double s = 0.0;
        for (int z = 0; z < pMatrix1.numCols; z++)
        {
          s += pMatrix1.get(y, z) * pMatrix2.get(z, x);
        }
        lResultMatrix.set(y, x, s);
      }
    }
    return lResultMatrix;
  }

  /**
   * Performs Matrix Transpose
   * @param pMatrix1 matrix M
   * @return M'
   */
  public static DenseMatrix64F transposeMatrix(DenseMatrix64F pMatrix1){
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(pMatrix1.numCols, pMatrix1.numRows);
    for (int y = 0; y < lResultMatrix.numRows; y++)
    {
      for (int x = 0; x < lResultMatrix.numCols; x++)
      {
        lResultMatrix.set(y, x, pMatrix1.get(x, y));
      }
    }
    return(lResultMatrix);
  }

  public static DenseMatrix64F convert1DDoubleArrayToDense64RowMatrix(double[] pArray){
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(pArray.length, 1);
    for( int y=0; y<pArray.length;y++){
      lResultMatrix.set(y,0,pArray[y]);
    }
    return(lResultMatrix);
  }

  public static DenseMatrix64F convert1DDoubleArrayToDense64ColumnMatrix(double[] pArray){
    DenseMatrix64F lResultMatrix = new DenseMatrix64F(1,pArray.length);
    for( int y=0; y<pArray.length;y++){
      lResultMatrix.set(0,y,pArray[y]);
    }
    return(lResultMatrix);
  }

  public static double[] convertDense64MatrixTo1DDoubleArray(DenseMatrix64F pMatrix){
    double[] lResultArray = new double[pMatrix.getNumElements()];
    int counter = 0;
    for( int y=0; y<pMatrix.getNumRows();y++){
      for( int x=0; x<pMatrix.getNumCols(); x++){
        lResultArray[counter] = pMatrix.get(y,x);
        counter ++;
      }

    }
    return(lResultArray);
  }

}
