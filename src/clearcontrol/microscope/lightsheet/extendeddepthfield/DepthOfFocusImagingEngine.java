package clearcontrol.microscope.lightsheet.extendeddepthfield;

import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import gnu.trove.list.array.TDoubleArrayList;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author haesleinhuepf
 *
 *
 */
public class DepthOfFocusImagingEngine extends TaskDevice implements
                                                          LoggingFeature,
                                                          VisualConsoleInterface
{




  private final LightSheetMicroscope mLightSheetMicroscope;


  private final Variable<String> mCalibrationDataName =
      new Variable<String>("TargetFolder",
                           "C:\temp");


  public DepthOfFocusImagingEngine(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("DepthOfFocusImagingEngine");
    mLightSheetMicroscope = pLightSheetMicroscope;
    this.setName("DepthOfFocusImagingEngine");
  }

  @Override
  public boolean startTask()
  {
    if (getLightSheetMicroscope().getCurrentTask().get() != null)
    {
      warning("Another task (%s) is already running, please stop it first.",
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
                          TimeoutException, IOException
  {
    if (isStopRequested())
      return false;/**/

    int lDetectionArmIndex = 0;
    int lLightSheetIndex = 0;

    int lNumberOfISamples = 10;
    int lNumberOfDSamples = 10;

    int lImageWidth = 512;
    int lImageHeight = 512;

    double lExposureTimeInSeconds = 1;

    String lFoldername = "C:/structure/temp/images/";
    String lDatasetname = "Test" + new Date();

    BoundedVariable<Number>
        lDetectionFocusZVariable =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(DetectionArmInterface.class,
                                            lDetectionArmIndex)
                                 .getZVariable();


    LightSheetInterface lLightSheetDevice =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(LightSheetInterface.class,
                                            lLightSheetIndex);


    BoundedVariable<Number> lZVariable =
        lLightSheetDevice.getZVariable();
    double lMinIZ = lZVariable.getMin().doubleValue();
    double lMaxIZ = lZVariable.getMax().doubleValue();

    double lStepIZ = (lMaxIZ - lMinIZ) / (lNumberOfISamples - 1);

    double lMinDZ = lDetectionFocusZVariable.getMin().doubleValue();
    double lMaxDZ = lDetectionFocusZVariable.getMax().doubleValue();

    double lStep = (lMaxDZ - lMinDZ) / (lNumberOfDSamples - 1);


    LightSheetMicroscopeQueue lQueue =
        getLightSheetMicroscope().requestQueue();

    // Initialize ----------------------------------------------------
    lQueue.clearQueue();
    // lQueue.zero();

    lQueue.setFullROI();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);
    lQueue.setExp(lExposureTimeInSeconds);

    // set to an initial state ---------------------------------------
    lQueue.setI(lLightSheetIndex);
    lQueue.setIX(lLightSheetIndex, 0);
    lQueue.setIY(lLightSheetIndex, 0);
    lQueue.setIZ(lLightSheetIndex, lMinDZ);

    //lQueue.setIH(mLightSheetIndex, 0);

    lQueue.setIZ(lLightSheetIndex, lMinDZ);
    lQueue.setDZ(lDetectionArmIndex /*?*/, lMinDZ);
    lQueue.setC(lDetectionArmIndex /*?*/, false);

    lQueue.addCurrentStateToQueue();

    // take images ---------------------------------------------------
    final TDoubleArrayList lDZList = new TDoubleArrayList();

    for (double lIZ = lMinIZ; lIZ <= lMaxDZ; lIZ += lStepIZ)
    {
      for (double z = lMinDZ; z <= lMaxDZ; z += lStep)
      {
        lDZList.add(z);

        lQueue.setDZ(lDetectionArmIndex, z);
        lQueue.setC(lDetectionArmIndex, true);

        lQueue.setIZ(lLightSheetIndex, lIZ);

        lQueue.addCurrentStateToQueue();
      }
    }
    lQueue.addVoxelDimMetaData(getLightSheetMicroscope(), 10);

    // clean up ------------------------------------------------------
    lQueue.setDZ(lDetectionArmIndex, lMinDZ);
    lQueue.setC(lDetectionArmIndex, false);

    lQueue.addCurrentStateToQueue();

    lQueue.setTransitionTime(0.1);

    lQueue.finalizeQueue();
    // ---------------------------------------------------------------

    // read out ------------------------------------------------------
    getLightSheetMicroscope().useRecycler("adaptation", 1, 4, 4);
    final Boolean lPlayQueueAndWait =
        getLightSheetMicroscope().playQueueAndWaitForStacks(lQueue,
                                                            100 + lQueue.getQueueLength(),
                                                            TimeUnit.SECONDS);

    if (lPlayQueueAndWait) {
      final OffHeapPlanarStack lStack =
          (OffHeapPlanarStack) getLightSheetMicroscope().getCameraStackVariable(
              lDetectionArmIndex)
                                                        .get();

      if (lStack != null) {
        //ContiguousMemoryInterface memory = lStack.getContiguousMemory();

       // long lWidth = lStack.getWidth();
       // long lHeight = lStack.getHeight();
       // long lDepth = lStack.getDepth();

        RawFileStackSink sink = new RawFileStackSink();
        sink.setLocation(new File(lFoldername), lDatasetname);
        sink.appendStack(lStack);

        sink.close();


      }
    }
    System.out.println("Bye.");
    return true;
  }

  public Variable<String> getCalibrationDataNameVariable()
  {
    return mCalibrationDataName;
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
