package clearcontrol.microscope.lightsheet.extendeddepthoffocus;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.cameras.StackCameraDeviceInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.core.FocusableImager;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.core.ImageRange;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.iqm.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionEngine;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.ContiguousOffHeapPlanarStackFactory;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import coremem.recycling.BasicRecycler;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The EDFImagingEngine allows taking images with several
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
public class EDFImagingEngine extends TaskDevice implements
                                                          LoggingFeature,
                                                          VisualConsoleInterface
{
  private final static double sDoubleTolerance = 0.0001;

  private BoundedVariable<Integer> mLightSheetMinIndex;
  private BoundedVariable<Integer> mLightSheetMaxIndex;



  //  private BoundedVariable<Integer> mLightSheetIndex;
  private BoundedVariable<Integer>
      mNumberOfISamples =
      new BoundedVariable<Integer>("Number of illumination samples (control planes)",
                                   7,
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

  private final BoundedVariable<Double>
      mMinimumRange =
      new BoundedVariable<Double>("Minimum focus Z range",
                                  10.0,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  1.0);


  private BoundedVariable<Double> mFirstZ;


  private BoundedVariable<Double> mLastZ;

  private BoundedVariable<Integer> mNumberOfIterations =
      new BoundedVariable<Integer>(
          "Number of iterations",
          20,
          0,
          Integer.MAX_VALUE,
          1);



  private final BoundedVariable<Double>
      mExposureTimeForEDFInSeconds =
      new BoundedVariable<Double>("Exposure time for EDF in seconds",
                                  0.03,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.001);

  private final BoundedVariable<Double>
      mExposureTimeForStacksInSeconds =
      new BoundedVariable<Double>("Exposure time for stacks in seconds",
                                  0.03,
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.001);

  private final Variable<Boolean> mSaveEDFStacks = new Variable<Boolean>("Save EDF stacks", false);
  private final Variable<Boolean> mSaveCameraStacks = new Variable<Boolean>("Save camera stacks", false);
  private final Variable<Boolean> mSaveFusedStacks = new Variable<Boolean>("Save fused stacks", true);

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

  private BoundedVariable<Integer> mNumberOfStackSlicesVariable = new BoundedVariable<Integer>("Number of slices", 100, 0, Integer.MAX_VALUE, 1);


  private final LightSheetMicroscope mLightSheetMicroscope;
  final LightSheetFastFusionEngine mFastFusionEngine;

  public EDFImagingEngine(ClearCLContext pContext, LightSheetMicroscope pLightSheetMicroscope)
  {
    super("EDF Imaging");
    mLightSheetMicroscope = pLightSheetMicroscope;

    mFastFusionEngine = new LightSheetFastFusionEngine(pContext, null, 1, mLightSheetMicroscope.getNumberOfDetectionArms());

    mFastFusionEngine.setSubtractingBackground(false);
    mFastFusionEngine.setRegistration(true);
    mFastFusionEngine.setDownscale(true);

    mFastFusionEngine.setup(1,
                            mLightSheetMicroscope.getNumberOfDetectionArms());


    this.setName("EDF_Imaging");

    mLightSheetMinIndex =
        new BoundedVariable<Integer>("Light sheet start",
                                     0,
                                     0,
                                     mLightSheetMicroscope.getNumberOfLightSheets() - 1,
                                     1);

    mLightSheetMaxIndex =
        new BoundedVariable<Integer>("Light sheet end",
                                     mLightSheetMicroscope.getNumberOfLightSheets() - 1,
                                     0,
                                     mLightSheetMicroscope.getNumberOfLightSheets() - 1,
                                     1);


    LightSheetInterface
        lLightSheetDevice =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(LightSheetInterface.class,
                                            0);

    BoundedVariable<Number>
        lLightsheetZVariable =
        lLightSheetDevice.getZVariable();

    mFirstZ = new BoundedVariable<Double>("First Z",
                                           lLightsheetZVariable.getMin().doubleValue(),
                                           lLightsheetZVariable.getMin().doubleValue(),
                                           lLightsheetZVariable.getMax().doubleValue(),
      1.0);

    mLastZ = new BoundedVariable<Double>("Last Z",
                                          lLightsheetZVariable.getMax().doubleValue(),
                                          lLightsheetZVariable.getMin().doubleValue(),
                                          lLightsheetZVariable.getMax().doubleValue(),
                                          1.0);
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

    AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>>
        lAcquisitionStateManager =
        mLightSheetMicroscope.getDevice(AcquisitionStateManager.class,
                                        0);

    //LightSheetAcquisitionStateInterface<?> lCurrentState =
     //   lAcquisitionStateManager.getCurrentState();



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
        lNumberOfIterations =
        mNumberOfIterations.get();

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

    double lExposureTimeForEDFInSeconds = mExposureTimeForEDFInSeconds.get();
    double lExposureTimeForStacksInSeconds = mExposureTimeForStacksInSeconds.get();

    String
        lDatasetname =
        "" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-00-").format(
            new Date()) + getDataSetNamePostfixVariable().get();

    BoundedVariable<Number>
        lDetectionFocusZVariable =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(DetectionArmInterface.class,
                                            0)
                                 .getZVariable();

    LightSheetInterface
        lLightSheetDevice =
        getLightSheetMicroscope().getDeviceLists()
                                 .getDevice(LightSheetInterface.class,
                                            0);

    BoundedVariable<Number>
        lLightsheetZVariable =
        lLightSheetDevice.getZVariable();

    int lNumberOfDetectionArms = mLightSheetMicroscope.getNumberOfDetectionArms();


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
        lMinFixedZ = mFirstZ.get(); // lDetectionFocusZVariable.getMin().doubleValue();
    double
        lMaxFixedZ = mLastZ.get(); // lDetectionFocusZVariable.getMax().doubleValue();

    if (!mDetectionArmFixedVariable.get())
    {
      lMinMovingZ = lDetectionFocusZVariable.getMin().doubleValue();
      lMaxMovingZ = lDetectionFocusZVariable.getMax().doubleValue();

      //lMinFixedZ = lLightsheetZVariable.getMin().doubleValue();
     // lMaxFixedZ = lLightsheetZVariable.getMax().doubleValue();
    }

    double lMinimumRange = mMinimumRange.get();

    ImageRange[][]
        lImageRanges =
        new ImageRange[lNumberOfDetectionArms][lNumberOfFixedSamples];

    double
        lStepMovingZ =
        (lMaxMovingZ - lMinMovingZ) / (lNumberOfMovingSamples - 1);
    double
        lStepFixedZ =
        (lMaxFixedZ - lMinFixedZ) / (lNumberOfFixedSamples - 1);

    int lNumberOfStackSlices = mNumberOfStackSlicesVariable.get();


    mLogFileWriter.write("Dataset " + mRootFolderVariable.get() + "\\" + lDatasetname);
    mLogFileWriter.write("Number of " + (mDetectionArmFixedVariable.get()?"detection arm":"light sheet") + " samples: " + lNumberOfFixedSamples);
    mLogFileWriter.write("Number of " + (!mDetectionArmFixedVariable.get()?"detection arm":"light sheet") + " samples: " + lNumberOfMovingSamples);
    mLogFileWriter.write("Detection arm fixed: " + mDetectionArmFixedVariable.get());
    mLogFileWriter.write("Number of iterations: " + lNumberOfIterations);
    mLogFileWriter.write("Minimum range: " + lMinimumRange);
    mLogFileWriter.write("Minimum fixed Z: " + lMinFixedZ);
    mLogFileWriter.write("Maximum fixed Z: " + lMaxFixedZ);
    mLogFileWriter.write("Minimum moving Z: " + lMinMovingZ);
    mLogFileWriter.write("Maximum moving Z: " + lMaxMovingZ);

    mLogFileWriter.write("Image width: " + lImageWidth);
    mLogFileWriter.write("Image height: " + lImageHeight);
    mLogFileWriter.write("Exposure time EDF (sec): " + lExposureTimeForEDFInSeconds);
    mLogFileWriter.write("Exposure time Stacks (sec): " + lExposureTimeForStacksInSeconds);
    mLogFileWriter.write("Number of slices in stacks: " + lNumberOfStackSlices);

    mLogFileWriter.write("Start " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS-").format(new Date()) + "\n");




    OffHeapPlanarStack[]
        lNormalStacks = new OffHeapPlanarStack[lNumberOfDetectionArms];

    OffHeapPlanarStack[]
        lEDFStacks = new OffHeapPlanarStack[lNumberOfDetectionArms];


    // define ImageRange: For all detection arm positions take
    // images for all light sheet positions (or vise versa)

    for (int lDetectionArm = 0; lDetectionArm < lNumberOfDetectionArms; lDetectionArm++)
    {

      int lQualitySampleIndex = 0;
      for (double lFixedZ = lMinFixedZ; lFixedZ <= lMaxFixedZ + 0.0001;
           lFixedZ +=
               lStepFixedZ)
      {
        ImageRange imageRange = new ImageRange();
        imageRange.mFixedPosition = lFixedZ;
        imageRange.mMovingPositions = new double[lNumberOfMovingSamples];

        int lMovingCount = 0;
        for (double lMovingZ = lMinMovingZ; lMovingZ <= lMaxMovingZ + 0.00001;
             lMovingZ +=
                 lStepMovingZ)
        {
          imageRange.mMovingPositions[lMovingCount] = lMovingZ;
          lMovingCount++;
        }

        lImageRanges[lDetectionArm][lQualitySampleIndex] = imageRange;
        lQualitySampleIndex++;
      }

      // setup imager
      FocusableImager
          lInitialImager =
          new FocusableImager(getLightSheetMicroscope(),
                              lLightSheetMinIndex,
                              lLightSheetMaxIndex,
                              lDetectionArm,
                              lExposureTimeForEDFInSeconds);

      lInitialImager.setFieldOfView((int) lImageWidth,
                                    (int) lImageHeight);

      // Get the first image stack for analysis / optimisation
      lEDFStacks[lDetectionArm] = imageEDFStack(lInitialImager, lImageRanges[lDetectionArm]);
    }



    BasicRecycler<StackInterface, StackRequest>
        stackRecycler =
        new BasicRecycler(new ContiguousOffHeapPlanarStackFactory(),
                          10,
                          10,
                          true);
    StackInterface
        lFusedStack = null;

    for (int lDetectionArm = 0; lDetectionArm < lNumberOfDetectionArms; lDetectionArm++) {
      for (int lFixedPosition = 0; lFixedPosition < lNumberOfFixedSamples; lFixedPosition++)
      {
        configureChart("Quality",
                       "Quality_C" + lDetectionArm + "_CPI" + lFixedPosition,
                       "EDF slice",
                       "Quality",
                       ChartType.Line);
      }
      configureChart("Selected_Z", "Selected_Z_C" + lDetectionArm, "fixedZ", "movingZ", ChartType.Line);

    }

    //Thread[] lImagingThreads = new Thread[lNumberOfDetectionArms];

    int lIterationCount[] = new int[lNumberOfDetectionArms];
    while (true)
    {
      for (int lDetectionArm = 0; lDetectionArm < lNumberOfDetectionArms; lDetectionArm++)
      {
        info("Start loop C" + lDetectionArm + "...");
        mLogFileWriter.write("Imaging with detection arm " + lDetectionArm + "\n");
        // save resulting images to disc

        if (mSaveEDFStacks.get())
        {
          info("Save C" + lDetectionArm + "_EDF...");
          String lChannel = "C" + lDetectionArm + "_EDF";
          lEDFStacks[lDetectionArm].getMetaData()
                                   .addEntry(MetaDataChannel.Channel,
                                             lChannel);
          mLogFileWriter.write("Start writing EDF stack to disc " + new SimpleDateFormat(
              "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
          lSink.appendStack(lChannel, lEDFStacks[lDetectionArm]);
          mLogFileWriter.write("Stop writing EDF stack to disc  " + new SimpleDateFormat(
              "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
        }
        // Analyse images: get quality per slice

        info("Analyse C" + lDetectionArm + "_EDF...");
        mLogFileWriter.write("Start analysing quality " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
        DiscreteConsinusTransformEntropyPerSliceEstimator
            lImageQualityEstimator =
            new DiscreteConsinusTransformEntropyPerSliceEstimator(
                lEDFStacks[lDetectionArm]);
        double[]
            lQualityPerSliceMeasurementsArray =
            lImageQualityEstimator.getQualityArray();

        mLogFileWriter.write("Stop analysing quality  " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

        int lQualitySampleIndex = 0;
        boolean lClearQualityGraph = true;
        boolean lClearSelectedZGraph = true;

        int lFixedPosition = 0;

        double lMeanQuality = new Mean().evaluate(lQualityPerSliceMeasurementsArray);
        double lStandardDeviationQuality = new StandardDeviation().evaluate(lQualityPerSliceMeasurementsArray, lMeanQuality);

        double lQualityThreshold = lMeanQuality * lStandardDeviationQuality;

        for (ImageRange lImageRange : lImageRanges[lDetectionArm])
        {
          double lMaxQuality = lQualityPerSliceMeasurementsArray[lQualitySampleIndex];
          double lBestMovingZ = 0;


          // determine moving slice in focus
          for (double lMovingZ : lImageRange.mMovingPositions)
          {
            addPoint("Quality", "Quality_C" + lDetectionArm + "_CPI" + lFixedPosition , lClearQualityGraph, lQualitySampleIndex, lQualityPerSliceMeasurementsArray[lQualitySampleIndex]);
            lClearQualityGraph = false;

            mLogFileWriter.write("Quality["
                                 + lQualitySampleIndex
                                 + "] "
                                 + lImageRange.mFixedPosition
                                 + "\t"
                                 + lMovingZ
                                 + "\t"
                                 + lQualityPerSliceMeasurementsArray[lQualitySampleIndex]
                                 + "\n");
            if (lMaxQuality < lQualityPerSliceMeasurementsArray[lQualitySampleIndex])
            {
              lMaxQuality = lQualityPerSliceMeasurementsArray[lQualitySampleIndex];
              lBestMovingZ = lMovingZ;
            }
            lQualitySampleIndex++;
          }
          lFixedPosition++;
          //configureChart("Selected_Z", "Selected_Z_C" + lDetectionArm, "LZ", "DZ", ChartType.Scatter);

          addPoint("Selected_Z", "Selected_Z_C" + lDetectionArm, lClearSelectedZGraph, lImageRange.mFixedPosition, lBestMovingZ);
          lClearSelectedZGraph = false;

          mLogFileWriter.write("Best moving position "
                               + lBestMovingZ
                               + " Quality: "
                               + lMaxQuality
                               + "\n");
          if (lMaxQuality < lQualityThreshold) {
            warning("Quality in CPI " + lFixedPosition + " to low! Resetting to calibration.");
            lBestMovingZ = lImageRange.mFixedPosition;
          }

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
          for (double lMovingZ = lBestMovingZ - lNewRange / 2;
               lMovingZ
               <= lBestMovingZ + lNewRange / 2 + sDoubleTolerance;
               lMovingZ +=
                   lStepMovingZ)
          {
            lImageRange.mMovingPositions[lMovingCount] =
                Math.max(Math.min(lMovingZ, lMaxMovingZ), lMinMovingZ);
            lMovingCount++;
          }
        }

        mLogFileWriter.write("ITERATION " + lIterationCount[lDetectionArm] + "\n");

        // Cancel if user wants it or if iterations exceeded
        lIterationCount[lDetectionArm]++;
        if (isStopRequested())
        {
          break;
        }

        final int lDetectionArmCopy = lDetectionArm;

        /*
        Thread lImagingThread = new Thread() {
          @Override
          public void run()
          {*/

        // take new images
        FocusableImager
            lPreciseImager =
            new FocusableImager(getLightSheetMicroscope(),
                                lLightSheetMinIndex,
                                lLightSheetMaxIndex,
                                lDetectionArmCopy,
                                lExposureTimeForEDFInSeconds);
        lPreciseImager.setFieldOfView((int) lImageWidth, (int) lImageHeight);

        FocusableImager
            lImager =
            new FocusableImager(getLightSheetMicroscope(),
                                lLightSheetMinIndex,
                                lLightSheetMaxIndex,
                                lDetectionArmCopy,
                                lExposureTimeForStacksInSeconds);
        lImager.setFieldOfView((int) lImageWidth, (int) lImageHeight);

        try
        {
          info("Imaging C" + lDetectionArm + "_EDF...");
          lEDFStacks[lDetectionArmCopy] =
              imageEDFStack(lPreciseImager, lImageRanges[lDetectionArmCopy]);

          info("Imaging C" + lDetectionArm + "...");
          lNormalStacks[lDetectionArmCopy] = imageNormalStack(lImager, lImageRanges[lDetectionArmCopy], lNumberOfStackSlices);
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

        String lNormalChannel = "C" + lDetectionArmCopy;

        if (mSaveCameraStacks.get())
        {
          info("Save C" + lDetectionArm + "...");
          mLogFileWriter.write("Start writing stack to disc " + new SimpleDateFormat(
              "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
          lSink.appendStack(lNormalChannel, lNormalStacks[lDetectionArmCopy]);
          mLogFileWriter.write("Stop writing stack to disc  " + new SimpleDateFormat(
              "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
        }
            /*
          }
        };

        lImagingThreads[lDetectionArm] = lImagingThread;
        for (int lOtherDetectionArm = 0; lOtherDetectionArm < lNumberOfDetectionArms; lOtherDetectionArm++) {
          if(lOtherDetectionArm != lDetectionArm) {
            if (lImagingThreads[lOtherDetectionArm] != null) {
              lImagingThreads[lOtherDetectionArm].join();
            }
          }
        }
        lImagingThreads[lDetectionArm].start();*/
      }
      if (isStopRequested())
      {
        warning("Cancelled by user");
        break;
      }


      if (mSaveFusedStacks.get())
      {
        info("Fusing images...");
        mLogFileWriter.write("Start fusing   " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

        for (int lDetectionArm = 0; lDetectionArm < lNumberOfDetectionArms; lDetectionArm++)
        {

          mFastFusionEngine.passImage("C" + lDetectionArm + "L0",
                                      lNormalStacks[lDetectionArm].getContiguousMemory(),
                                      ImageChannelDataType.UnsignedInt16,
                                      lNormalStacks[lDetectionArm].getDimensions());
        }
        //mFastFusionEngine.addTask(new MemoryReleaseTask("fused", "C0L0", "C1L0", "C0L0d", "C1L0d", "C0L0blur", "C1L0blur", "C1L0reg"));

        mFastFusionEngine.executeAllTasks();

        mFastFusionEngine.waitFusionTasksToComplete();

        for (String name : mFastFusionEngine.getAvailableImagesSlotKeys())
        {
          info("available image: " + name);
        }

        ClearCLImage lFusedImage = mFastFusionEngine.getImage("fused");

        if (lFusedStack == null)
        {
          lFusedStack =
              stackRecycler.getOrWait(1000,
                                      TimeUnit.SECONDS,
                                      StackRequest.build(lFusedImage.getDimensions()));
        }

        lFusedImage.writeTo(lFusedStack.getContiguousMemory(), true);

        mLogFileWriter.write("Stop fusing    " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");


        info("Saving fused stack...");
        mLogFileWriter.write("Start writing fused stack to disc " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
        lSink.appendStack("default", lFusedStack);
        mLogFileWriter.write("Stop writing stack to disc  " + new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

        mFastFusionEngine.reset(false);
      }

      if (isStopRequested())
      {
        warning("Cancelled by user");
        break;
      }

      info("Loop done.");
      lNumberOfIterations--;
      if (lNumberOfIterations < 0) {
        info("Number of iterations reached.");
        break;
      }
    }

    lSink.close();
    mLogFileWriter.close();
    info("Bye.");
    return true;
  }

  private OffHeapPlanarStack imageNormalStack(FocusableImager pImager,
                                           ImageRange[] pImageRanges, int pNumberOfSlices ) throws
                                                                   InterruptedException,
                                                                   ExecutionException,
                                                                   TimeoutException,
                                                                   IOException
  {

    double[] x = new double[pImageRanges.length];
    double[] yLightSheetPosition = new double[pImageRanges.length];
    double[] yDetectionArmPosition = new double[pImageRanges.length];

    int count = 0;
    for (ImageRange lImageRange : pImageRanges)
    {
      for (double lMovingPosition : lImageRange.mMovingPositions)
      {
        x[count] = count;
        if (mDetectionArmFixedVariable.get())
        {
          yLightSheetPosition[count] = lMovingPosition;
          yDetectionArmPosition[count] = lImageRange.mFixedPosition;
        }
        else
        {
          yLightSheetPosition[count] = lImageRange.mFixedPosition;
          yDetectionArmPosition[count] = lMovingPosition;
        }
      }
      count++;
    }

    UnivariateInterpolator lLightSheetPositionInterpolator = new SplineInterpolator();
    UnivariateFunction lLightSheetPositionFunction = lLightSheetPositionInterpolator.interpolate(x,
                                                yLightSheetPosition);

    UnivariateInterpolator lDetectionArmPositionInterpolator = new SplineInterpolator();
    UnivariateFunction lDetectionArmPositionFunction = lDetectionArmPositionInterpolator.interpolate(x, yDetectionArmPosition);

    for (int slice = 0; slice < pNumberOfSlices; slice++) {
      double position = (double)(count-1) * slice / (pNumberOfSlices);

      mLogFileWriter.write("Image at "
                           + lLightSheetPositionFunction.value(position)
                           + "/"
                           + lDetectionArmPositionFunction.value(position)
                           + "\n");
      pImager.addImageRequest(lLightSheetPositionFunction.value(position),
                              lDetectionArmPositionFunction.value(position));
    }

    // Request for all images defined in the ImageRange
/*    for (ImageRange lImageRange : pImageRanges)
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
*/
    // Actually run the imaging here
    mLogFileWriter.write("Start imaging    " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
    final OffHeapPlanarStack lStack = pImager.execute();
    mLogFileWriter.write("Finished imaging " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

    lStack.getMetaData().addEntry(MetaDataView.Camera, pImager.getDetectionArmIndex());
    lStack.getMetaData().addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapse);

    return lStack;
  }


  private OffHeapPlanarStack imageEDFStack(FocusableImager pImager,
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
          pImager.addImageRequest(lMovingPosition, lImageRange.mFixedPosition);
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
    mLogFileWriter.write("Start EDF imaging    " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");
    final OffHeapPlanarStack lStack = pImager.execute();
    mLogFileWriter.write("Finished EDF imaging " + new SimpleDateFormat(
        "yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) + "\n");

    lStack.getMetaData().addEntry(MetaDataView.Camera, pImager.getDetectionArmIndex());
    lStack.getMetaData().addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapse);

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
   * Returns true if EDF imging should be stopped immediately.
   *
   * @return true for stopping, false otherwise.
   */
  public boolean isStopRequested()
  {
    return ScriptingEngine.isCancelRequestedStatic()
           || getStopSignalVariable().get();
  }

  public BoundedVariable<Integer> getNumberOfStackSlicesVariable()
  {
    return mNumberOfStackSlicesVariable;
  }

  public BoundedVariable<Double> getFirstZ()
  {
    return mFirstZ;
  }

  public BoundedVariable<Double> getLastZ()
  {
    return mLastZ;
  }

  public BoundedVariable<Double> getExposureTimeForEDFInSeconds()
  {
    return mExposureTimeForEDFInSeconds;
  }

  public BoundedVariable<Double> getExposureTimeForStacksInSeconds()
  {
    return mExposureTimeForStacksInSeconds;
  }

  public Variable<Boolean> getSaveEDFStacks()
  {
    return mSaveEDFStacks;
  }

  public Variable<Boolean> getSaveCameraStacks()
  {
    return mSaveCameraStacks;
  }

  public Variable<Boolean> getSaveFusedStacks()
  {
    return mSaveFusedStacks;
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

  public BoundedVariable<Integer> getNumberOfIterations()
  {
    return mNumberOfIterations;
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
}
