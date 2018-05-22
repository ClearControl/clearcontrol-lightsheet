package clearcontrol.microscope.lightsheet.postprocessing.measurements;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * TimeStampContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class TimeStampContainer extends DataContainerBase {

    private final long mTimeStampInNanoSeconds;

    public TimeStampContainer(long pTimepoint, long pTimeStampInNanoSeconds) {
        super(pTimepoint);

        mTimeStampInNanoSeconds = pTimeStampInNanoSeconds;
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {

    }

    public long getTimeStampInNanoSeconds() {
        return mTimeStampInNanoSeconds;
    }
}
