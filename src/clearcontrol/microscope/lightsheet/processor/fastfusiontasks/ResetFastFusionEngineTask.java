package clearcontrol.microscope.lightsheet.processor.fastfusiontasks;

import clearcontrol.core.log.LoggingFeature;
import fastfuse.FastFusionEngineInterface;
import fastfuse.tasks.TaskBase;
import fastfuse.tasks.TaskInterface;

/**
 * Deprecated: As stack saving dependent on what has been imaged is no
 * longer a FastFuseTask, resetting the engine is no longer needed
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
@Deprecated
public class ResetFastFusionEngineTask extends TaskBase
    implements TaskInterface, LoggingFeature
{
  String mSourceImageKey;

  public ResetFastFusionEngineTask(String pSourceImageKey) {
    super(pSourceImageKey);
    mSourceImageKey = pSourceImageKey;
  }


  @Override public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                                   boolean pWaitToFinish)
  {
    if (pFastFusionEngine.getAvailableImagesSlotKeys().contains(mSourceImageKey)) {
      pFastFusionEngine.reset(false);
      return true;
    }
    return false;
  }
}
