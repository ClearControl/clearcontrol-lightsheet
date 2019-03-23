package clearcontrol.microscope.lightsheet.timelapse.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.timelapse.TimelapseInterface;

/**
 * The TimelapseStopInstruction stops the running time lapse
 *
 * Author: @haesleinhuepf 05 2018
 */
public class TimelapseStopAfterNIterationsInstruction extends
                                      LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface
{
  private long counter = 0;

  private BoundedVariable<Integer> maximumCount = new BoundedVariable<Integer>("Number of iterations", 5, 0, Integer.MAX_VALUE);

  public TimelapseStopAfterNIterationsInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Stop timelapse after n interations", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    counter = maximumCount.get();
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    TimelapseInterface lTimelapse =
                                  (TimelapseInterface) getLightSheetMicroscope().getDevice(TimelapseInterface.class,
                                                                                           0);
    if (lTimelapse != null)
    {
      counter--;
      if (counter <= 0) {
        lTimelapse.stopTimelapse();
      }
    }
    return true;
  }

  @Override
  public TimelapseStopAfterNIterationsInstruction copy()
  {
    return new TimelapseStopAfterNIterationsInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Stopthe timelapse after passing this instruction for a given number of times.";
  }

  public BoundedVariable<Integer> getMaximumCount() {
    return maximumCount;
  }

  @Override
  public Variable[] getProperties() {
    return new Variable[]{
      getMaximumCount()
    };
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[0];
  }
}
