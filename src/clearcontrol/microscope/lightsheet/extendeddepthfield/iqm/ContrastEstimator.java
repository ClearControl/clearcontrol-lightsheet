package clearcontrol.microscope.lightsheet.extendeddepthfield.iqm;

import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class ContrastEstimator
{
  RandomAccessibleInterval<ShortType> image;
  double[] standardDeviationPerSlice = null;

  public ContrastEstimator(StackInterface stack) {
    StackToImgConverter<ShortType> converter = new StackToImgConverter<>(stack);
    image = converter.getRandomAccessibleInterval();
  }

  public double[] getContrastPerSlice() {
    if (standardDeviationPerSlice == null) {
      calculateContrast();
    }
    return standardDeviationPerSlice;
  }

  private synchronized void calculateContrast() {
    int numberOfSlices = (int)image.dimension(2);
    standardDeviationPerSlice = new double[numberOfSlices];

    for (int z = 0; z < numberOfSlices; z++) {
      RandomAccessibleInterval<ShortType> slice = Views.hyperSlice(image, 2, z);

      Cursor<ShortType> cursor = Views.iterable(slice).localizingCursor();

      double sum = 0;
      long count = 0;
      while (cursor.hasNext()) {
        sum += cursor.next().get();
        count++;
      }
      double mean = sum / count;

      sum = 0;
      cursor.reset();
      while (cursor.hasNext()) {
        sum += Math.pow(cursor.next().get() - mean, 2);
      }
      double stdDev = sum / (count - 1);
      System.out.println("z: " + z + " stddev:" + stdDev);
      standardDeviationPerSlice[z] = stdDev;
    }
  }




}
