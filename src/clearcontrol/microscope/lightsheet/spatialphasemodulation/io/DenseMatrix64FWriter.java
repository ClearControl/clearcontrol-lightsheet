package clearcontrol.microscope.lightsheet.spatialphasemodulation.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.ejml.data.DenseMatrix64F;

import java.io.*;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrix64FWriter
{
  File mTargetFile;
  DenseMatrix64F mSourceMatrix;
  public DenseMatrix64FWriter(File pTargetFile, DenseMatrix64F pSourceMatrix) {
    mTargetFile = pTargetFile;
    mSourceMatrix = pSourceMatrix;
  }

  public boolean write() {
    Double[][] data = new Double[mSourceMatrix.numRows][mSourceMatrix.numCols];

    for (int y = 0; y < mSourceMatrix.numRows; y++) {
      for (int x = 0; x < mSourceMatrix.numCols; x++) {
        data[x][y] = mSourceMatrix.get(x, y);
      }
    }

    ObjectMapper lObjectMapper = new ObjectMapper();
    lObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    lObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);

    try
    {
      lObjectMapper.writeValue(mTargetFile, data);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}