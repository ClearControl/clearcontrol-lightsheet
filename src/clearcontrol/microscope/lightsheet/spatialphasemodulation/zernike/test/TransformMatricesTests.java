package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.junit.Test;

public class TransformMatricesTests
{

  private static final int cDimension = 8;

  @Test
  public void testZernickeTransformMatrix()
  {
    final DenseMatrix64F lComputeZernickeTransformMatrix =
                                                         TransformMatrices.computeZernickeTransformMatrix(cDimension);
    // System.out.println(lComputeZernickeTransformMatrix);

    final double lDeterminant =
                              CommonOps.det(lComputeZernickeTransformMatrix);
    // System.out.println(lDeterminant);

    // assertEquals(1, lDeterminant, 1e-10);
  }

  @Test
  public void testCosineTransformMatrix()
  {
    final DenseMatrix64F lComputeCosineTransformMatrix =
                                                       TransformMatrices.computeCosineTransformMatrix(cDimension);
    // System.out.println(lComputeCosineTransformMatrix);

    final double lDeterminant =
                              CommonOps.det(lComputeCosineTransformMatrix);
    // System.out.println(lDeterminant);

    assertEquals(1, lDeterminant, 1e-10);
  }

  @Test
  public void testSumMatrices()
  {

    DenseMatrix64F lMatrix1 = new DenseMatrix64F(new double[][]
    {
      { 0, 1 },
      { 0, 1 } });
    DenseMatrix64F lMatrix2 = new DenseMatrix64F(new double[][]
    {
      { 1, 1 },
      { 1, 1 } });
    DenseMatrix64F lMatrix3 = new DenseMatrix64F(new double[][]
    {
      { 1, 2 },
      { 1, 2 } });

    ArrayList<DenseMatrix64F> lList = new ArrayList<>();
    lList.add(lMatrix1);
    lList.add(lMatrix2);

    DenseMatrix64F lResultMatrix = TransformMatrices.sum(lList);
    assertTrue(TransformMatrices.matricesEqual(lMatrix3,
                                               lResultMatrix,
                                               0.1));

  }

  @Test
  public void testMultiplyMatrix()
  {

    DenseMatrix64F lMatrix1 = new DenseMatrix64F(new double[][]
    {
      { 1, 2 },
      { 3, 4 } });
    DenseMatrix64F lMatrix2 = new DenseMatrix64F(new double[][]
    {
      { 2, 0 },
      { 1, 2 } });
    DenseMatrix64F lMatrix3 = new DenseMatrix64F(new double[][]
    {
      { 4, 4 },
      { 10, 8 } });

    DenseMatrix64F lResultMatrix =
                                 TransformMatrices.multiplyMatrix(lMatrix1,
                                                                  lMatrix2);
    assertTrue(TransformMatrices.matricesEqual(lMatrix3,
                                               lResultMatrix,
                                               0.1));

  }

  @Test
  public void testTrasposeMatrix()
  {

    DenseMatrix64F lMatrix1 = new DenseMatrix64F(new double[][]
    {
      { 5, 4, 3 },
      { 4, 0, 4 },
      { 7, 10, 3 } });
    DenseMatrix64F lMatrix3 = new DenseMatrix64F(new double[][]
    {
      { 5, 4, 7 },
      { 4, 0, 10 },
      { 3, 4, 3 } });

    DenseMatrix64F lResultMatrix =
                                 TransformMatrices.transposeMatrix(lMatrix1);
    assertTrue(TransformMatrices.matricesEqual(lMatrix3,
                                               lResultMatrix,
                                               0.1));

  }

  @Test
  public void testconvert1DDoubleArrayToDense64RowMatrix()
  {

    double[] lArray =
    { 1, 2, 3, 4 };
    DenseMatrix64F lMatrix3 = new DenseMatrix64F(new double[][]
    {
      { 1 },
      { 2 },
      { 3 },
      { 4 } });

    DenseMatrix64F lResultMatrix =
                                 TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(lArray);
    assertTrue(TransformMatrices.matricesEqual(lMatrix3,
                                               lResultMatrix,
                                               0.1));

  }

  @Test
  public void testconvertDense64MatrixTo1DDoubleArra()
  {

    double[] lArray =
    { 1, 2, 3, 4 };
    DenseMatrix64F lMatrix3 = new DenseMatrix64F(new double[][]
    {
      { 1 },
      { 2 },
      { 3 },
      { 4 } });

    double[] lResultArray =
                          TransformMatrices.convertDense64MatrixTo1DDoubleArray(lMatrix3);
    assertTrue(TransformMatrices.matricesEqual(TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(lArray),
                                               TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(lResultArray),
                                               0.1));

  }

}
