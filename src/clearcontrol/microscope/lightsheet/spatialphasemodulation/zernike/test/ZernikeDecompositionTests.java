package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.test;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.SimpleZernikeDecomposer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class ZernikeDecompositionTests
{
  @Test
  public void testZernikeDecomposition() {
    int lWidth = 11;
    int lHeight = 11;

    ArrayList<DenseMatrix64F> lList = new ArrayList<>();
    lList.add(TransformMatrices.multiply(new ZernikePolynomialsDenseMatrix64F(lWidth, lHeight, 0,0), 0.5));
    lList.add(TransformMatrices.multiply(new ZernikePolynomialsDenseMatrix64F(lWidth, lHeight, -2,2), 0.5));

    DenseMatrix64F lSumMatrix = TransformMatrices.sum(lList);

    SimpleZernikeDecomposer lDecomposer = new SimpleZernikeDecomposer(lSumMatrix);
    System.out.println(lDecomposer.getCompositionCode());

  }

  @Test
  public void simpleTest() {

    System.out.println("target: Z[0,0]");
    SimpleZernikeDecomposer lDecomposer = new SimpleZernikeDecomposer(TransformMatrices.multiply(new ZernikePolynomialsDenseMatrix64F(11,11, 0,0), 1.0));
    System.out.println(lDecomposer.getCompositionCode());
    assertTrue(lDecomposer.getCompositionCode().equals("Z[0,0] 1.0\n"));

    System.out.println("target: Z[0,0]");
    SimpleZernikeDecomposer lDecomposer1 = new SimpleZernikeDecomposer(new ZernikePolynomialsDenseMatrix64F(11,11, 0,0));
    System.out.println(lDecomposer1.getCompositionCode());
    assertTrue(lDecomposer1.getCompositionCode().equals("Z[0,0] 1.0\n"));

    System.out.println("target: Z[-1,1]");
    SimpleZernikeDecomposer lDecomposer2 = new SimpleZernikeDecomposer(new ZernikePolynomialsDenseMatrix64F(11,11, -1,1));
    System.out.println(lDecomposer2.getCompositionCode());
    assertTrue(lDecomposer2.getCompositionCode().equals("Z[-1,1] 1.0\n"));

    System.out.println("target: Z[0,2]");
    SimpleZernikeDecomposer lDecomposer3 = new SimpleZernikeDecomposer(new ZernikePolynomialsDenseMatrix64F(11,11, 0,2));
    System.out.println(lDecomposer3.getCompositionCode());
    assertTrue(lDecomposer3.getCompositionCode().equals("Z[0,2] 1.0\n"));
  }
}
