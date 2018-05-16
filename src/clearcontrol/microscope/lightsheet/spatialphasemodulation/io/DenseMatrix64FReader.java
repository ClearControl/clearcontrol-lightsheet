package clearcontrol.microscope.lightsheet.spatialphasemodulation.io;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.CheckBox;
import org.ejml.data.DenseMatrix64F;

import java.io.File;
import java.io.IOException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrix64FReader
{
  File mSourceFile;
  DenseMatrix64F mTargetMatrix;

  public DenseMatrix64FReader(File pSourceFile) {
    mSourceFile = pSourceFile;
    mTargetMatrix = null;
  }

  public DenseMatrix64FReader(File pSourceFile, DenseMatrix64F pTargetMatrix) {
    mSourceFile = pSourceFile;
    mTargetMatrix = pTargetMatrix;
  }

  public boolean read() {
    Double[][] data; // = new Double[mTargetMatrix.numRows][mTargetMatrix.numCols];


    ObjectMapper lObjectMapper = new ObjectMapper();
    lObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    lObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false);

    try
    {
      data = lObjectMapper.readValue(mSourceFile, Double[][].class);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }

    if (mTargetMatrix == null) {
      mTargetMatrix = new DenseMatrix64F(data[0].length, data.length);
    }

    for (int y = 0; y < mTargetMatrix.numRows; y++) {
      for (int x = 0; x < mTargetMatrix.numCols; x++) {
        mTargetMatrix.set(y, x, data[x][y]);
      }
    }

    //mSpatialPhaseModulatorDevice.getMatrixReference().set(lTargetMatrix);




    return true;
  }

  public DenseMatrix64F getMatrix() {
    if (mTargetMatrix == null) {
      read();
    }
    return mTargetMatrix;
  }
}
