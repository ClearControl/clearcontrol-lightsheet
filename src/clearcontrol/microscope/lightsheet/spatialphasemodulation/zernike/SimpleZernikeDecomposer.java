package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;

/**
 * This class implements a simple, iterative decomposition of Zernike
 * modes. It is not intended to be used in productive environments, it's
 * just here for testing until a better/more precise implementation
 * comes up.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class SimpleZernikeDecomposer
{
  private ArrayList<ZernikePolynomialsDenseMatrix64F> mZernikePolynomialList = null;
  private ArrayList<Double> mMostImportantCoefficients;
  private ArrayList<ZernikePolynomialsDenseMatrix64F> mMostImportantZernikePolynomialsList;

  final double mMinFactor;
  final double mMaxFactor;
  final double mMinAbsoluteFactor;
  int sMaxN = 6;
  private String mDecomposerComment;

  public SimpleZernikeDecomposer(DenseMatrix64F pMatrix, double pMinFactor, double pMaxFactor, double pMinAbsoluteFactor) {
    mMinFactor = pMinFactor;
    mMaxFactor = pMaxFactor;
    mMinAbsoluteFactor = pMinAbsoluteFactor;


    initializeZernikePolynomialList(pMatrix.numCols, pMatrix.numRows);

    mMostImportantCoefficients = new ArrayList<>();

    mMostImportantZernikePolynomialsList = new ArrayList<>();

    decompose(pMatrix);
  }

  public SimpleZernikeDecomposer(DenseMatrix64F pMatrix) {
    this(pMatrix, -1, 1, 0.001);
  }

  public String getCompositionCode() {
    String result = "";
    double sum = 0;
    for (int i = 0; i < mMostImportantCoefficients.size(); i++)
    {
      sum = sum + mMostImportantCoefficients.get(i);
    }

    for (int i = 0; i < mMostImportantCoefficients.size(); i++) {

        result =
            result
            + "Z["
            + mMostImportantZernikePolynomialsList.get(i).getM()
            + ","
            + mMostImportantZernikePolynomialsList.get(i).getN()
            + "] "
            + (mMostImportantCoefficients.get(i) / sum)
            + "\n";

    }
    result = result + mDecomposerComment;
    return result;
  }

  private void decompose(DenseMatrix64F pMatrix) {
    int index = getIndexOfMostSimilarZernikePolynomial(pMatrix);
    if (index < 0) {
      return;
    }

    ZernikePolynomialsDenseMatrix64F lMatrixMultiplicant = mZernikePolynomialList.get(index);
    //System.out.println("Checking " + mZernikePolynomialList.get(index).getM() + " " + mZernikePolynomialList.get(index).getN());


    double sum = 0;
    long count = 0;
    for (int y = 0; y < lMatrixMultiplicant.numRows; y++) {
      for (int x = 0; x < lMatrixMultiplicant.numCols; x++) {
        if (lMatrixMultiplicant.get(y, x) != 0) {
          sum = sum + pMatrix.get(y, x) / lMatrixMultiplicant.get(y, x);
          count++;
        }
      }
    }
    //System.out.println("Sum " + sum);
    double factor = sum / count; //(lMatrixMultiplicant.numCols * lMatrixMultiplicant.numRows);
    //System.out.println("Factor " + factor);

    boolean lContinueDecomposition = true;
    if (Math.abs(factor) > mMinAbsoluteFactor &&
        factor >= mMinFactor &&
        factor <= mMaxFactor) {
      System.out.println("Add to list " + factor);
      mMostImportantCoefficients.add(factor);
      mMostImportantZernikePolynomialsList.add(lMatrixMultiplicant);
      mDecomposerComment = "# Decomposition OK";
    }
    else // error / strange situation handling
    {
      if (mMostImportantCoefficients.size() == 0)
      {
        mMostImportantCoefficients.add(1.0);
        mMostImportantZernikePolynomialsList.add(lMatrixMultiplicant);
        mDecomposerComment =
            "# Decomposer failed in first step.\n";
        lContinueDecomposition = false;
      } else if (mMostImportantCoefficients.size() == 1) {
        mMostImportantCoefficients.add(1.0 - mMostImportantCoefficients.get(0));
        mMostImportantZernikePolynomialsList.add(lMatrixMultiplicant);
        mDecomposerComment =
            "# Decomposer failed in second step.\n";
        lContinueDecomposition = false;
      }
    }
    mZernikePolynomialList.remove(index);


    if (lContinueDecomposition || mZernikePolynomialList.size() > 0) {

      DenseMatrix64F lRemaining = new DenseMatrix64F(pMatrix.numCols, pMatrix.numRows);

      for (int y = 0; y < lMatrixMultiplicant.numRows; y++) {
        for (int x = 0; x < lMatrixMultiplicant.numCols; x++) {
          if (lMatrixMultiplicant.get(y, x) != 0) {
            lRemaining.set(y, x, pMatrix.get(y, x) - lMatrixMultiplicant.get(y, x) * factor);
          }
        }
      }

      decompose(lRemaining);
    }

  }

  private int getIndexOfMostSimilarZernikePolynomial(DenseMatrix64F pMatrix)
  {
    double minMSE = Double.MAX_VALUE;
    int minMSEIndex = -1;
    for (int i = 0; i < mZernikePolynomialList.size(); i++) {
      double mse = TransformMatrices.meanSquaredError(pMatrix, mZernikePolynomialList.get(i));
      if (mse < minMSE) {
        minMSE = mse;
        minMSEIndex = i;
      }
    }
    return minMSEIndex;
  }

  private void initializeZernikePolynomialList(int pWidth, int pHeight)
  {
    mZernikePolynomialList = new ArrayList<>();

    for (int n = 0; n <= sMaxN; n++) {
      for (int m = -n; m <= n; m += 2) {
        //System.out.println("m" + m + " n" + n);
        mZernikePolynomialList.add(new ZernikePolynomialsDenseMatrix64F(pWidth, pHeight, m,n));
      }
    }
  }

}
