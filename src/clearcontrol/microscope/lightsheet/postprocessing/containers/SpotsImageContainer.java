package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * SpotsImageContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class SpotsImageContainer extends StackInterfaceContainer
{
  public SpotsImageContainer(long pTimePoint)
  {
    super(pTimePoint);
  }

  @Override
  public boolean isDataComplete()
  {
    return keySet().contains("spots");
  }
}
