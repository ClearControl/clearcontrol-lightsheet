package clearcontrol.microscope.lightsheet.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import fastfuse.tasks.*;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) November 2017
 */
public class OfflineFastFusionEngine extends TaskDevice implements
                                     LoggingFeature,
                                     VisualConsoleInterface
{
  LightSheetMicroscope mLightSheetMicroscope;

  ClearCLContext mContext;

  LightSheetFastFusionEngine mFastFusionEngine;

  private Variable<File> mRootFolderVariable =
                                             new Variable("RootFolder",
                                                          (Object) null);

  private BoundedVariable<Integer> mFirstTimePointToFuse =
                                                         new BoundedVariable<Integer>("First time point",
                                                                                      0,
                                                                                      0,
                                                                                      Integer.MAX_VALUE,
                                                                                      1);
  private BoundedVariable<Integer> mLastTimePointToFuse =
                                                        new BoundedVariable<Integer>("Last time point",
                                                                                     0,
                                                                                     0,
                                                                                     Integer.MAX_VALUE,
                                                                                     1);

  private final Variable<Boolean> mDownscaleSwitchVariable =
                                                           new Variable<Boolean>("DownscaleSwitch",
                                                                                 true);

  private final Variable<Boolean> mRegistrationSwitchVariable =
                                                              new Variable<Boolean>("RegistrationSwitch",
                                                                                    true);

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

  private final Variable<Boolean> mBackgroundSubtractionSwitchVariable =
                                                                       new Variable<Boolean>("BackgroundSubtractionSwitch",
                                                                                             false);

  private RegistrationTask mRegistrationTask;

  // Todo: determine following list from the selected folder
  private String[] names =
  { "C0L0", "C0L1", "C0L2", "C0L3", "C1L0", "C1L1", "C1L2", "C1L3" };

  public OfflineFastFusionEngine(String pName,
                                 LightSheetMicroscope pLightSheetMicroscope,
                                 ClearCLContext pContext)
  {
    super(pName);
    mLightSheetMicroscope = pLightSheetMicroscope;
    mContext = pContext;

    mFastFusionEngine =
                      new LightSheetFastFusionEngine(mContext,
                                                     null,
                                                     mLightSheetMicroscope.getNumberOfLightSheets(),
                                                     mLightSheetMicroscope.getNumberOfDetectionArms());
    mFastFusionEngine.setTimeStepping(false);
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

  @Override
  public void run()
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

  private boolean execute() throws InterruptedException,
                            ExecutionException,
                            TimeoutException,
                            IOException
  {
    if (isStopRequested())
    {
      return false;
    }

    File lRootFolder = getRootFolderVariable().get();

    String lDatasetname = lRootFolder.getName();

    lRootFolder = lRootFolder.getParentFile();

    mFastFusionEngine.setSubtractingBackground(mBackgroundSubtractionSwitchVariable.get());
    mFastFusionEngine.setRegistration(mRegistrationSwitchVariable.get());
    mFastFusionEngine.setDownscale(mDownscaleSwitchVariable.get());

    int lNumberOfLightSheets =
                             mLightSheetMicroscope.getNumberOfLightSheets();
    String lStackDirectory = lRootFolder + "/"
                             + lDatasetname
                             + "/stacks/";

    if (new File(lStackDirectory + "C0L0").exists()
        && new File(lStackDirectory + "C0L1").exists()
        && new File(lStackDirectory + "C0L2").exists()
        && new File(lStackDirectory + "C0L3").exists()
        && new File(lStackDirectory + "C1L0").exists()
        && new File(lStackDirectory + "C1L1").exists()
        && new File(lStackDirectory + "C1L2").exists()
        && new File(lStackDirectory + "C1L3").exists())
    {
      lNumberOfLightSheets = 4;
    }
    else if (new File(lStackDirectory + "C0L0").exists()
             && new File(lStackDirectory + "C1L0").exists())
    {
      lNumberOfLightSheets = 1;
    }
    mFastFusionEngine.setup(lNumberOfLightSheets,
                            mLightSheetMicroscope.getNumberOfDetectionArms());

    if (mRegistrationSwitchVariable.get())
    {

      mFastFusionEngine.getRegistrationTask()
                       .getParameters()
                       .setNumberOfRestarts(getNumberOfRestartsVariable().get()
                                                                         .intValue());

      mFastFusionEngine.getRegistrationTask()
                       .getParameters()
                       .setTranslationSearchRadius(getTranslationSearchRadiusVariable().get()
                                                                                       .doubleValue());

      mFastFusionEngine.getRegistrationTask()
                       .getParameters()
                       .setRotationSearchRadius(getRotationSearchRadiusVariable().get()
                                                                                 .doubleValue());

      mFastFusionEngine.getRegistrationTask()
                       .getParameters()
                       .setMaxNumberOfEvaluations((int) getMaxNumberOfEvaluationsVariable().get()
                                                                                           .intValue());

      mFastFusionEngine.getRegistrationTask()
                       .setSmoothingConstant(getSmoothingConstantVariable().get()
                                                                           .doubleValue());
    }

    assert lRootFolder != null;
    assert lRootFolder.isDirectory();

    BasicRecycler<StackInterface, StackRequest> stackRecycler =
                                                              new BasicRecycler(new ContiguousOffHeapPlanarStackFactory(),
                                                                                10,
                                                                                10,
                                                                                true);

    for (int timePoint =
                       getFirstTimePointToFuse().get(); timePoint <= getLastTimePointToFuse().get(); timePoint++)
    {

      if (isStopRequested())
      {
        break;
      }

      long lTimeMillis = System.currentTimeMillis();

      ArrayList<StackInterface> stacks = new ArrayList<>();
      for (int i = 0; i < names.length; i++)
      {
        if (new File(lStackDirectory + names[i]).exists())
        {

          RawFileStackSource rawFileStackSource =
                                                new RawFileStackSource(stackRecycler);
          rawFileStackSource.setLocation(lRootFolder, lDatasetname);
          info("getting" + names[i]);
          StackInterface stack =
                               rawFileStackSource.getStack(names[i],
                                                           timePoint);

          stacks.add(stack);

          info("Stack " + names[i]);
          mFastFusionEngine.passImage(names[i],
                                      stack.getContiguousMemory(),
                                      ImageChannelDataType.UnsignedInt16,
                                      stack.getDimensions());

          if (stack.getMetaData().getVoxelDimX() != null)
          {
            info("pixel size x" + stack.getMetaData().getVoxelDimX());
            info("pixel size y" + stack.getMetaData().getVoxelDimY());
            info("pixel size z" + stack.getMetaData().getVoxelDimZ());

            double lVoxelSizeX = stack.getMetaData().getVoxelDimX();
            double lVoxelSizeZ = stack.getMetaData().getVoxelDimZ();

            if (mDownscaleSwitchVariable.get())
            {
              lVoxelSizeX = lVoxelSizeX * 2;
            }

            if (mRegistrationSwitchVariable.get())
            {

              float lZAspectRatio =
                                  (float) (lVoxelSizeZ / lVoxelSizeX);

              mFastFusionEngine.getRegistrationTask()
                               .getParameters()
                               .setScaleZ(lZAspectRatio);

            }
          }
        }

        if (isStopRequested())
        {
          mFastFusionEngine.reset(true);
          break;
        }

      }

      int executedTasks = mFastFusionEngine.executeAllTasks();
      info("tasks executed: " + executedTasks);
      mFastFusionEngine.waitFusionTasksToComplete();

      for (String name : mFastFusionEngine.getAvailableImagesSlotKeys())
      {
        info("available image: " + name);
      }
      RawFileStackSink sink = new RawFileStackSink();
      sink.setLocation(lRootFolder, lDatasetname);

      ClearCLImage lFusedImage = mFastFusionEngine.getImage("fused");

      StackInterface lFusedStack =
                                 stackRecycler.getOrWait(1000,
                                                         TimeUnit.SECONDS,
                                                         StackRequest.build(lFusedImage.getDimensions()));

      lFusedImage.writeTo(lFusedStack.getContiguousMemory(), true);

      sink.appendStack(lFusedStack);
      sink.close();

      for (StackInterface stack : stacks)
      {
        stackRecycler.release(stack);
      }

      // mFastFusionEngine.removeImage("fused-preliminary");
      // mFastFusionEngine.removeImage("fused");

      mFastFusionEngine.reset(false);

      for (String name : mFastFusionEngine.getAvailableImagesSlotKeys())
      {
        info("still image: " + name);
      }

      mFastFusionEngine.reset(true);

      for (String name : mFastFusionEngine.getAvailableImagesSlotKeys())
      {
        info("still still image: " + name);
      }

      info("Fusion of time point " + timePoint
           + " took "
           + ((System.currentTimeMillis() - lTimeMillis) / 1000)
           + " sec");

      // stackRecycler.free();
    }

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

  public Variable<Boolean> getBackgroundSubtractionSwitchVariable()
  {
    return mBackgroundSubtractionSwitchVariable;
  }

  public Variable<Boolean> getDownscaleSwitchVariable()
  {
    return mDownscaleSwitchVariable;
  }

  public Variable<Boolean> getRegistrationSwitchVariable()
  {
    return mRegistrationSwitchVariable;
  }

  public BoundedVariable<Integer> getFirstTimePointToFuse()
  {
    return mFirstTimePointToFuse;
  }

  public BoundedVariable<Integer> getLastTimePointToFuse()
  {
    return mLastTimePointToFuse;
  }

}
