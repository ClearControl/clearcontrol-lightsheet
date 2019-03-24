package clearcontrol.microscope.lightsheet.imaging.opticsprefused;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

/**
 * This FusionInstruction takes the oldest OpticsPrefusedImageDataContainer from
 * the DataWarehouse and fuses the images. Results are saved as
 * FusedImageContainer back to the DataWarehouse.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) April 2018
 */
public class OpticsPrefusedFusionInstruction extends FusionInstruction
                                             implements
                                             InstructionInterface,
                                             LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   *
   */
  public OpticsPrefusedFusionInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Post-processing: Optics prefused fusion",
          pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    DataWarehouse lDataWarehouse =
                                 getLightSheetMicroscope().getDataWarehouse();
    final OpticsPrefusedImageDataContainer lContainer =
                                                      lDataWarehouse.getOldestContainer(OpticsPrefusedImageDataContainer.class);
    String[] lInputImageKeys =
                             new String[getLightSheetMicroscope().getNumberOfDetectionArms()];

    int count = 0;
    for (int d =
               0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
    {
      lInputImageKeys[count] = "C" + d + "opticsprefused";
      count++;
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
  public OpticsPrefusedFusionInstruction copy()
  {
    return new OpticsPrefusedFusionInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Fuses image stacks acquired by optics-prefused acquisition.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[] {FusedImageDataContainer.class};
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    if (!recycleSavedContainers.get()) {
      return new Class[0];
    }
    return new Class[]{OpticsPrefusedImageDataContainer.class};
  }
}
