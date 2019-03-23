package clearcontrol.devices.stages.kcube.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.state.spatial.PositionListContainer;

/**
 * The SpaceTravelInstruction allows to move the FOV between timepoints along a
 * given travel route. It works by moving three BasicStages: X, Y and Z
 *
 * Author: @haesleinhuepf 04 2018
 */
public class SpaceTravelInstruction extends
                                    LightSheetMicroscopeInstructionBase
                                    implements
                                    PropertyIOableInstructionInterface
{

  private int mCurrentTravelPathPosition = 0;
  private PositionListContainer mTravelPath =
                                            new PositionListContainer(-1);

  BasicStageInterface mStageX = null;
  BasicStageInterface mStageY = null;
  BasicStageInterface mStageZ = null;

  private BoundedVariable<Integer> mSleepAfterMotionInMilliSeconds =
                                                                   new BoundedVariable<Integer>("Sleep after motion in ms",
                                                                                                1000,
                                                                                                0,
                                                                                                Integer.MAX_VALUE);

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public SpaceTravelInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Move X/Y/Z stage along position list",
          pLightSheetMicroscope);
  }

  public SpaceTravelInstruction(String pDeviceName,
                                LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    mCurrentTravelPathPosition = -1;
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    if (mTravelPath.size() == 0)
    {
      return false;
    }
    if (!initializeStages())
    {
      return false;
    }
    mCurrentTravelPathPosition++;
    if (mCurrentTravelPathPosition > mTravelPath.size() - 1)
    {
      mCurrentTravelPathPosition = 0;
    }

    goToPosition(mCurrentTravelPathPosition);
    return true;
  }

  public boolean goToPosition(int pTargetTravelPathPosition)
  {
    if (!initializeStages())
    {
      return false;
    }
    Position target = mTravelPath.get(pTargetTravelPathPosition);

    mStageX.moveBy(target.mX - mStageX.getPositionVariable().get(),
                   true);
    mStageY.moveBy(target.mY - mStageY.getPositionVariable().get(),
                   true);
    mStageZ.moveBy(target.mZ - mStageZ.getPositionVariable().get(),
                   true);

    try
    {
      Thread.sleep(mSleepAfterMotionInMilliSeconds.get());
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    return true;
  }

  public boolean appendCurrentPositionToPath(int lTargetIndex)
  {
    if (!initializeStages())
    {
      return false;
    }

    Position here = new Position(mStageX.getPositionVariable().get(),
                                 mStageY.getPositionVariable().get(),
                                 mStageZ.getPositionVariable().get());
    mTravelPath.add(lTargetIndex, here);

    return true;
  }

  private boolean initializeStages()
  {
    if (mStageX != null && mStageY != null && mStageZ != null)
    {
      return true;
    }

    for (BasicStageInterface lStage : getLightSheetMicroscope().getDevices(BasicStageInterface.class))
    {
      if (lStage.toString().contains("X"))
      {
        mStageX = lStage;
      }
      if (lStage.toString().contains("Y"))
      {
        mStageY = lStage;
      }
      if (lStage.toString().contains("Z"))
      {
        mStageZ = lStage;
      }
    }
    return mStageX != null && mStageY != null && mStageZ != null;

  }

  public BoundedVariable<Integer> getSleepAfterMotionInMilliSeconds()
  {
    return mSleepAfterMotionInMilliSeconds;
  }

  public PositionListContainer getTravelPathList()
  {
    return mTravelPath;
  }

  @Override
  public SpaceTravelInstruction copy()
  {
    return this;
    // new SpaceTravelInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Move an X/Y/Z stage to the next position in a given list of positions.";
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { mTravelPath.getAsStringVariable() };
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
