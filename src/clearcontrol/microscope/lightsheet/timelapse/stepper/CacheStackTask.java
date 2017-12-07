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
  private float mStartTime;
  private float mLastTime;

  public CacheStackTask(String pSrcImageSlotKey, Handler pTimeStepHandler)
  {
	mTimeStepHandler = pTimeStepHandler;
    mSrcImageSlotKey = pSrcImageSlotKey;
    // one way to track time
    mStartTime = System.currentTimeMillis();
  }

@Override
public boolean enqueue(FastFusionEngineInterface pFastFusionEngine, boolean pWaitToFinish) 
{
	//TODO we need a measure of runtime overall
	float pTime = System.currentTimeMillis()-mStartTime;
	float pStep = pTime - mLastTime;
	
	
	ClearCLImage CurrImage = pFastFusionEngine.getImage(mSrcImageSlotKey);
	mTimeStepHandler.processImage(CurrImage, pTime, pStep);
	System.out.println("computed step would be: "+pStep);
	
	mLastTime = pTime;
	
	return true;
}

}
