package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;

import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class DenseMatrixImage extends WritableImage
{
  DenseMatrix64F mMatrix;
  LookUpTable mLookUpTable;

  public DenseMatrixImage(DenseMatrix64F lMatrix,
                          LookUpTable lLookUpTable)
  {
    super(lMatrix.numCols, lMatrix.numRows);
    mMatrix = lMatrix;
    mLookUpTable = lLookUpTable;

    process();
  }

  private void process()
  {
    PixelWriter lPixelWriter = getPixelWriter();

    for (int x = 0; x < mMatrix.numCols; x++)
    {
      for (int y = 0; y < mMatrix.numRows; y++)
      {

        float lValue = (float) (mMatrix.get(x, y) / 2.0 + 0.5);
        if (lValue < 0)
        {
          lValue = 0;
        }
        if (lValue > 1)
        {
          lValue = 1;
        }
        Color color = mLookUpTable.getColor(lValue);
        lPixelWriter.setColor(x, y, color);
      }
    }

  }

  public void setMatrix(DenseMatrix64F lMatrix)
  {
    mMatrix = lMatrix;
    process();
  }
}
