package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;

/**
 * RestartTimelapseWhileNoSampleChosenInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class RestartTimelapseWhileNoSampleChosenInstruction extends
                                                            LightSheetMicroscopeInstructionBase
{

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public RestartTimelapseWhileNoSampleChosenInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Restart timelapse while no sample is chosen",
          pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    SpaceTravelInstruction spaceTravelScheduler =
                                                getLightSheetMicroscope().getDevice(SpaceTravelInstruction.class,
                                                                                    0);
    if (spaceTravelScheduler.getTravelPathList().size() > 1)
    {
      getLightSheetMicroscope().getTimelapse()
                               .getLastExecutedSchedulerIndexVariable()
                               .set(-1);
    }
    return true;
  }

  @Override
  public RestartTimelapseWhileNoSampleChosenInstruction copy()
  {
    return new RestartTimelapseWhileNoSampleChosenInstruction(getLightSheetMicroscope());
  }
}
