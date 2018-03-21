package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.adaptive.modules.AdaptationModuleInterface;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class AdaptationScheduler extends SchedulerBase implements
                                                        SchedulerInterface,
                                                        LoggingFeature
{
  AdaptiveEngine mAdaptiveEngine;
  Class mTargetAdapationModuleClass;
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public AdaptationScheduler(String pName, Class pTargetAdapationModuleClass)
  {
    super(pName);
    mTargetAdapationModuleClass = pTargetAdapationModuleClass;
  }

  @Override public boolean initialize()
  {
    mAdaptiveEngine =
        (AdaptiveEngine) mMicroscope.getDevice(AdaptiveEngine.class, 0);
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    for (int i = 0; i < mAdaptiveEngine.getModuleList().size(); i++) {
      AdaptationModuleInterface<?>
          lAdaptationModel =
          (AdaptationModuleInterface<?>) mAdaptiveEngine.getModuleList().get(i);
      lAdaptationModel.getIsActiveVariable().set(lAdaptationModel.getClass() == mTargetAdapationModuleClass);
    }

    while(mAdaptiveEngine.step());

    return true;
  }
}