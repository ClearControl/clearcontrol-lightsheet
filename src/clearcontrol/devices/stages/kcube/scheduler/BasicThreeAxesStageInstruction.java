package clearcontrol.devices.stages.kcube.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.SchedulerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class BasicThreeAxesStageInstruction extends InstructionBase implements
                                                                SchedulerInterface,
                                                                LoggingFeature
{

  private BoundedVariable<Double> mStartXVariable;
  private BoundedVariable<Double> mStartYVariable;
  private BoundedVariable<Double> mStartZVariable;
  private BoundedVariable<Double> mStopXVariable;
  private BoundedVariable<Double> mStopYVariable;
  private BoundedVariable<Double> mStopZVariable;
  private BoundedVariable<Integer> mNumberOfStepsVariable;
  private Variable<Boolean> mRestartAfterFinishVariable = new Variable<Boolean>("Restart after finish", true);

  private BasicThreeAxesStageInterface mBasicThreeAxesStageInterface;

  private int mStepCount = 0;

  public BasicThreeAxesStageInstruction(BasicThreeAxesStageInterface pBasicThreeAxesStageInterface) {
    super("Smart: Move X/Y/Z stages linear in space");
    mBasicThreeAxesStageInterface = pBasicThreeAxesStageInterface;

    mStartXVariable = new BoundedVariable<Double>("Start X", pBasicThreeAxesStageInterface.getXPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mStartYVariable = new BoundedVariable<Double>("Start Y", pBasicThreeAxesStageInterface.getYPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mStartZVariable = new BoundedVariable<Double>("Start Z", pBasicThreeAxesStageInterface.getZPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mStopXVariable = new BoundedVariable<Double>("Stop X", pBasicThreeAxesStageInterface.getXPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mStopYVariable = new BoundedVariable<Double>("Stop Y", pBasicThreeAxesStageInterface.getYPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mStopZVariable = new BoundedVariable<Double>("Stop Z", pBasicThreeAxesStageInterface.getZPositionVariable().get(), -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);
    mNumberOfStepsVariable = new BoundedVariable<Integer>("Number of steps", 10, 0, Integer.MAX_VALUE);
  }


  @Override public boolean initialize()
  {
    info("Go to start position");
    double
        stepDistanceX = mStartXVariable.get() - mBasicThreeAxesStageInterface.getXPositionVariable().get();
    double
        stepDistanceY = mStartYVariable.get() - mBasicThreeAxesStageInterface.getYPositionVariable().get();
    double
        stepDistanceZ = mStartZVariable.get() - mBasicThreeAxesStageInterface.getZPositionVariable().get();

    mBasicThreeAxesStageInterface.moveXBy(stepDistanceX, true);
    mBasicThreeAxesStageInterface.moveYBy(stepDistanceY, true);
    mBasicThreeAxesStageInterface.moveZBy(stepDistanceZ, true);

    mStepCount = 0;

    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    double
        lStepDistanceX =
        (mStopXVariable.get() - mStartXVariable.get()) / (
            mNumberOfStepsVariable.get()
            - 1);
    double
        lStepDistanceY =
        (mStopYVariable.get() - mStartYVariable.get()) / (
            mNumberOfStepsVariable.get()
            - 1);
    double
        lStepDistanceZ =
        (mStopZVariable.get() - mStartZVariable.get()) / (
            mNumberOfStepsVariable.get()
            - 1);

    if (mStepCount > mNumberOfStepsVariable.get()) {
      initialize();
    }
    mBasicThreeAxesStageInterface.moveXBy(lStepDistanceX, true);
    mBasicThreeAxesStageInterface.moveYBy(lStepDistanceY, true);
    mBasicThreeAxesStageInterface.moveZBy(lStepDistanceZ, true);

    mStepCount ++;
    
    return true;
  }

  public BoundedVariable<Double> getStartXVariable()
  {
    return mStartXVariable;
  }

  public BoundedVariable<Double> getStartYVariable()
  {
    return mStartYVariable;
  }

  public BoundedVariable<Double> getStartZVariable()
  {
    return mStartZVariable;
  }

  public BoundedVariable<Double> getStopXVariable()
  {
    return mStopXVariable;
  }

  public BoundedVariable<Double> getStopYVariable()
  {
    return mStopYVariable;
  }

  public BoundedVariable<Double> getStopZVariable()
  {
    return mStopZVariable;
  }

  public BoundedVariable<Integer> getNumberOfStepsVariable()
  {
    return mNumberOfStepsVariable;
  }

  public Variable<Boolean> getRestartAfterFinishVariable() {
    return mRestartAfterFinishVariable;
  }
}
