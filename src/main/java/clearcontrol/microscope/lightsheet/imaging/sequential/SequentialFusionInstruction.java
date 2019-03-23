package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

/**
 * This FusionInstruction takes the oldest SequentialImageDataContainer from the
 * DataWarehouse and fuses the images. Results are saved as FusedImageContainer
 * back to the DataWarehouse.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class SequentialFusionInstruction extends FusionInstruction
                                         implements
                                         InstructionInterface,
                                         LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public SequentialFusionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Post-processing: Sequential fusion",
          pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse =
                                 getLightSheetMicroscope().getDataWarehouse();
    final SequentialImageDataContainer lContainer =
                                                  lDataWarehouse.getOldestContainer(SequentialImageDataContainer.class);
    String[] lInputImageKeys =
                             new String[getLightSheetMicroscope().getNumberOfDetectionArms()
                                        * getLightSheetMicroscope().getNumberOfLightSheets()];

    int count = 0;
    for (int l =
               0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
    {
      for (int d =
                 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
      {
        lInputImageKeys[count] = "C" + d + "L" + l;
        count++;
      }
    }

    StackInterface lFusedStack = fuseStacks(lContainer,
                                            lInputImageKeys);
    if (lFusedStack == null)
    {
      return false;
    }

    storeFusedContainer(lFusedStack);
    return true;
  }

  @Override
  public SequentialFusionInstruction copy()
  {
    return new SequentialFusionInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Fuses image stacks acquired by sequential acquisition.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[]{FusedImageDataContainer.class};
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{SequentialImageDataContainer.class};
  }
}
