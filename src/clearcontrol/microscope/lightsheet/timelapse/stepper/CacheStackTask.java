package clearcontrol.microscope.lightsheet.timelapse.stepper;

import clearcl.ClearCLImage;
import fastfuse.FastFusionEngineInterface;
import fastfuse.tasks.TaskBase;
import fastfuse.tasks.TaskInterface;
import framework.Handler;

/**
 * Identity task - this task does nothing, just instantaneously passes the image
 * from a source to a destination slot.
 *
 * @author Raddock
 */
public class CacheStackTask extends TaskBase implements TaskInterface
{

  private Handler mTimeStepHandler;
  private final String mSrcImageSlotKey;
  private final long mStartTime;
  private long mLastTime;

  public CacheStackTask(String pSrcImageSlotKey,
                        Handler pTimeStepHandler)
  {
    super(pSrcImageSlotKey);
    mTimeStepHandler = pTimeStepHandler;
    mSrcImageSlotKey = pSrcImageSlotKey;
    mStartTime = System.currentTimeMillis();
    mLastTime = 0;
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {
    System.out.println("available images are");

    for (String Key : pFastFusionEngine.getAvailableImagesSlotKeys())
    {
      System.out.println(Key);
    }

    // read out time and measure time step

    // measure total time since start of application
    long pRunTime = System.currentTimeMillis() - mStartTime;

    // compute step till last run
    long pStep = pRunTime - mLastTime;

    System.out.println("time is: " + pRunTime
                       + " and Step is: "
                       + pStep);

    // set LastTime for next run
    mLastTime = pRunTime;

    if (pFastFusionEngine.isImageAvailable(mSrcImageSlotKey))
    {
      System.out.println(mSrcImageSlotKey
                         + " is being cached and processed");

      ClearCLImage CurrImage =
                             pFastFusionEngine.getImage(mSrcImageSlotKey);
      mTimeStepHandler.processImage(CurrImage,
                                    (float) pRunTime,
                                    (float) pStep);
      System.out.println("computed step would be: " + pStep);
    }
    else
    {
      System.out.println(mSrcImageSlotKey + " was not available");
    }
    return true;
  }

}
