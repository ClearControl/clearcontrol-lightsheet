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

  private final BoundedVariable<Double>
      mExposureVariableInSeconds =
      new BoundedVariable<Double>("Exposure time (s)",
                                  1.0,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.0);

  private Variable<String>
      mDataSetNamePostfixVariable =
      new Variable<String>("Test");
  private Variable<File>
      mRootFolderVariable =
      new Variable("RootFolder", (Object) null);

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

  public BoundedVariable<Double> getExposureVariable()
  {
    return mExposureVariableInSeconds;
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

    double lStep = (lMaxDZ - lMinDZ) / (lNumberOfDSamples - 1);

    RawFileStackSink sink = new RawFileStackSink();
    sink.setLocation(mRootFolderVariable.get(), lDatasetname);
    System.out.println(mRootFolderVariable.get() + lDatasetname);


    // Initialize ----------------------------------------------------
    FocusableImager
        imager = new FocusableImager(getLightSheetMicroscope(), lLightSheetIndex, lDetectionArmIndex, lExposureTimeInSeconds);

    imager.setFieldOfView((int)lImageWidth, (int)lImageHeight);

    // take images ---------------------------------------------------
    final TDoubleArrayList lDZList = new TDoubleArrayList();

    for (double lIZ = lMinIZ; lIZ <= lMaxDZ; lIZ += lStepIZ)
    {
      for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
      {
        lDZList.add(z);
        imager.addImageRequest(lIZ, z);
      }
    }
    // save result ---------------------------------------------------

    final OffHeapPlanarStack
          lStack = imager.execute();

    if (lStack != null)
    {
      sink.appendStack(lStack);

      ContrastEstimator contrastEstimator = new ContrastEstimator(lStack);
      double[] quality = contrastEstimator.getSignalRangeSize();

      ArrayList<Double> sortedLightSheetPositions = new ArrayList<Double>();
      HashMap<Double, Double> nextZPositions = new HashMap<Double, Double>();

      int count = 0;
      for (double lIZ = lMinIZ; lIZ <= lMaxDZ; lIZ += lStepIZ)
      {
        double maxRange = quality[count];
        double bestDetectionZ = 0;
        for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
        {
          System.out.println("" + lIZ + "\t" + z + "\t" + quality[count]);
          if (maxRange < quality[count]) {
            maxRange = quality[count];
            bestDetectionZ = z;
          }
          count++;
        }
        sortedLightSheetPositions.add(lIZ);
        nextZPositions.put(lIZ, bestDetectionZ);
      }



      // take new images

      FocusableImager preciseImager = new FocusableImager(getLightSheetMicroscope(), lLightSheetIndex, lDetectionArmIndex, lExposureTimeInSeconds);
      preciseImager.setFieldOfView((int)lImageWidth, (int)lImageHeight);

      for (Double lightsheetZ : sortedLightSheetPositions) {
        double detectionZ = nextZPositions.get(lightsheetZ);
        double radius = 10;
        double step = 0.5;



        for (double z = detectionZ - radius; z <= detectionZ + radius; z += step) {
          preciseImager.addImageRequest(lightsheetZ, z);
        }

      }

      OffHeapPlanarStack stack = preciseImager.execute();


      sink.appendStack(stack);

    }

    sink.close();
    System.out.println("Bye.");
    return true;
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
