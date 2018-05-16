package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike;

import org.ejml.data.DenseMatrix64F;

import java.util.ArrayList;

/**
 * This class implements a simple, iterative decomposition of Zernike
 * modes. It is not intended to be used in productive environments, it's
 * just here for testing until a better/more precise implementation
 * comes up.
 *
 * This decomposer takes a given matrix M0 and determined a Zernike mode
 * Z1 the matrix is most similar to. It subtracts then w1*Z1 from M0:
 *
 * M1 = M0 - w1*Z1
 *
 * With M1 the process is repeated until no Zernike modes are available anymore.
 * Then, the list of Zernike modes Z and the list of weights w are
 * assembled to a string like:
 *
 * Z[0,0] 0.5
 * Z[-1,1] 0.25
 * Z[1,1] 0.25
 *
 * Todo: replace this class by a more sophisticated decomposition
 * Deprecated: There will be no more sophisticated decomposition implemented
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
@Deprecated
public class SimpleZernikeDecomposer
{
  private ArrayList<ZernikePolynomialsDenseMatrix64F> mZernikePolynomialList = null;
  private ArrayList<Double> mMostImportantCoefficients;
  private ArrayList<ZernikePolynomialsDenseMatrix64F> mMostImportantZernikePolynomialsList;

  final double mMinFactor;
  final double mMaxFactor;
  final double mMinAbsoluteFactor;

  /**
   * Maximum n parameter of Zernike modes
   */
  int sMaxN = 6;

  /**
   * The composer assembles a short text explaining success or failure of the decomposition
   */
  private String mDecomposerComment;

  /**
   * Constructor
   * @param pMatrix Zernike mode to decompose
   * @param pMinFactor maximum factor which is allowed for each component
   * @param pMaxFactor minimum factor which is allowed for each component
   * @param pMinAbsoluteFactor minimum absolute factor to prevent small weights
   */
  public SimpleZernikeDecomposer(DenseMatrix64F pMatrix, double pMinFactor, double pMaxFactor, double pMinAbsoluteFactor) {
    mMinFactor = pMinFactor;
    mMaxFactor = pMaxFactor;
    mMinAbsoluteFactor = pMinAbsoluteFactor;

    initializeZernikePolynomialList(pMatrix.numCols, pMatrix.numRows);

    mMostImportantCoefficients = new ArrayList<Double>();

    mMostImportantZernikePolynomialsList = new ArrayList<ZernikePolynomialsDenseMatrix64F>();

    decompose(pMatrix);
  }

  public SimpleZernikeDecomposer(DenseMatrix64F pMatrix) {
    this(pMatrix, -1, 1, 0.001);
  }

  public String getCompositionCode() {
    String lResultText = "";
    double lSum = 0;
    for (int i = 0; i < mMostImportantCoefficients.size(); i++)
    {
      lSum = lSum + mMostImportantCoefficients.get(i);
    }

    for (int i = 0; i < mMostImportantCoefficients.size(); i++) {

        lResultText =
            lResultText
            + "Z["
            + mMostImportantZernikePolynomialsList.get(i).getM()
            + ","
            + mMostImportantZernikePolynomialsList.get(i).getN()
            + "] "
            + (mMostImportantCoefficients.get(i) / lSum)
            + "\n";

    }
    lResultText = lResultText + mDecomposerComment;
    return lResultText;
  }

  private void decompose(DenseMatrix64F pMatrix) {
    int lIndex = getIndexOfMostSimilarZernikePolynomial(pMatrix);
    if (lIndex < 0) {
      return;
    }

    ZernikePolynomialsDenseMatrix64F lMatrixMultiplicant = mZernikePolynomialList.get(lIndex);
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
      //System.out.println("Add to list " + factor);
      mMostImportantCoefficients.add(factor);
      mMostImportantZernikePolynomialsList.add(lMatrixMultiplicant);
      mDecomposerComment = "# Decomposition OK";
    }
    else // error / strange situation handling
    {
      // This block is entered if the decomposer fails to find a good
      // factor. To prevent an empty result, something is printed out
      // as result and the comment points the user to a potential
      // algorithm failure.
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
    mZernikePolynomialList.remove(lIndex);


    if (lContinueDecomposition || mZernikePolynomialList.size() > 0) {
      // Compute remaining matrix values and continue decomposition
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

  /**
   * Determine the Zernike mode in the list which is the most similar
   * to the given matrix.
   *
   * Todo: take a unknown factor into account.
   *
   * @param pMatrix
   * @return
   */
  private int getIndexOfMostSimilarZernikePolynomial(DenseMatrix64F pMatrix)
  {
    double lMinimumMSE = Double.MAX_VALUE;
    int lMinimumMSEIndex = -1;
    for (int i = 0; i < mZernikePolynomialList.size(); i++) {
      double lMSE = TransformMatrices.meanSquaredError(pMatrix, mZernikePolynomialList.get(i));
      if (lMSE < lMinimumMSE) {
        lMinimumMSE = lMSE;
        lMinimumMSEIndex = i;
      }
    }
    return lMinimumMSEIndex;
  }

  /**
   * Initialize the list of Zernike modes which can be composed
   * @param pWidth
   * @param pHeight
   */
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
