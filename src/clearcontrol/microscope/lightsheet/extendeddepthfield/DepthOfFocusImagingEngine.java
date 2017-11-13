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
import clearcontrol.microscope.lightsheet.extendeddepthfield.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * The DepthOfFocusImagingEngine allows taking images with several
 * focus planes per light sheet position and vice versa.
 *
 * Initially, n light sheet positions are imaged; with m detection arm
 * positions each. Afterwards, within the m images, the one with the
 * highest quality measurement is defined as the one in focus. In the
 * following step, again m detection arm positons are imaged, but the
 * range becomes smaller. After 3-4 iterations, the minimum range
 * should be reached. Adaption according to the detection arm position
 * in focus is done at every single step.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * October 2017
 */
public class DepthOfFocusImagingEngine extends TaskDevice implements
                                                          LoggingFeature,
                                                          VisualConsoleInterface
{
  private final static double sDoubleTolerance = 0.0001;

  private BoundedVariable<Integer> mDetectionArmIndex;
  private BoundedVariable<Integer> mLightSheetMinIndex;
  private BoundedVariable<Integer> mLightSheetMaxIndex;



  //  private BoundedVariable<Integer> mLightSheetIndex;
  private BoundedVariable<Integer>
      mNumberOfISamples =
      new BoundedVariable<Integer>("Number of illumination samples",
                                   100,
                                   0,
                                   Integer.MAX_VALUE,
                                   1);
  private BoundedVariable<Integer>
      mNumberOfDSamples =
      new BoundedVariable<Integer>("Number of detection samples",
                                   11,
                                   0,
                                   Integer.MAX_VALUE,
                                   1);

  private BoundedVariable<Integer>
      mNumberOfPrecisionIncreasingIterations =
      new BoundedVariable<Integer>(
          "Number of precision increasing iterations",
          5,
          0,
          Integer.MAX_VALUE,
          1);

  private final BoundedVariable<Double>
      mExposureVariableInSeconds =
      new BoundedVariable<Double>("Exposure time (s)",
                                  0.1,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.001);

  private final BoundedVariable<Double>
      mMinimumRange =
      new BoundedVariable<Double>("Minimum Range",
                                  10.0,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  1.0);
  private BufferedWriter mLogFileWriter;

  private Variable<String>
      mDataSetNamePostfixVariable =
      new Variable<String>("Test");
  private Variable<File>
      mRootFolderVariable =
      new Variable("RootFolder", (Object) null);

  private Variable<Boolean>
      mDetectionArmFixedVariable =
      new Variable<Boolean>("DetectionArmFixed", false);

  class ImageRange
  {
    double mFixedPosition;
    double[] mMovingPositions;
  }

  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }


  public BoundedVariable<Integer> getLightSheetMinIndex()
  {
    return mLightSheetMinIndex;
  }


  public BoundedVariable<Integer> getLightSheetMaxIndex()
  {
    return mLightSheetMaxIndex;
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

  public Variable<Boolean> getDetectionArmFixedVariable()
  {
    return mDetectionArmFixedVariable;
  }

  private final LightSheetMicroscope mLightSheetMicroscope;

  public DepthOfFocusImagingEngine(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("EDF Imaging");
    mLightSheetMicroscope = pLightSheetMicroscope;
    this.setName("EDF_Imaging");

    mDetectionArmIndex =
        new BoundedVariable<Integer>("Detection arm",
                                     0,
                                     0,
                                     mLightSheetMicroscope.getNumberOfDetectionArms(),
                                     1);

    mLightSheetMinIndex =
        new BoundedVariable<Integer>("Light sheet start",
                                     0,
                                     0,
                                     mLightSheetMicroscope.getNumberOfDetectionArms(),
                                     1);

    mLightSheetMaxIndex =
        new BoundedVariable<Integer>("Light sheet end",
                                     mLightSheetMicroscope.getNumberOfLightSheets(),
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
      return false;

    int lDetectionArmIndex = mDetectionArmIndex.get();
    int lLightSheetMinIndex = mLightSheetMinIndex.get();
    int lLightSheetMaxIndex = mLightSheetMaxIndex.get();

    int lNumberOfMovingSamples = mNumberOfISamples.get();
    int lNumberOfFixedSamples = mNumberOfDSamples.get();
    if (!mDetectionArmFixedVariable.get())
    {
      lNumberOfMovingSamples = mNumberOfDSamples.get();
      lNumberOfFixedSamples = mNumberOfISamples.get();
    }
    int
        lNumberOfPrecisionIncreasingIterations =
        mNumberOfPrecisionIncreasingIterations.get();

    long
        lImageWidth =
        (Long) getLightSheetMicroscope().getDevice(
            StackCameraDeviceInterface.class,
            0).getStackWidthVariable().get();
    long
        lImageHeight =
        (Long) getLightSheetMicroscope().getDevice(
            StackCameraDeviceInterface.class,
            0).getStackHeightVariable().get();

    double lExposureTimeInSeconds = mExposureVariableInSeconds.get();

    String
        lDatasetname =
        "" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-00-").format(
            new Date()) + getDataSetNamePostfixVariable().get();

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
                                            0);

    BoundedVariable<Number>
        lLightsheetZVariable =
        lLightSheetDevice.getZVariable();


    // Open Image Sink and log file
    RawFileStackSink lSink = new RawFileStackSink();
    lSink.setLocation(mRootFolderVariable.get(), lDatasetname);
    File
        lLogFile =
        new File(mRootFolderVariable.get() + "\\" + lDatasetname,
                 "log.txt");

    lLogFile.getParentFile().mkdir();

    mLogFileWriter = new BufferedWriter(new FileWriter(lLogFile));

    System.out.println(mRootFolderVariable.get() + lDatasetname);

    // define ranges where to take images
    double lMinMovingZ = lLightsheetZVariable.getMin().doubleValue();
    double lMaxMovingZ = lLightsheetZVariable.getMax().doubleValue();

    double
        lMinFixedZ =
        lDetectionFocusZVariable.getMin().doubleValue();
    double
        lMaxFixedZ =
        lDetectionFocusZVariable.getMax().doubleValue();

    if (!mDetectionArmFixedVariable.get())
    {
      lMinMovingZ = lDetectionFocusZVariable.getMin().doubleValue();
      lMaxMovingZ = lDetectionFocusZVariable.getMax().doubleValue();

      lMinFixedZ = lLightsheetZVariable.getMin().doubleValue();
      lMaxFixedZ = lLightsheetZVariable.getMax().doubleValue();
    }

    double lMinimumRange = mMinimumRange.get();

    ImageRange[]
        lImageRanges =
        new ImageRange[lNumberOfFixedSamples];

    double
        lStepMovingZ =
        (lMaxMovingZ - lMinMovingZ) / (lNumberOfMovingSamples - 1);
    double
        lStepFixedZ =
        (lMaxFixedZ - lMinFixedZ) / (lNumberOfFixedSamples - 1);

    int lQualitySampleIndex = 0;

    mLogFileWriter.write("Dataset " + mRootFolderVariable.get() + "\\" + lDatasetname);
    mLogFileWriter.write("Detection arm index: " + lDetectionArmIndex );
    mLogFileWriter.write("Number of " + (mDetectionArmFixedVariable.get()?"detection arm":"light sheet") + " samples: " + lNumberOfFixedSamples);
    mLogFileWriter.write("Number of " + (!mDetectionArmFixedVariable.get()?"detection arm":"light sheet") + " samples: " + lNumberOfMovingSamples);
    mLogFileWriter.write("Detection arm fixed: " + mDetectionArmFixedVariable.get());
    mLogFileWriter.write("Number of iterations: " + lNumberOfPrecisionIncreasingIterations);
    mLogFileWriter.write("Minimum range: " + lMinimumRange);
    mLogFileWriter.write("Minimum fixed Z: " + lMinFixedZ);
    mLogFileWriter.write("Maximum fixed Z: " + lMaxFixedZ);
    mLogFileWriter.write("Minimum moving Z: " + lMinMovingZ);
    mLogFileWriter.write("Maximum moving Z: " + lMaxMovingZ);

    mLogFileWriter.write("Image width: " + lImageWidth);
    mLogFileWriter.write("Image height: " + lImageHeight);
    mLogFileWriter.write("Exposure time (sec): " + lExposureTimeInSeconds);

    mLogFileWriter.write("Start " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date()) + "\n");


    // setup imager
    FocusableImager
        lInitialImager =
        new FocusableImager(getLightSheetMicroscope(),
                            lLightSheetMinIndex,
                            lLightSheetMaxIndex,
                            lDetectionArmIndex,
                            lExposureTimeInSeconds);

    lInitialImager.setFieldOfView((int) lImageWidth,
                                  (int) lImageHeight);


    // define ImageRange: For all detection arm positions take
    // images for all light sheet positions (or vise versa)
    for (double lFixedZ = lMinFixedZ; lFixedZ <= lMaxFixedZ + 0.0001;
         lFixedZ +=
             lStepFixedZ)
    {
      ImageRange imageRange = new ImageRange();
      imageRange.mFixedPosition = lFixedZ;
      imageRange.mMovingPositions =
          new double[lNumberOfMovingSamples];

      int lMovingCount = 0;
      for (double lMovingZ = lMinMovingZ; lMovingZ
                                          <= lMaxMovingZ + 0.00001;
           lMovingZ +=
               lStepMovingZ)
      {
        imageRange.mMovingPositions[lMovingCount] = lMovingZ;
        lMovingCount++;
      }

      lImageRanges[lQualitySampleIndex] = imageRange;
      lQualitySampleIndex++;
    }

    // Get the first image stack for analysis / optimisation
    OffHeapPlanarStack
        lStack =
        takeImages(lInitialImager, lImageRanges);

    int lIterationCount = 0;
    while (lStack != null)
    {
      // save result images to disc
      lSink.appendStack(lStack);

      // Analyse images: get quality per slice
      DiscreteConsinusTransformEntropyPerSliceEstimator
          lImageQualityEstimator =
          new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
      double[]
          lQualityPerSliceMeasurementsArray =
          lImageQualityEstimator.getQualityArray();


      lQualitySampleIndex = 0;
      for (ImageRange lImageRange : lImageRanges)
      {
        double
            lMaxQuality =
            lQualityPerSliceMeasurementsArray[lQualitySampleIndex];
        double lBestMovingZ = 0;


        mLogFileWriter.write("Start analysing quality " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

        // determine moving slice in focus
        for (double lMovingZ : lImageRange.mMovingPositions)
        {
          info(""
               + lImageRange.mFixedPosition
               + "\t"
               + lMovingZ
               + "\t"
               + lQualityPerSliceMeasurementsArray[lQualitySampleIndex]);
          mLogFileWriter.write("Quality[" + lQualitySampleIndex + "] "
                               + lImageRange.mFixedPosition
                               + "\t"
                               + lMovingZ
                               + "\t"
                               + lQualityPerSliceMeasurementsArray[lQualitySampleIndex]
                               + "\n");
          if (lMaxQuality
              < lQualityPerSliceMeasurementsArray[lQualitySampleIndex])
          {
            lMaxQuality =
                lQualityPerSliceMeasurementsArray[lQualitySampleIndex];
            lBestMovingZ = lMovingZ;
          }
          lQualitySampleIndex++;
        }

        mLogFileWriter.write("Finished analysing quality " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
        mLogFileWriter.write("Best moving position "
                             + lBestMovingZ
                             + "\n");

        // setup a new range for this particular fixed slice
        double
            lOldRange =
            lImageRange.mMovingPositions[lNumberOfMovingSamples - 1]
            - lImageRange.mMovingPositions[0];
        double lNewRange = lOldRange / 2;
        if (lNewRange < lMinimumRange)
        {
          lNewRange = lMinimumRange;
        }

        lStepMovingZ = lNewRange / (lNumberOfMovingSamples - 1);

        mLogFileWriter.write("Fixed "
                             + lImageRange.mFixedPosition
                             + " new moving range "
                             + (lBestMovingZ - lNewRange / 2)
                             + " - "
                             + lBestMovingZ
                             + lNewRange / 2
                             + "\n");

        int lMovingCount = 0;
        for (double lMovingZ = lBestMovingZ - lNewRange / 2; lMovingZ
                                                             <=
                                                             lBestMovingZ
                                                             +
                                                             lNewRange
                                                             / 2
                                                             + sDoubleTolerance;
             lMovingZ +=
                 lStepMovingZ)
        {
          lImageRange.mMovingPositions[lMovingCount] =
              Math.max(Math.min(lMovingZ, lMaxMovingZ), lMinMovingZ);
          lMovingCount++;
        }
      }

      mLogFileWriter.write("ITERATION " + lIterationCount + "\n");

      // Cancel if user wants it or if iterations exceeded
      lIterationCount++;
      if (lIterationCount > lNumberOfPrecisionIncreasingIterations)
      {
        break;
      }
      if (isStopRequested()) {
        warning("Cancelled by user");
        break;
      }

      // take new images
      FocusableImager
          lPreciseImager =
          new FocusableImager(getLightSheetMicroscope(),
                              lLightSheetMinIndex,
                              lLightSheetMaxIndex,
                              lDetectionArmIndex,
                              lExposureTimeInSeconds);
      lPreciseImager.setFieldOfView((int) lImageWidth,
                                    (int) lImageHeight);

      lStack = takeImages(lPreciseImager, lImageRanges);
    }

    lSink.close();
    mLogFileWriter.close();
    info("Bye.");
    return true;
  }

  private OffHeapPlanarStack takeImages(FocusableImager pImager,
                                        ImageRange[] pImageRanges) throws
                                                                   InterruptedException,
                                                                   ExecutionException,
                                                                   TimeoutException,
                                                                   IOException
  {
    // Request for all images defined in the ImageRange
    for (ImageRange lImageRange : pImageRanges)
    {
      for (double lMovingPosition : lImageRange.mMovingPositions)
      {
        if (mDetectionArmFixedVariable.get())
        {
          mLogFileWriter.write("Image at "
                               + lMovingPosition
                               + "/"
                               + lImageRange.mFixedPosition
                               + "\n");
          pImager.addImageRequest(lMovingPosition,
                                  lImageRange.mFixedPosition);
        }
        else
        {
          mLogFileWriter.write("Image at "
                               + lImageRange.mFixedPosition
                               + "/"
                               + lMovingPosition
                               + "\n");
          pImager.addImageRequest(lImageRange.mFixedPosition,
                                  lMovingPosition);
        }
      }
    }

    // Actually run the imaging here
    mLogFileWriter.write("Start imaging " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
    final OffHeapPlanarStack lStack = pImager.execute();
    mLogFileWriter.write("Finished imaging " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
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
