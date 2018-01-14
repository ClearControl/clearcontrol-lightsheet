package clearcontrol.microscope.lightsheet.spatialphasemodulation.io.demo;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FWriter;
import net.imglib2.img.array.ArrayImgs;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrixIODemo
{
  @Test
  public void testMatrixIO() throws IOException
  {
    double[][] data = { {1, 0, 0},
                        {0, 1, 0},
                        {0, 0, 1}
    };
    // generate matrix
    DenseMatrix64F lMatrix = new DenseMatrix64F(data);
    DenseMatrix64F lMatrixReloaded = new DenseMatrix64F(3,3);

    File lTempFile = File.createTempFile("temp", ".json");


    // save matrix
    DenseMatrix64FWriter lWriter = new DenseMatrix64FWriter(lTempFile, lMatrix);
    assertTrue(lWriter.write());

    // load matrix
    DenseMatrix64FReader lReader = new DenseMatrix64FReader(lTempFile, lMatrixReloaded);
    assertTrue(lReader.read());

    System.out.println("I: " + Arrays.toString(lMatrix.data));
    System.out.println("O: " + Arrays.toString(lMatrixReloaded.data));

    // check if content is the same
    assertTrue(Arrays.equals(lMatrix.data, lMatrixReloaded.data));

  }
}
