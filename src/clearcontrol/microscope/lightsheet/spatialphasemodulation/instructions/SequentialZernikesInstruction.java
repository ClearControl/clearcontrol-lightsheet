package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;

public class SequentialZernikesInstruction extends InstructionBase
                                           implements
                                           LoggingFeature,
                                           PropertyIOableInstructionInterface
{

  private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;

  private BoundedVariable<Double> mMaximumZernikeCoefficientVariable =
                                                                     new BoundedVariable<Double>("Maximum Zernike coefficient",
                                                                                                 0.5,
                                                                                                 -5.0,
                                                                                                 5.0,
                                                                                                 0.0001);
  private BoundedVariable<Double> mMinimumZernikeCoefficientVariable =
                                                                     new BoundedVariable<Double>("Minimum Zernike coefficient",
                                                                                                 -0.5,
                                                                                                 -5.0,
                                                                                                 5.0,
                                                                                                 0.0001);
  private BoundedVariable<Double> mStepperVariable =
                                                   new BoundedVariable<Double>("Step size",
                                                                               0.01,
                                                                               0.0,
                                                                               Double.MAX_VALUE,
                                                                               0.0001);
  private BoundedVariable<Double> mInitialValueVariable =
                                                        new BoundedVariable<Double>("Initial value",
                                                                                    0.0,
                                                                                    0.0,
                                                                                    Double.MAX_VALUE,
                                                                                    0.0001);
  private BoundedVariable<Integer> mStartingModeVariable =
                                                         new BoundedVariable<Integer>("Starting mode",
                                                                                      3,
                                                                                      0,
                                                                                      Integer.MAX_VALUE);
  private BoundedVariable<Integer> mChangingModeVariable =
                                                         new BoundedVariable<Integer>("Changing mode",
                                                                                      1,
                                                                                      0,
                                                                                      Integer.MAX_VALUE);
  private BoundedVariable<Integer> mEndingModeVariable =
                                                       new BoundedVariable<Integer>("Ending mode",
                                                                                    3,
                                                                                    0,
                                                                                    Integer.MAX_VALUE);

  private double[] mArray;
  private String mDirection;

  public SequentialZernikesInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase)
  {
    super("Adaptive optics: Send sequential mirror modes to "
          + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName());
    mZernikeModeFactorBasedSpatialPhaseModulatorBase =
                                                     pZernikeModeFactorBasedSpatialPhaseModulatorBase;
    mMaximumZernikeCoefficientVariable.set(0.5);
    mMinimumZernikeCoefficientVariable.set(-0.5);
    mStepperVariable.set(0.01);
    mInitialValueVariable.set(0.0);
    mStartingModeVariable.set(3);
    mEndingModeVariable.set(3);
  }

  @Override
  public boolean initialize()
  {
    mArray =
           mZernikeModeFactorBasedSpatialPhaseModulatorBase.getZernikeFactors();
    for (int i = 0; i < mArray.length; i++)
    {
      mArray[i] = mInitialValueVariable.get();
    }
    mChangingModeVariable = mStartingModeVariable;
    mDirection = "positive";
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    if (mStepperVariable.get() > (mMaximumZernikeCoefficientVariable.get()
                                  - mInitialValueVariable.get()))
    {
      System.out.println("Stepper is too big");
      return false;
    }
    if (mChangingModeVariable.get() <= mEndingModeVariable.get())
    {
      switch (mDirection)
      {
      case "positive":
      {
        mArray[mChangingModeVariable.get()] =
                                            mArray[mChangingModeVariable.get()]
                                              + mStepperVariable.get();
        break;
      }

      case "negative":
      {
        mArray[mChangingModeVariable.get()] =
                                            mArray[mChangingModeVariable.get()]
                                              - mStepperVariable.get();
        break;
      }
      }

    }
    mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(mArray);
    if (mArray[mChangingModeVariable.get()] >= mMaximumZernikeCoefficientVariable.get())
    {
      mDirection = "negative";
      mArray[mChangingModeVariable.get()] =
                                          mInitialValueVariable.get();
    }
    else if (mArray[mChangingModeVariable.get()] <= mMinimumZernikeCoefficientVariable.get())
    {
      mDirection = "positive";
      mArray[mChangingModeVariable.get()] =
                                          mInitialValueVariable.get();
      mChangingModeVariable.set(mChangingModeVariable.get() + 1);
    }

    return true;

  }

  @Override
  public SequentialZernikesInstruction copy()
  {
    return new SequentialZernikesInstruction(mZernikeModeFactorBasedSpatialPhaseModulatorBase);
  }

  public BoundedVariable<Double> getInitialValueVariable()
  {
    return mInitialValueVariable;
  }

  public BoundedVariable<Double> getMaximumZernikeCoefficientVariable()
  {
    return mMaximumZernikeCoefficientVariable;
  }

  public BoundedVariable<Double> getMinimumZernikeCoefficientVariable()
  {
    return mMinimumZernikeCoefficientVariable;
  }

  public BoundedVariable<Double> getStepperVariable()
  {
    return mStepperVariable;
  }

  public BoundedVariable<Integer> getEndingModeVariable()
  {
    return mEndingModeVariable;
  }

  /*public BoundedVariable<Integer> getChangingModeVariable() {
      return mChangingModeVariable;
  }*/

  public BoundedVariable<Integer> getStartingModeVariable()
  {
    return mStartingModeVariable;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getStartingModeVariable(),
      getEndingModeVariable(),
      getInitialValueVariable(),
      getMinimumZernikeCoefficientVariable(),
      getMaximumZernikeCoefficientVariable(),
      getStepperVariable() };
  }
}
