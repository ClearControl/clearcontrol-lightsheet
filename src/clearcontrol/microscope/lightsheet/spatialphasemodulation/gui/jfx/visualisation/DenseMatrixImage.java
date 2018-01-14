package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.ejml.data.DenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class DenseMatrixImage extends WritableImage
{
  DenseMatrix64F mMatrix;
  LookUpTable mLut;

  public DenseMatrixImage(DenseMatrix64F lMatrix, LookUpTable lLut) {
    super(lMatrix.numCols, lMatrix.numRows);
    mMatrix = lMatrix;
    mLut = lLut;

    process();
  }

  private void process() {
    PixelWriter lPixelWriter = getPixelWriter();

    for (int x = 0; x < mMatrix.numCols; x++) {
      for (int y = 0; y < mMatrix.numRows; y++) {

        float value = (float)(mMatrix.get(x,y) / 2.0 + 0.5);
        if (value < 0) {
          value = 0;
        }
        if (value > 1) {
          value = 1;
        }
        Color color = mLut.getColor(value);
        lPixelWriter.setColor(x, y, color);
      }
    }

  }

  public void setMatrix(DenseMatrix64F lMatrix) {
    mMatrix = lMatrix;
    process();
  }
}
