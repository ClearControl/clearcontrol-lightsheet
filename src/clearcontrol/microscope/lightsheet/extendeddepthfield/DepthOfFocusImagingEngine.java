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

import java.io.*;
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
//  private BoundedVariable<Integer> mLightSheetIndex;
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
                                  0.1,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.1);

  private final BoundedVariable<Double> mMinimumRange = new BoundedVariable<Double>("Minimum Range", 10.0, 0.0, Double.POSITIVE_INFINITY, 1.0);
  private BufferedWriter logFileStream;

  private Variable<String>
      mDataSetNamePostfixVariable =
      new Variable<String>("Test");
  private Variable<File>
      mRootFolderVariable =
      new Variable("RootFolder", (Object) null);

  private Variable<Boolean> mDetectionArmFixedVariable = new Variable<Boolean>("DetectionArmFixed", true);

  class ImageRange{
    double fixedPosition;
    double[] movingPositions;
  }


  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return mDetectionArmIndex;
  }
//
//  public BoundedVariable<Integer> getLightSheetIndex()
//  {
//    return mLightSheetIndex;
//  }

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

  public Variable<Boolean> getDetectionArmFixedVariable() {
    return mDetectionArmFixedVariable;
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

//    mLightSheetIndex =
//        new BoundedVariable<Integer>("Light sheet",
//                                     0,
//                                     0,
//                                     mLightSheetMicroscope.getNumberOfLightSheets(),
//                                     1);
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
//    int lLightSheetIndex = mLightSheetIndex.get();


    int lNumberOfMovingSamples = mNumberOfISamples.get();
    int lNumberOfFixedSamples = mNumberOfDSamples.get();
    if (!mDetectionArmFixedVariable.get()) {
      lNumberOfMovingSamples = mNumberOfDSamples.get();
      lNumberOfFixedSamples = mNumberOfISamples.get();
    }
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
        lDatasetname = "" +
         new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-00-").format(new Date()) + getDataSetNamePostfixVariable().get();

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


    double lMinMovingZ = lLightsheetZVariable.getMin().doubleValue();
    double lMaxMovingZ = lLightsheetZVariable.getMax().doubleValue();

    double lMinFixedZ = lDetectionFocusZVariable.getMin().doubleValue();
    double lMaxFixedZ = lDetectionFocusZVariable.getMax().doubleValue();

    if (!mDetectionArmFixedVariable.get())
    {
      lMinMovingZ = lDetectionFocusZVariable.getMin().doubleValue();
      lMaxMovingZ = lDetectionFocusZVariable.getMax().doubleValue();

      lMinFixedZ = lLightsheetZVariable.getMin().doubleValue();
      lMaxFixedZ = lLightsheetZVariable.getMax().doubleValue();
    }
    //double lStep = (lMaxDZ - lMinDZ) / (lNumberOfDSamples - 1);

    RawFileStackSink sink = new RawFileStackSink();
    sink.setLocation(mRootFolderVariable.get(), lDatasetname);
    File logFile = new File(mRootFolderVariable.get() + "\\" + lDatasetname, "log.txt");

    logFile.getParentFile().mkdir();

    logFileStream = new BufferedWriter(new FileWriter(logFile));

    logFileStream.write("Start " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date()) + "\n" );
    System.out.println(mRootFolderVariable.get() + lDatasetname);

    double lMinimumRange = mMinimumRange.get();

    // Initialize ----------------------------------------------------
    FocusableImager
        imager = new FocusableImager(getLightSheetMicroscope(), 0, 3, lDetectionArmIndex, lExposureTimeInSeconds);

    imager.setFieldOfView((int)lImageWidth, (int)lImageHeight);

    // take images ---------------------------------------------------
    final TDoubleArrayList lDZList = new TDoubleArrayList();


    ImageRange[] imageRanges = new ImageRange[lNumberOfMovingSamples];


    double lStepMovingZ = (lMaxMovingZ - lMinMovingZ) / (lNumberOfMovingSamples - 1);
    double lStepFixedZ = (lMaxFixedZ - lMinFixedZ) / (lNumberOfFixedSamples - 1);

    int count = 0;
    for (double lFixedZ = lMinFixedZ; lFixedZ <= lMaxFixedZ + 0.0001; lFixedZ += lStepFixedZ)
    {
      ImageRange imageRange = new ImageRange();
      imageRange.fixedPosition = lFixedZ;
      imageRange.movingPositions = new double[lNumberOfMovingSamples];

      int dCount = 0;
      for (double lMovingZ = lMinMovingZ; lMovingZ <= lMaxMovingZ + 0.00001; lMovingZ += lStepMovingZ)
      {
        imageRange.movingPositions[dCount] = lMovingZ;
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
        double bestMovingZ = 0;

        for (double lMovingZ : imageRange.movingPositions)
        {
          System.out.println("" + imageRange.fixedPosition
                             + "\t" + lMovingZ + "\t" + quality[count]);
          logFileStream.write("Quality " + imageRange.fixedPosition
                              + "\t" + lMovingZ + "\t" + quality[count] + "\n");
          if (maxQuality < quality[count]) {
            maxQuality = quality[count];
            bestMovingZ = lMovingZ;
          }
          count++;
        }
        logFileStream.write("Best moving position " + bestMovingZ + "\n");

        double oldRange = imageRange.movingPositions[lNumberOfMovingSamples - 1] - imageRange.movingPositions[0];
        double newRange = oldRange / 2;
        if (newRange < lMinimumRange) {
          newRange = lMinimumRange;
        }

        lStepMovingZ = newRange / (lNumberOfMovingSamples - 1);

        logFileStream.write("Fixed " + imageRange.fixedPosition + " new moving range " + (bestMovingZ - newRange / 2) + " - " + bestMovingZ + newRange / 2 + "\n");

        int dCount = 0;
        for (double lMovingZ = bestMovingZ - newRange / 2; lMovingZ <= bestMovingZ + newRange / 2 + 0.0001; lMovingZ += lStepMovingZ) {
          imageRange.movingPositions[dCount] = Math.max(Math.min(lMovingZ, lMaxMovingZ), lMinMovingZ);
          dCount++;
        }
      }



      logFileStream.write("ITERATION " + iterationcount + "\n");


      // take new images
      FocusableImager preciseImager = new FocusableImager(getLightSheetMicroscope(), 0, 3, lDetectionArmIndex, lExposureTimeInSeconds);
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
    logFileStream.close();
    System.out.println("Bye.");
    return true;
  }

  private OffHeapPlanarStack takeImages(FocusableImager imager, ImageRange[] imageRanges) throws
                                                                                          InterruptedException,
                                                                                          ExecutionException,
                                                                                          TimeoutException,
                                                                                          IOException
  {

    int count = 0;
    for (ImageRange imageRange : imageRanges)
    {
      System.out.println(count);
      count++;

      for (double movingPosition : imageRange.movingPositions) {
        if (mDetectionArmFixedVariable.get()) {
          logFileStream.write("Image at " + imageRange.fixedPosition + "/" + movingPosition + "\n");
          imager.addImageRequest(imageRange.fixedPosition, movingPosition);
        } else {
          logFileStream.write("Image at " + movingPosition + "/" + imageRange.fixedPosition + "\n");
          imager.addImageRequest(movingPosition, imageRange.fixedPosition);
        }
      }
    }
    // save result ---------------------------------------------------

    logFileStream.write("Start imaging " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())  + "\n");
    final OffHeapPlanarStack
        lStack = imager.execute();
    logFileStream.write("Stop imaging " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())  + "\n");
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
