package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.math.argmax.SmartArgMaxFinder;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import ij.IJ;
import ij.gui.Plot;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This scheduler initializes focus alpha (illumination alpha). It
 * should be used if the focus is really off.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FocusFinderAlphaByVariationScheduler extends
                                                  SchedulerBase implements
                                                                SchedulerInterface,
                                                                LoggingFeature
{
  private LightSheetMicroscope mLightSheetMicroscope;
  private int mControlPlaneIndex;
  private int mLightSheetIndex;
  private int mDetectionArmIndex;

  private BoundedVariable<Double>
      mExposureTimeInSecondsVariable = new BoundedVariable<Double>("exp", 0.05, 0.0000001, Double.MAX_VALUE, 0.0000001);
  private BoundedVariable<Integer> mImageWidthVariable = new BoundedVariable<Integer>("image width", 2048, 16, 2048, 1);
  private BoundedVariable<Integer> mImageHeightVariable = new BoundedVariable<Integer>("image height", 2048, 16, 2048, 1);
  private BoundedVariable<Integer> mNumberOfImagesToTakeVariable = new BoundedVariable<Integer>("number of samples", 25, 3, 1000, 1);
  private BoundedVariable<Double>
      mAlphaStepVariable = new BoundedVariable<Double>("alphaRange", 0.5, 0.01, Double.MAX_VALUE, 0.01);


  public static boolean debug = false;



  public FocusFinderAlphaByVariationScheduler(int pLightSheetIndex, int pDetectionArmIndex, int pControlPlaneIndex) {
    super("Adaptation: Focus finder alpha for C" + pDetectionArmIndex + "L" + pLightSheetIndex + "CPI" + pControlPlaneIndex);
    mControlPlaneIndex = pControlPlaneIndex;
    mLightSheetIndex = pLightSheetIndex;
    mDetectionArmIndex = pDetectionArmIndex;
  }


  @Override public boolean initialize()
  {
    if (mMicroscope instanceof LightSheetMicroscope){
      mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    }
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState
        lAcquisitionState =
        (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager()
                                                            .getCurrentState();

    int cpi = mControlPlaneIndex;
    int lLightSheetIndex = mLightSheetIndex;
    int lCameraIdx = mDetectionArmIndex;

    info("Control plane " + cpi);
    double lCPPositionZ = lAcquisitionState.getControlPlaneZ(cpi);
    info("Z position " + lCPPositionZ);

    //low Z values are on the back of XWing at camera 1
    //high Z values are better visible in camera 0 on the front


    double alpha = determineBestDeltaAlphaAtZ(lCPPositionZ, lLightSheetIndex, lCameraIdx);
    info("determined best alpha " + alpha);

    double lIZ = lAcquisitionState.getInterpolationTables().get(
        LightSheetDOF.IA, cpi, lLightSheetIndex);
    info("old alpha " + lIZ);

    lAcquisitionState.getInterpolationTables().set(LightSheetDOF.IA, cpi, lLightSheetIndex, alpha);


    LightSheetMicroscopeQueue lQueue = mLightSheetMicroscope.requestQueue();
    System.out.println("Z1: " + lCPPositionZ);
    lAcquisitionState.applyAcquisitionStateAtZ(lQueue, lCPPositionZ);
    System.out.println("Z1: " + lQueue.getIZ(lLightSheetIndex));

    return false;
  }

  private double determineBestDeltaAlphaAtZ(double lCPPositionZ, int lLightSheetIndex, int lCameraIdx) {

    double lIlluminationAlphaStep = mAlphaStepVariable.get();
    int lNumberOfImagesToTake = mNumberOfImagesToTakeVariable.get();
    int lImageWidth = mImageWidthVariable.get();
    int lImageHeight = mImageHeightVariable.get();
    double lExposureTimeInSeconds = mExposureTimeInSecondsVariable.get();

    int lDetectionArmIndex = lCameraIdx;
    int lIlluminationArmIndex = lLightSheetIndex;

    double lIlluminationZStart = lCPPositionZ;
    double lDetectionZZStart = lCPPositionZ; //lIlluminationZStart - (lDetectionZStep * (lNumberOfImagesToTake-1) / 2);
    double lAlphaStart = -lIlluminationAlphaStep * (lNumberOfImagesToTake-1) / 2;

    LightSheetMicroscope lLightsheetMicroscope = mLightSheetMicroscope;

    // build a queue
    LightSheetMicroscopeQueue lQueue =  mLightSheetMicroscope.requestQueue();

    InterpolatedAcquisitionState lCurrentState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();



    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(lLightsheetMicroscope, lQueue, lIlluminationZStart, lDetectionArmIndex, lDetectionZZStart);

    // Do a break of three seconds before imaging
    // lQueue.setExp(3);
    // lQueue.addCurrentStateToQueue();


    // --------------------------------------------------------------------
    // build a queue
    double[] lXAxis = new double[lNumberOfImagesToTake];

    for (int illuminationAlphaIndex = 0; illuminationAlphaIndex < lNumberOfImagesToTake; illuminationAlphaIndex++) {
      //#print(" i" + str(lIlluminationZStart))
      //#print(" d" + str(lDetectionZZStart + illuminationZIndex * lDetectionZStep))
      lCurrentState.applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);
      // configure light sheets accordingly
      for (int k = 0; k
                      < lLightsheetMicroscope.getNumberOfLightSheets(); k++)
      {
        lQueue.setI(k, k == mLightSheetIndex);
      }
      //lQueue.setIW(lIlluminationArmIndex, 0.45);

      //lQueue.setI(lIlluminationArmIndex, true);
      //lQueue.setIZ(lIlluminationArmIndex, lIlluminationZStart);

      //double lPositionZ = lDetectionZZStart + illuminationZIndex * lDetectionZStep;
      //lQueue.setDZ(lDetectionArmIndex, lDetectionZZStart);

      double lPositionAlpha = lAlphaStart + illuminationAlphaIndex * lIlluminationAlphaStep;
      lQueue.setIA(lIlluminationArmIndex, lPositionAlpha);

      lQueue.setC(lDetectionArmIndex, true);
      lQueue.setExp(lExposureTimeInSeconds);
      lQueue.addCurrentStateToQueue();


      lQueue.setTransitionTime(0.5);
      lQueue.setFinalisationTime(0.005);

      lXAxis[illuminationAlphaIndex] = lPositionAlpha;
    }

    goToInitialPosition(lLightsheetMicroscope, lQueue, lIlluminationZStart, lDetectionArmIndex, lDetectionZZStart);

    lQueue.finalizeQueue();

    // --------------------------------------------------------------------
    // Laser on!

    // Thread.sleep(20000);

    // acquire!
    boolean lPlayQueueAndWait = false;
    try {
      lPlayQueueAndWait = lLightsheetMicroscope.playQueueAndWaitForStacks(lQueue, 10000 + lQueue.getQueueLength(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
    if (!lPlayQueueAndWait) {
      warning("Error while imaging");
    }

    StackInterface
        lStack = lLightsheetMicroscope.getCameraStackVariable(lDetectionArmIndex).get();

    // measure quality
    DiscreteConsinusTransformEntropyPerSliceEstimator
        lImageQualityEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
    double[] lQualityPerSliceMeasurementsArray = lImageQualityEstimator.getQualityArray();

    if (debug)
    {
      info("arr " + Arrays.toString(lQualityPerSliceMeasurementsArray));
      Plot
          plot =
          new Plot("title",
                   "quality",
                   "alpha",
                   lXAxis,
                   lQualityPerSliceMeasurementsArray);
      plot.show();
    }
    SmartArgMaxFinder lSmartArgMaxFinder = new SmartArgMaxFinder();
    double lArgMaxQualityDeltaZ = lSmartArgMaxFinder.argmax(lXAxis, lQualityPerSliceMeasurementsArray);


    if (debug)
    {
      info("bef " + lQualityPerSliceMeasurementsArray[
          lNumberOfImagesToTake
          / 2]);
      info("max " + lArgMaxQualityDeltaZ);
      info("arg " + lArgMaxQualityDeltaZ);

      // --------------------------------------------------------------------
      // convert and show
      //RandomAccessibleInterval imgStack = EasyScopyUtilities.stackToImg((OffHeapPlanarStack) lStack);
      //ImageJFunctions.show(imgStack);
      //IJ.run("Enhance Contrast", "saturated=0.35");
    }
    return lArgMaxQualityDeltaZ;
  }


  private void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope, LightSheetMicroscopeQueue lQueue, double lIlluminationZStart, int lDetectionArmIndex, double lDetectionZZStart) {
    ((InterpolatedAcquisitionState)lLightsheetMicroscope.getAcquisitionStateManager().getCurrentState()).applyAcquisitionStateAtZ(lQueue, lIlluminationZStart);

    for (int i = 0; i < lLightsheetMicroscope.getNumberOfLightSheets(); i++) {
      lQueue.setI(i, false);

      //lQueue.setIZ(lIlluminationZStart);
      //lQueue.setDZ(lDetectionArmIndex, lDetectionZZStart);
    }

    lQueue.setC(lDetectionArmIndex, false);

    lQueue.addCurrentStateToQueue();
    lQueue.addCurrentStateToQueue();
  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  public BoundedVariable<Integer> getImageWidthVariable()
  {
    return mImageWidthVariable;
  }

  public BoundedVariable<Integer> getImageHeightVariable()
  {
    return mImageHeightVariable;
  }

  public BoundedVariable<Integer> getNumberOfImagesToTakeVariable()
  {
    return mNumberOfImagesToTakeVariable;
  }

  public BoundedVariable<Double> getAlphaStepVariable()
  {
    return mAlphaStepVariable;
  }
}
