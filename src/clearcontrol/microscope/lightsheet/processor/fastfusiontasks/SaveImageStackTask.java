package clearcontrol.microscope.lightsheet.processor.fastfusiontasks;

import clearcl.ClearCLImage;
import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionEngine;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.stacks.MetaDataViewFlags;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.sourcesink.sink.FileStackSinkInterface;
import coremem.recycling.RecyclerInterface;
import fastfuse.FastFusionEngineInterface;
import fastfuse.tasks.FusionTaskBase;
import fastfuse.tasks.TaskBase;
import fastfuse.tasks.TaskInterface;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.concurrent.TimeUnit;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class SaveImageStackTask extends TaskBase
    implements TaskInterface, LoggingFeature
{
  RecyclerInterface<StackInterface, StackRequest> mStackRecycler;
  FileStackSinkInterface mSinkInterface;
  String mChannel;
  String mAfterDoneImageFlagKey;
  String mSourceImageKey;

  public SaveImageStackTask(String pSourceImageKey, String pAfterDoneImageFlagKey, FileStackSinkInterface pSinkInterface, RecyclerInterface<StackInterface, StackRequest> pStackRecycler, String pChannel) {
    super(pSourceImageKey);
    mStackRecycler = pStackRecycler;
    mSinkInterface = pSinkInterface;
    mChannel = pChannel;
    mAfterDoneImageFlagKey = pAfterDoneImageFlagKey;
    mSourceImageKey = pSourceImageKey;
  }

  @Override public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                                   boolean pWaitToFinish)
  {


    if (pFastFusionEngine.getAvailableImagesSlotKeys().contains(mSourceImageKey))
    {
      ClearCLImage lFusedImage = pFastFusionEngine.getImage(mSourceImageKey);

      /*copyFusedStack(pStackRecycler,
                     lFusedImage,
                     mEngine.getFusedMetaData(),
                     null);*/

      StackInterface lFusedStack =
          mStackRecycler.getOrWait(1000,
                                   TimeUnit.SECONDS,
                                   StackRequest.build(lFusedImage.getDimensions()));


      if (pFastFusionEngine instanceof LightSheetFastFusionEngine)
      {
        LightSheetFastFusionEngine lLightSheetFastFusionEngine = (LightSheetFastFusionEngine)pFastFusionEngine;

        lFusedStack.setMetaData(lLightSheetFastFusionEngine.getFusedMetaData());
        lFusedStack.getMetaData().addEntry(MetaDataFusion.Fused, true);
      }
      /*if (pChannel != null)
        lFusedStack.getMetaData().addEntry(MetaDataChannel.Channel,
                                           pChannel);
      */
      lFusedStack.getMetaData().removeAllEntries(MetaDataView.class);
      lFusedStack.getMetaData()
                 .removeAllEntries(MetaDataViewFlags.class);
      lFusedStack.getMetaData().removeEntry(MetaDataOrdinals.Index);

      info("Resulting fused stack metadata:"
           + lFusedStack.getMetaData());

      lFusedImage.writeTo(lFusedStack.getContiguousMemory(), true);

      ElapsedTime.measureForceOutput("FastFuse stack saving",
                                     () -> mSinkInterface.appendStack(mChannel,
                                                                          lFusedStack));

      MutablePair<Boolean, ClearCLImage>
          lDestImageAndFlag =
          pFastFusionEngine.ensureImageAllocated(mAfterDoneImageFlagKey,
                                                 lFusedImage.getChannelDataType(),
                                                 new long[] {1,1,1});
      lDestImageAndFlag.setLeft(true);

      return true;
    }
    return false;
  }
}
