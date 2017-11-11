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
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;
import clearcontrol.stack.sourcesink.source.RawFileStackSource;
import coremem.enums.NativeTypeEnum;
import coremem.recycling.BasicRecycler;
import fastfuse.FastFusionEngine;
import fastfuse.FastFusionMemoryPool;
import fastfuse.registration.AffineMatrix;
import fastfuse.tasks.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

  private final Variable<Boolean> mBackgroundSubtractionSwitchVariable =
      new Variable<Boolean>("BackgroundSubtractionSwitch", false);

  private RegistrationTask mRegistrationTask;


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

  public Variable<String> getDataSetNamePostfixVariable()
  {
    return mDataSetNamePostfixVariable;
  }

  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }
  public OfflineFastFusionProcessor(String pName, LightSheetMicroscope pLightSheetMicroscope, ClearCLContext pContext)
  {
    super(pName);
    mLightSheetMicroscope = pLightSheetMicroscope;
    mContext = pContext;
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

    double lMemRatio = mMemRatioVariable.get();

    boolean lDownscale = mDownscaleSwitchVariable.get();

    boolean lRegistration = mRegistrionSwitchVariable.get();

    boolean lSubtractBackground = mBackgroundSubtractionSwitchVariable.get();

    int lStackIndex = 0;
    assert lRootFolder != null;
    assert lRootFolder.isDirectory();


    FastFusionEngine lFastFusionEngine = new LightSheetFastFusionEngine(mContext, null, 4,2, lSubtractBackground);
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
      lFastFusionEngine.addTasks(DownsampleXYbyHalfTask.applyAndReleaseInputs(
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
      lFastFusionEngine.addTasks(IdentityTask.withSuffix("d",
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

    lFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("C0",
                                                  lInitialFusionDataType,
                                                  lKernelSigmasFusion,
                                                  true,
                                                  "C0L0d",
                                                  "C0L1d",
                                                  "C0L2d",
                                                  "C0L3d"));

    lFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("C1",
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
      lFastFusionEngine.addTasks(lRegistrationTaskList);
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
      lFastFusionEngine.addTask(FlipTask.flipX("C1", "C1adjusted"));
      lFastFusionEngine.addTask(new MemoryReleaseTask("C1adjusted", "C1"));
    }

    // addTasks(CompositeTasks.fuseWithSmoothWeights("fused",
    // ImageChannelDataType.UnsignedInt16,
    // pKernelSigmasFusion,
    // true,
    // "C0",
    // "C1adjusted"));

    if (lSubtractBackground)
    {
      lFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights(
          "fused-preliminary",
          ImageChannelDataType.Float,
          lKernelSigmasFusion,
          true,
          "C0",
          "C1adjusted"));

      lFastFusionEngine.addTasks(CompositeTasks.subtractBlurredCopyFromFloatImage(
          "fused-preliminary",
          "fused",
          lKernelSigmasBackground,
          true,
          ImageChannelDataType.UnsignedInt16));
    }
    else
    {

      lFastFusionEngine.addTasks(CompositeTasks.fuseWithSmoothWeights("fused-preliminary",
                                                    ImageChannelDataType.Float,
                                                    lKernelSigmasFusion,
                                                    true,
                                                    "C0",
                                                    "C1adjusted"));

      lFastFusionEngine.addTask(new NonnegativeSubtractionTask("fused-preliminary",
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


      lFastFusionEngine.passImage(names[i], stack.getContiguousMemory(),
                ImageChannelDataType.UnsignedInt16,
                stack.getDimensions());
    }
    lFastFusionEngine.executeAllTasks();

    lFastFusionEngine.waitFusionTasksToComplete();

    for (String name : lFastFusionEngine.getAvailableImagesSlotKeys()) {
      System.out.println("available: " + name);
    }

    RawFileStackSink sink = new RawFileStackSink();
    sink.setLocation(lRootFolder, lDatasetname);

    ClearCLImage lFusedImage = lFastFusionEngine.getImage("fused");

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
}
