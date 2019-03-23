package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * The WriteAllStacksAsRawToDiscInstruction gets the oldest container of a given
 * class and stores all its image stacks to disc.
 *
 * Author: @haesleinhuepf September 2018
 */
public class WriteAllStacksAsRawToDiscInstruction extends
                                                  WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
  public WriteAllStacksAsRawToDiscInstruction(Class pContainerClass,
                                              LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Write all stacks in " + pContainerClass.getSimpleName()
          + " as RAW to disc",
          pContainerClass,
          null,
          null,
          pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse =
                                 ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();
    StackInterfaceContainer container =
                                      lDataWarehouse.getOldestContainer(mContainerClass);

    mImageKeys = new String[container.keySet().size()];
    int count = 0;
    for (String key : container.keySet())
    {
      mImageKeys[count] = key;
      count++;
    }

    return super.enqueue(pTimePoint);
  }

  @Override
  public WriteAllStacksAsRawToDiscInstruction copy()
  {
    return new WriteAllStacksAsRawToDiscInstruction(mContainerClass,
                                                    getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Write image stacks from a given container to disc.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{StackInterfaceContainer.class};
  }
}
