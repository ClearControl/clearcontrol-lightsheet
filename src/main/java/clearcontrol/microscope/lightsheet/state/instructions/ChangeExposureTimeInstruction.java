package clearcontrol.microscope.lightsheet.state.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;

/**
 * ChangeExposureTimeInstruction
 *
 * Author: @haesleinhuepf 05 2018
 */
public class ChangeExposureTimeInstruction extends
                                           LightSheetMicroscopeInstructionBase
                                           implements
                                           PropertyIOableInstructionInterface
{
  BoundedVariable<Double> mExposureTimeInSecondsVariable =
                                                         new BoundedVariable<Double>("Exposure time in seconds",
                                                                                     0.01,
                                                                                     0.0,
                                                                                     Double.MAX_VALUE,
                                                                                     0.00001);

  public ChangeExposureTimeInstruction(double pExposureTimeInSeconds,
                                       LightSheetMicroscope pLightSheetMicroscope)
  {

    super("Adaptation: Change exposure time to "
          + pExposureTimeInSeconds + " s", pLightSheetMicroscope);
    mExposureTimeInSecondsVariable.set(pExposureTimeInSeconds);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    getLightSheetMicroscope().getAcquisitionStateManager()
                             .getCurrentState()
                             .getExposureInSecondsVariable()
                             .set(mExposureTimeInSecondsVariable.get());
    return true;
  }

  @Override
  public ChangeExposureTimeInstruction copy()
  {
    return new ChangeExposureTimeInstruction(mExposureTimeInSecondsVariable.get(),
                                             getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Change the exposure time.";
  }

  public BoundedVariable<Double> getExposureTimeInSecondsVariable()
  {
    return mExposureTimeInSecondsVariable;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getExposureTimeInSecondsVariable() };
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
