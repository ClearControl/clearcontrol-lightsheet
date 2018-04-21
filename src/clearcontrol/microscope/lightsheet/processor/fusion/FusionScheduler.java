package clearcontrol.microscope.lightsheet.processor.fusion;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public abstract class FusionScheduler extends SchedulerBase implements
                                                   LoggingFeature
{

  private static Object mLock = new Object();
  private StackInterface mFusedStack = null;

  protected LightSheetMicroscope mLightSheetMicroscope;

  private final RecyclerInterface<StackInterface, StackRequest>
      mRecycler;

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pDeviceName device name
   * @param pRecycler
   */
  public FusionScheduler(String pDeviceName,
                         RecyclerInterface<StackInterface, StackRequest> pRecycler)
  {
    super(pDeviceName);
    mRecycler = pRecycler;
  }

  @Override public boolean initialize()
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("I'm only compatible to LightSheetMicroscopes!");
      return false;
    }

    mLightSheetMicroscope =
        (LightSheetMicroscope) mMicroscope;
    return true;
  }

  protected StackInterface fuseStacks(StackInterfaceContainer pContainer, String[] pImageKeys)
  {
    ElapsedTime.measure("Handle container (" + pContainer + ") and fuse", () ->
    {
      synchronized (mLock)
      {
        for (String key : pImageKeys)
        {
          StackInterface lResultingStack = pContainer.get(key);

          LightSheetFastFusionProcessor
              lProcessor =
              mLightSheetMicroscope.getDevice(
                  LightSheetFastFusionProcessor.class,
                  0);

          info("sending: " + lResultingStack);
          StackInterface
              lStackInterface =
              lProcessor.process(lResultingStack, mRecycler);
          info("Got back: " + lStackInterface);
          if (lStackInterface != null)
          {
            mFusedStack = lStackInterface;
          }
        }
      }
    });

    return mFusedStack;
  }

  protected void storeFusedContainer(StackInterface lFusedStack) {
    DataWarehouse lDataWarehouse = mLightSheetMicroscope.getDataWarehouse();
    FusedImageDataContainer
        lFusedContainer = new FusedImageDataContainer(mLightSheetMicroscope);
    lFusedContainer.put("fused", lFusedStack);
    lDataWarehouse.put("fused", lFusedContainer);
  }

  public StackInterface getFusedStack()
  {
    return mFusedStack;
  }
}
