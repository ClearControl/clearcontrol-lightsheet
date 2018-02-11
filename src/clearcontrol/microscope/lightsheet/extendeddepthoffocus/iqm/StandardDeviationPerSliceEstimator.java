package clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.Views;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.imglib2.StackToImgConverter;

/**
 * ToDo; This doesn't work with large mImage stacks, because StackToImgConverter
 * tries to build ArrayImgs and not PlanarImgs.
 * <p>
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) November 2017
 */
@Deprecated
public class StandardDeviationPerSliceEstimator
{
  RandomAccessibleInterval<ShortType> mImage;
  double[] standardDeviationPerSlice = null;

  public StandardDeviationPerSliceEstimator(StackInterface stack)
  {

    StackToImgConverter<ShortType> lConverter =
                                              new StackToImgConverter<>(stack);
    mImage = lConverter.getRandomAccessibleInterval();
  }

  public double[] getQualityArray()
  {
    if (standardDeviationPerSlice == null)
    {
      calculate();
    }
    return standardDeviationPerSlice;
  }

  private synchronized void calculate()
  {

    int lNumberOfSlices = (int) mImage.dimension(2);
    standardDeviationPerSlice = new double[lNumberOfSlices];

    for (int z = 0; z < lNumberOfSlices; z++)
    {
      RandomAccessibleInterval<ShortType> lSlice =
                                                 Views.hyperSlice(mImage,
                                                                  2,
                                                                  z);

      Cursor<ShortType> lCursor = Views.iterable(lSlice)
                                       .localizingCursor();

      double lSum = 0;
      long lCount = 0;
      while (lCursor.hasNext())
      {
        lSum += lCursor.next().get();
        lCount++;
      }
      double lMean = lSum / lCount;

      lSum = 0;
      lCursor.reset();
      while (lCursor.hasNext())
      {
        lSum += Math.pow(lCursor.next().get() - lMean, 2);
      }
      double lStdDev = lSum / (lCount - 1);

      standardDeviationPerSlice[z] = lStdDev;
    }

  }
}
