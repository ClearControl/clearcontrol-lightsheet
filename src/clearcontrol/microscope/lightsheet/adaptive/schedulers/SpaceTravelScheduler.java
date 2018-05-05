package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.state.spatial.PositionListContainer;

import java.util.ArrayList;

/**
 * The SpaceTravelScheduler allows to move the FOV between timepoints along a given travel route. It works by moving
 * three BasicStages: X, Y and Z
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class SpaceTravelScheduler extends SchedulerBase {


    private int mCurrentTravelPathPosition = 0;
    private PositionListContainer mTravelPath = new PositionListContainer(-1);

    BasicStageInterface mStageX = null;
    BasicStageInterface mStageY = null;
    BasicStageInterface mStageZ = null;

    private BoundedVariable<Integer> mSleepAfterMotionInMilliSeconds = new BoundedVariable<Integer>("Sleep after motion in ms", 1000, 0, Integer.MAX_VALUE);

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public SpaceTravelScheduler() {
        super("Adaptation: Move in space");
    }

    public SpaceTravelScheduler(String pDeviceName) {
        super(pDeviceName);
    }


    @Override
    public boolean initialize() {
        mCurrentTravelPathPosition = -1;
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (mTravelPath.size() == 0) {
            return false;
        }
        if (!initializeStages()) {
            return false;
        }
        mCurrentTravelPathPosition ++;
        if (mCurrentTravelPathPosition > mTravelPath.size() - 1) {
            mCurrentTravelPathPosition = 0;
        }

        goToPosition(mCurrentTravelPathPosition);
        return true;
    }

    public boolean goToPosition(int pTargetTravelPathPosition) {
        Position target = mTravelPath.get(pTargetTravelPathPosition);

        mStageX.moveBy(target.mX - mStageX.getPositionVariable().get(), true);
        mStageY.moveBy(target.mY - mStageY.getPositionVariable().get(), true);
        mStageZ.moveBy(target.mZ - mStageZ.getPositionVariable().get(), true);

        try {
            Thread.sleep(mSleepAfterMotionInMilliSeconds.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean appendCurrentPositionToPath(int lTargetIndex) {
        if (!initializeStages()) {
            return false;
        }

        Position here = new Position(mStageX.getPositionVariable().get(),
                mStageY.getPositionVariable().get(),
                mStageZ.getPositionVariable().get());
        mTravelPath.add(lTargetIndex, here);

        return true;
    }

    private boolean initializeStages() {
        if (mStageX != null && mStageY != null && mStageZ != null) {
            return true;
        }
        if (mMicroscope instanceof LightSheetMicroscope) {
            LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope)mMicroscope;
            for (BasicStageInterface lStage : lLightSheetMicroscope.getDevices(BasicStageInterface.class)) {
                if (lStage.toString().contains("X")) {
                    mStageX = lStage;
                }
                if (lStage.toString().contains("Y")) {
                    mStageY = lStage;
                }
                if (lStage.toString().contains("Z")) {
                    mStageZ = lStage;
                }
            }
            return mStageX != null && mStageY != null && mStageZ != null;
        }
        return false;
    }

    public BoundedVariable<Integer> getSleepAfterMotionInMilliSeconds() {
        return mSleepAfterMotionInMilliSeconds;
    }

    public PositionListContainer getTravelPathList() {
        return mTravelPath;
    }

}
