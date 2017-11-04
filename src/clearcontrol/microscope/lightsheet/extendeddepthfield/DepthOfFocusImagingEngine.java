package clearcontrol.microscope.lightsheet.extendeddepthfield;

import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.extendeddepthfield.iqm.ContrastEstimator;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * The DepthOfFocuseImagingEngine allows taking images with several
 * focus planes per light sheet position
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class DepthOfFocusImagingEngine extends TaskDevice implements
                                                          LoggingFeature,
                                                          VisualConsoleInterface
{
  private BoundedVariable<Integer> mDetectionArmIndex;
  private BoundedVariable<Integer> mLightSheetIndex;
  private BoundedVariable<Integer>
      mNumberOfISamples =
      new BoundedVariable<Integer>("Number of illumination samples",
                                   10,
                                   0,
                                   Integer.MAX_VALUE,
                                   1);
  private BoundedVariable<Integer>
      mNumberOfDSamples =
      new BoundedVariable<Integer>("Number of detection samples",
                                   10,
                                   0,
                                   Integer.MAX_VALUE,
                                   1);

  private BoundedVariable<Integer>
      mNumberOfPrecisionIncreasingIterations = new BoundedVariable<Integer>("Number of precision increasing iterations",
  5,
                                                                            0,Integer.MAX_VALUE,1);


  private final BoundedVariable<Double>
      mExposureVariableInSeconds =
      new BoundedVariable<Double>("Exposure time (s)",
                                  1.0,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.1);

  private final BoundedVariable<Double> mMinimumRange = new BoundedVariable<Double>("Minimum Range", 10.0, 0.0, Double.POSITIVE_INFINITY, 1.0);


  private Variable<String>
      mDataSetNamePostfixVariable =
      new Variable<String>("Test");
  private Variable<File>
      mRootFolderVariable =
      new Variable("RootFolder", (Object) null);


  class ImageRange{
    double lightSheetPosition;
    double[] detectionArmPositions;
  }


  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return mLightSheetIndex;
  }

  public BoundedVariable<Integer> getNumberOfISamples()
  {
    return mNumberOfISamples;
  }



  public BoundedVariable<Integer> getNumberOfDSamples()
  {
    return mNumberOfDSamples;
  }

  public BoundedVariable<Integer> getNumberOfPrecisionIncreasingIterations()
  {
    return mNumberOfPrecisionIncreasingIterations;
  }

  public BoundedVariable<Double> getExposureVariable()
  {
    return mExposureVariableInSeconds;
  }

  public BoundedVariable<Double> getMinimumRange()
  {
    return mMinimumRange;
  }

  public Variable<String> getDataSetNamePostfixVariable()
  {
    return mDataSetNamePostfixVariable;
  }

  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  private final LightSheetMicroscope mLightSheetMicroscope;

  public DepthOfFocusImagingEngine(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("DepthOfFocusImagingEngine");
    mLightSheetMicroscope = pLightSheetMicroscope;
    this.setName("ExtdDepthOfFocusImaging");

    mDetectionArmIndex =
        new BoundedVariable<Integer>("Detection arm",
                                     0,
                                     0,
                                     mLightSheetMicroscope.getNumberOfDetectionArms(),
                                     1);

    mLightSheetIndex =
        new BoundedVariable<Integer>("Light sheet",
                                     0,
                                     0,
                                     mLightSheetMicroscope.getNumberOfLightSheets(),
                                     1);
  }

  @Override public boolean startTask()
  {
    if (getLightSheetMicroscope().getCurrentTask().get() != null)
    {
      warning(
          "Another task (%s) is already running, please stop it first.",
          getLightSheetMicroscope().getCurrentTask());
      return false;
    }
    getLightSheetMicroscope().getCurrentTask().set(this);
    return super.startTask();
  }

  @Override public void run()
  {
    try
    {
      image();

    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    catch (ExecutionException e)
    {
      e.printStackTrace();
    }
    catch (TimeoutException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      getLightSheetMicroscope().getCurrentTask().set(null);
    }
  }

  private boolean image() throws
                          InterruptedException,
                          ExecutionException,
                          TimeoutException,
                          IOException
  {
    if (isStopRequested())
      return false;/**/

    int lDetectionArmIndex = mDetectionArmIndex.get();
    int lLightSheetIndex = mLightSheetIndex.get();

    int lNumberOfISamples = mNumberOfISamples.get();
    int lNumberOfDSamples = mNumberOfDSamples.get();
    int lNumberOfPrecisionIncreasingIterations = mNumberOfPrecisionIncreasingIterations.get();

    long
        lImageWidth = (Long)
        getLightSheetMicroscope().getDevice(StackCameraDeviceInterface.class,
                                            0).getStackWidthVariable().get();
    long
        lImageHeight =(Long)
        getLightSheetMicroscope().getDevice(StackCameraDeviceInterface.class,
                                            0).getStackHeightVariable().get();


    double lExposureTimeInSeconds = mExposureVariableInSeconds.get();

    //String lFoldername = "C:/structure/temp/images/";
    String
        lDatasetname =
        getDataSetNamePostfixVariable().get() + new SimpleDateFormat("yyyy.MM.dd HH-mm-ss").format(new Date());

    BoundedVariable<Number>
        lDetectionFocusZVariable =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(DetectionArmInterface.class,
                                            lDetectionArmIndex)
                                 .getZVariable();

    LightSheetInterface
        lLightSheetDevice =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(LightSheetInterface.class,
                                            lLightSheetIndex);

    BoundedVariable<Number>
        lZVariable =
        lLightSheetDevice.getZVariable();
    double lMinIZ = lZVariable.getMin().doubleValue();
    double lMaxIZ = lZVariable.getMax().doubleValue();
    double lStepIZ = (lMaxIZ - lMinIZ) / (lNumberOfISamples - 1);

    double lMinDZ = lDetectionFocusZVariable.getMin().doubleValue();
    double lMaxDZ = lDetectionFocusZVariable.getMax().doubleValue();

    //double lStep = (lMaxDZ - lMinDZ) / (lNumberOfDSamples - 1);

    RawFileStackSink sink = new RawFileStackSink();
    sink.setLocation(mRootFolderVariable.get(), lDatasetname);
    System.out.println(mRootFolderVariable.get() + lDatasetname);

    double lMinimumRange = mMinimumRange.get()

    // Initialize ----------------------------------------------------
    FocusableImager
        imager = new FocusableImager(getLightSheetMicroscope(), lLightSheetIndex, lDetectionArmIndex, lExposureTimeInSeconds);

    imager.setFieldOfView((int)lImageWidth, (int)lImageHeight);

    // take images ---------------------------------------------------
    final TDoubleArrayList lDZList = new TDoubleArrayList();


    ImageRange[] imageRanges = new ImageRange[lNumberOfISamples];


    int count = 0;
    for (double lIZ = lMinIZ; lIZ <= lMaxIZ + 0.0001; lIZ += lStepIZ)
    {
      ImageRange imageRange = new ImageRange();
      imageRange.lightSheetPosition = lIZ;
      imageRange.detectionArmPositions = new double[lNumberOfDSamples];

      double lStep = (lMaxDZ - lMinDZ) / (lNumberOfDSamples - 1);
      int dCount = 0;
      for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
      {
        imageRange.detectionArmPositions[dCount] = z;
        dCount ++;
      }

      imageRanges[count] = imageRange;
      count++;
    }

    OffHeapPlanarStack
        lStack = takeImages(imager, imageRanges);

    int iterationcount = 0;
    while (lStack != null)
    {
      sink.appendStack(lStack);


      ContrastEstimator contrastEstimator = new ContrastEstimator(lStack);
      double[] quality = contrastEstimator.getContrastPerSlice();

      ArrayList<Double> sortedLightSheetPositions = new ArrayList<Double>();
      HashMap<Double, Double> nextZPositions = new HashMap<Double, Double>();

      count = 0;
      for (ImageRange imageRange : imageRanges)
      {
        double maxQuality = quality[count];
        double bestDetectionZ = 0;

        for (double z : imageRange.detectionArmPositions)
        {
          System.out.println("" + imageRange.lightSheetPosition + "\t" + z + "\t" + quality[count]);
          if (maxQuality < quality[count]) {
            maxQuality = quality[count];
            bestDetectionZ = z;
          }
          count++;
        }

        double oldRange = imageRange.detectionArmPositions[lNumberOfDSamples - 1] - imageRange.detectionArmPositions[0];
        double newRange = oldRange / 2;
        if (newRange < lMinimumRange) {
          newRange = lMinimumRange;
        }

        double zStep = newRange / (lNumberOfDSamples - 1);

        int dCount = 0;
        for (double z = bestDetectionZ - newRange / 2; z <= bestDetectionZ + newRange / 2; z += zStep) {
          imageRange.detectionArmPositions[dCount] = Math.max(Math.min(z, lMaxDZ), lMinDZ);
          dCount++;
        }
      }





      // take new images
      FocusableImager preciseImager = new FocusableImager(getLightSheetMicroscope(), lLightSheetIndex, lDetectionArmIndex, lExposureTimeInSeconds);
      preciseImager.setFieldOfView((int)lImageWidth, (int)lImageHeight);

      /*
      for (Double lightsheetZ : sortedLightSheetPositions) {
        double detectionZ = nextZPositions.get(lightsheetZ);
        double radius = 10;
        double step = 0.5;

        for (double z = detectionZ - radius; z <= detectionZ + radius; z += step) {
          preciseImager.addImageRequest(lightsheetZ, z);
        }

      }

      OffHeapPlanarStack stack = preciseImager.execute();
      */

      //sink.appendStack(lStack);
      iterationcount  ++;
      if (iterationcount > lNumberOfPrecisionIncreasingIterations) {

        break;
      }

      lStack = takeImages(preciseImager, imageRanges);
    }

    sink.close();
    System.out.println("Bye.");
    return true;
  }

  private OffHeapPlanarStack takeImages(FocusableImager imager, ImageRange[] imageRanges) throws
                                                                                          InterruptedException,
                                                                                          ExecutionException,
                                                                                          TimeoutException
  {
    int count = 0;
    for (ImageRange imageRange : imageRanges)
    {
      System.out.println(count);
      count++;

      for (double z : imageRange.detectionArmPositions) {
        imager.addImageRequest(imageRange.lightSheetPosition, z);
      }
    }
    // save result ---------------------------------------------------

    final OffHeapPlanarStack
        lStack = imager.execute();
    return lStack;
  }

  /**
   * Returns a lightsheet microscope
   *
   * @return lightsheet microscope
   */
  public LightSheetMicroscope getLightSheetMicroscope()
  {
    return mLightSheetMicroscope;
  }

  /**
   * Returns true if calibration should be stopped immediately.
   *
   * @return true for stopping, false otherwise.
   */
  public boolean isStopRequested()
  {
    return ScriptingEngine.isCancelRequestedStatic()
           || getStopSignalVariable().get();
  }
}
