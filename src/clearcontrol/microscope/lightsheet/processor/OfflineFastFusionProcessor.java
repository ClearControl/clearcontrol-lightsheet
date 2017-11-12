package clearcontrol.microscope.lightsheet.processor;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcontrol.core.device.task.TaskDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.scripting.engine.ScriptingEngine;
import clearcontrol.stack.ContiguousOffHeapPlanarStackFactory;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import clearcontrol.stack.sourcesink.source.RawFileStackSource;
import coremem.recycling.BasicRecycler;
import fastfuse.FastFusionEngine;
import fastfuse.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class OfflineFastFusionProcessor extends TaskDevice implements
                                                           LoggingFeature,
                                                           VisualConsoleInterface {
  LightSheetMicroscope mLightSheetMicroscope;

  ClearCLContext mContext;

  LightSheetFastFusionEngine mFastFusionEngine;

  private Variable<String>
      mDataSetNamePostfixVariable =
      new Variable<String>("Test");
  private Variable<File>
      mRootFolderVariable =
      new Variable("RootFolder", (Object) null);

  private BoundedVariable<Double> mMemRatioVariable =
      new BoundedVariable<Double>("MemRatio",
      0.8,
      0.0,
          1.0,
      0.1);


  private final Variable<Boolean> mDownscaleSwitchVariable =
      new Variable<Boolean>("DownscaleSwitch", true);

  private final Variable<Boolean> mRegistrionSwitchVariable =
      new Variable<Boolean>("RegistrationSwitch", true);

  private final Variable<Integer> mNumberOfRestartsVariable =
      new Variable<Integer>("NumberOfRestarts",
                            5);

  private final Variable<Integer> mMaxNumberOfEvaluationsVariable =
      new Variable<Integer>("MaxNumberOfEvaluations",
                            200);

  private final BoundedVariable<Double> mTranslationSearchRadiusVariable =
      new BoundedVariable<Double>("TranslationSearchRadius",
                                  15.0);
  private final BoundedVariable<Double> mRotationSearchRadiusVariable =
      new BoundedVariable<Double>("RotationSearchRadius",
                                  3.0);

  private final BoundedVariable<Double> mSmoothingConstantVariable =
      new BoundedVariable<Double>("SmoothingConstant",
                                  0.05);

  private final Variable<Boolean> mTransformLockSwitchVariable =
      new Variable<Boolean>("TransformLockSwitch",
                            true);

  private final Variable<Integer> mTransformLockThresholdVariable =
      new Variable<Integer>("TransformLockThreshold",
                            20);

  private final Variable<Boolean> mBackgroundSubtractionSwitchVariable =
      new Variable<Boolean>("BackgroundSubtractionSwitch", false);

  private RegistrationTask mRegistrationTask;

  // Todo: determine following list from the selected folder
  private String[]
      names =
      { "C0L0",
        "C0L1",
        "C0L2",
        "C0L3",
        "C1L0",
        "C1L1",
        "C1L2",
        "C1L3" };











  public OfflineFastFusionProcessor(String pName, LightSheetMicroscope pLightSheetMicroscope, ClearCLContext pContext)
  {
    super(pName);
    mLightSheetMicroscope = pLightSheetMicroscope;
    mContext = pContext;

    mFastFusionEngine = new LightSheetFastFusionEngine(mContext, null, mLightSheetMicroscope.getNumberOfLightSheets(), mLightSheetMicroscope.getNumberOfDetectionArms());
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
      execute();

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

  private boolean execute() throws
                          InterruptedException,
                          ExecutionException,
                          TimeoutException,
                          IOException
  {
    if (isStopRequested())
      return false;

    String
        lDatasetname = getDataSetNamePostfixVariable().get();

    File lRootFolder = getRootFolderVariable().get();

    mFastFusionEngine.setSubtractingBackground(mBackgroundSubtractionSwitchVariable.get());
    mFastFusionEngine.setRegistration(mRegistrionSwitchVariable.get());
    mFastFusionEngine.setDownscale(mDownscaleSwitchVariable.get());
    mFastFusionEngine.setup(mLightSheetMicroscope.getNumberOfLightSheets(), mLightSheetMicroscope.getNumberOfDetectionArms());


    int lStackIndex = 0;
    assert lRootFolder != null;
    assert lRootFolder.isDirectory();


    //FastFusionEngine(mContext);
    /*
    long
        lMaxMemoryInBytes =
        (long) (lMemRatio * mContext.getDevice()
                                    .getGlobalMemorySizeInBytes());
    FastFusionMemoryPool.getInstance(mContext, lMaxMemoryInBytes);

    int[] lKernelSizesRegistration = new int[] { 3, 3, 3 };
    float[]
        lKernelSigmasRegistration =
        new float[] { 0.5f, 0.5f, 0.5f };

    float[] lKernelSigmasFusion = new float[] { 15, 15, 5 };

    float[] lKernelSigmasBackground = new float[] { 30, 30, 10 };

    if (lDownscale)
      mFastFusionEngine.addTasks(DownsampleXYbyHalfTask.applyAndReleaseInputs(
          DownsampleXYbyHalfTask.Type.Median,
          "d",
          "C0L0",
          "C0L1",
          "C0L2",
          "C0L3",
          "C1L0",
          "C1L1",
          "C1L2",
          "C1L3"));
    else
      mFastFusionEngine.addTasks(IdentityTask.withSuffix("d",
                                       "C0L0",
                                       "C0L1",
                                       "C0L2",
                                       "C0L3",
                                       "C1L0",
                                       "C1L1",
                                       "C1L2",
                                       "C1L3"));

    ImageChannelDataType
        lInitialFusionDataType =
        lRegistration ?
        ImageChannelDataType.Float :
        ImageChannelDataType.UnsignedInt16;

    mFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("C0",
                                                  lInitialFusionDataType,
                                                  lKernelSigmasFusion,
                                                  true,
                                                  "C0L0d",
                                                  "C0L1d",
                                                  "C0L2d",
                                                  "C0L3d"));

    mFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("C1",
                                                  lInitialFusionDataType,
                                                  lKernelSigmasFusion,
                                                  true,
                                                  "C1L0d",
                                                  "C1L1d",
                                                  "C1L2d",
                                                  "C1L3d"));

    if (lRegistration)
    {
      List<TaskInterface>
          lRegistrationTaskList =
          CompositeTasks.registerWithBlurPreprocessing("C0",
                                                       "C1",
                                                       "C1adjusted",
                                                       lKernelSigmasRegistration,
                                                       lKernelSizesRegistration,
                                                       AffineMatrix.scaling(
                                                           -1,
                                                           1,
                                                           1),
                                                       true);
      mFastFusionEngine.addTasks(lRegistrationTaskList);
      // extract registration task from list
      for (TaskInterface lTask : lRegistrationTaskList)
        if (lTask instanceof RegistrationTask)
        {
          mRegistrationTask = (RegistrationTask) lTask;
          break;
        }
    }
    else
    {
      mFastFusionEngine.addTask(FlipTask.flipX("C1", "C1adjusted"));
      mFastFusionEngine.addTask(new MemoryReleaseTask("C1adjusted", "C1"));
    }

    // addTasks(CompositeTasks.fuseWithSmoothWeights("fused",
    // ImageChannelDataType.UnsignedInt16,
    // pKernelSigmasFusion,
    // true,
    // "C0",
    // "C1adjusted"));

    if (lSubtractBackground)
    {
      mFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights(
          "fused-preliminary",
          ImageChannelDataType.Float,
          lKernelSigmasFusion,
          true,
          "C0",
          "C1adjusted"));

      mFastFusionEngine.addTasks(CompositeTasks.subtractBlurredCopyFromFloatImage(
          "fused-preliminary",
          "fused",
          lKernelSigmasBackground,
          true,
          ImageChannelDataType.UnsignedInt16));
    }
    else
    {

      mFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("fused-preliminary",
                                                    ImageChannelDataType.Float,
                                                    lKernelSigmasFusion,
                                                    true,
                                                    "C0",
                                                    "C1adjusted"));

      mFastFusionEngine.addTask(new NonnegativeSubtractionTask("fused-preliminary",
                                             0,
                                             "fused",
                                             ImageChannelDataType.UnsignedInt16));
    }*/

    BasicRecycler<StackInterface, StackRequest>
        stackRecycler =
        new BasicRecycler(new ContiguousOffHeapPlanarStackFactory(),
                          10,
                          10,
                          true);
    RawFileStackSource
        rawFileStackSource =
        new RawFileStackSource(stackRecycler);

    for (int i = 0; i < names.length; i++)
    {
      rawFileStackSource.setLocation(lRootFolder, lDatasetname);
      StackInterface
          stack =
          rawFileStackSource.getStack(names[i], lStackIndex);


      mFastFusionEngine.passImage(names[i], stack.getContiguousMemory(),
                                  ImageChannelDataType.UnsignedInt16,
                                  stack.getDimensions());
    }
    mFastFusionEngine.executeAllTasks();

    mFastFusionEngine.waitFusionTasksToComplete();

    for (String name : mFastFusionEngine.getAvailableImagesSlotKeys()) {
      System.out.println("available: " + name);
    }
    System.out.println("tasks " + mFastFusionEngine.getTasks().size());

    RawFileStackSink sink = new RawFileStackSink();
    sink.setLocation(lRootFolder, lDatasetname);

    ClearCLImage lFusedImage = mFastFusionEngine.getImage("fused");

    StackInterface lFusedStack = /*
    if (lFusedImage.getChannelDataType() == ImageChannelDataType.Float)
    {
     lFusedStack = new OffHeapPlanarStack(true,
                            0,
                            NativeTypeEnum.Float,
                            1,
                            new long[] { lFusedImage.getWidth(),
                                         lFusedImage.getHeight(),
                                         lFusedImage.getDepth() });
    } else if (lFusedImage.getChannelDataType() == ImageChannelDataType.UnsignedInt16) {
      lFusedStack = new OffHeapPlanarStack(true,
                                           0,
                                           NativeTypeEnum.UnsignedByte,
                                           1,
                                           new long[] { lFusedImage.getWidth(),
                                                        lFusedImage.getHeight(),
                                                        lFusedImage.getDepth() });
    }
    */
        stackRecycler.getOrWait(1000,
                                 TimeUnit.SECONDS,
                                 StackRequest.build(lFusedImage.getDimensions()));

    System.out.println("ow: " + lFusedImage.getWidth());
    System.out.println("oh: " + lFusedImage.getHeight());
    System.out.println("od: " + lFusedImage.getDepth());
    System.out.println("ot: " + lFusedImage.getChannelDataType());
    System.out.println("tw: " + lFusedStack.getWidth());
    System.out.println("th: " + lFusedStack.getHeight());
    System.out.println("td: " + lFusedStack.getDepth());
    System.out.println("tt: " + lFusedStack.getDataType());

    lFusedImage.writeTo(lFusedStack.getContiguousMemory(), true);

    sink.appendStack(lFusedStack);
    sink.close();


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





  public Variable<String> getDataSetNamePostfixVariable()
  {
    return mDataSetNamePostfixVariable;
  }

  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }


  /**
   * Returns the variable holding the translation search radius.
   *
   * @return translation search radius variable.
   */
  public BoundedVariable<Double> getTranslationSearchRadiusVariable()
  {
    return mTranslationSearchRadiusVariable;
  }

  /**
   * Returns the variable holding the rotation search radius
   *
   * @return rotation search radius
   */
  public BoundedVariable<Double> getRotationSearchRadiusVariable()
  {
    return mRotationSearchRadiusVariable;
  }

  /**
   * Returns the variable holding the number of optimization restarts
   *
   * @return number of optimization restarts variable
   */
  public Variable<Integer> getNumberOfRestartsVariable()
  {
    return mNumberOfRestartsVariable;
  }

  /**
   * Returns the max number of evaluations variable
   *
   * @return max number of evaluations variable
   */
  public Variable<Integer> getMaxNumberOfEvaluationsVariable()
  {
    return mMaxNumberOfEvaluationsVariable;
  }

  /**
   * Returns the variable holding the smoothing constant
   *
   * @return smoothing constant variable
   */
  public BoundedVariable<Double> getSmoothingConstantVariable()
  {
    return mSmoothingConstantVariable;
  }

  /**
   * Returns the switch that decides whether to lock the transformation after a
   * certain number of time points has elapsed
   *
   * @return Transform lock switch variable
   */
  public Variable<Boolean> getTransformLockSwitchVariable()
  {
    return mTransformLockSwitchVariable;
  }

  /**
   * Returns the variable holding the number of timepoints until the
   * transformation should be 'locked' with more stringent temporal filtering
   *
   * @return transform lock timer variable
   */
  public Variable<Integer> getTransformLockThresholdVariable()
  {
    return mTransformLockThresholdVariable;
  }

  public Variable<Boolean> getBackgroundSubtractionSwitchVariable() {
    return mBackgroundSubtractionSwitchVariable;
  }
  public Variable<Boolean> getDownscaleSwitchVariable() {
    return mDownscaleSwitchVariable;
  }
  public Variable<Boolean> getRegistrationSwitchVariable() {
    return mRegistrionSwitchVariable;
  }






}