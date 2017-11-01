package clearcontrol.microscope.lightsheet.extendeddepthfield.iqm;

import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class ContrastEstimator
{
  RandomAccessibleInterval<ShortType> image;
  double[] contrastPerSlice = null;
  double[] rangesPerSlice = null;

  public ContrastEstimator(StackInterface stack) {
    StackToImgConverter<ShortType> converter = new StackToImgConverter<>(stack);
    image = converter.getRandomAccessibleInterval();
  }

  public double[] getContrastPerSlice() {
    if (contrastPerSlice == null) {
      calculateContrast();
    }
    return contrastPerSlice;
  }
  public double[] getSignalRangeSize() {
    if (rangesPerSlice == null) {
      calculateContrast();
    }
    return rangesPerSlice;
  }

  private synchronized void calculateContrast() {
    int numberOfSlices = (int)image.dimension(2);
    contrastPerSlice = new double[numberOfSlices];
    rangesPerSlice = new double[numberOfSlices];
    for (int z = 0; z < numberOfSlices; z++) {
      RandomAccessibleInterval<ShortType> slice = Views.hyperSlice(image, 2, z);
      double min = 0;
      double max = 0;
      double sum = 0;
      double count = 0;

      Cursor<ShortType> cursor = Views.iterable(slice).cursor();
      if (cursor.hasNext()) {
        double value = cursor.next().get();
        min = value;
        max = value;
        sum = value;
        count = 1;
      }
      while (cursor.hasNext()) {
        double value = cursor.next().get();
        if (min > value) {
          min = value;
        }
        if (max < value) {
          max = value;
        }
        sum += value;
        count ++;
      }

      double range = max - min;
      double mean = sum / count;
      double contrast = range / mean;

      rangesPerSlice[z] = range;
      contrastPerSlice[z] = contrast;
    }
  }




}
