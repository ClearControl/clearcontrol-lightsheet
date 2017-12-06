package clearcontrol.microscope.lightsheet.livestatistics;

import clearcl.*;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.imglib2.StackToImgConverter;
import clearcontrol.stack.processor.StackProcessorInterface;
import clearcontrol.stack.processor.clearcl.ClearCLStackProcessorBase;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.offheap.OffHeapMemoryAccess;
import coremem.recycling.RecyclerInterface;
import fastfuse.FastFusionEngineInterface;
import fastfuse.tasks.TaskBase;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.ComputeMinMax;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.apache.bcel.generic.FLOAD;

import java.io.IOException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * December 2017
 */
public class LiveStatisticsProcessor extends ClearCLStackProcessorBase
    implements
    StackProcessorInterface,
    VisualConsoleInterface
{
  LightSheetMicroscope mLightSheetMicroscope;

  public LiveStatisticsProcessor(String pProcessorName,
                                 LightSheetMicroscope pLightSheetMicroscope,
                                 ClearCLContext pContext) {
    super(pProcessorName, pContext);
    mLightSheetMicroscope = pLightSheetMicroscope;
  }

  private float mMin;
  private float mMax;
  private long[] mHistogram;


  public float getMin()
  {
    return mMin;
  }

  public float getMax()
  {
    return mMax;
  }

  public long[] getHistogram()
  {
    return mHistogram;
  }

  @Override public StackInterface process(StackInterface pStack,
                                          RecyclerInterface<StackInterface, StackRequest> pStackRecycler)
  {
    RandomAccessibleInterval<ShortType>
        img = new StackToImgConverter(pStack).getRandomAccessibleInterval();

    ShortType lMinPixel = new ShortType();
    ShortType lMaxPixel = new ShortType();

    ComputeMinMax<ShortType> computeMinMax = new ComputeMinMax<ShortType>(Views.iterable(img), lMinPixel, lMaxPixel);
    computeMinMax.process();

    float lMin = lMinPixel.get();
    float lRange = lMaxPixel.get() - lMinPixel.get();
    Cursor<ShortType> cursor = Views.iterable(img).cursor();

    long[] lHistogram = new long[256];

    while( cursor.hasNext()) {
      lHistogram[(int)(((cursor.next()).get() - lMin) / lRange * 255)]++;
    }

    mMin = lMin;
    mMax = lMaxPixel.get();
    mHistogram = lHistogram;


    configureChart("Histogram",
                   "Histogram",
                   "Signal intensity",
                   "Pixel count",
                   ChartType.Line);

    for (int i = 0; i < mHistogram.length; i++ )
    {
      addPoint("Histogram",
               "Histogram",
               i == 0,
               mMin + i * lRange,
               mHistogram[i]);
    }


    return null;
  }
}
