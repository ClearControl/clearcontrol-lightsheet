package clearcontrol.microscope.lightsheet.state.spatial;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;

import java.util.ArrayList;

/**
 * The PositionListContainer stores stage positions of the microscope, e.g. for path planning
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class PositionListContainer extends ArrayList<Position> implements DataContainerInterface {

    private final long mTimepoint;

    public PositionListContainer(long pTimepoint) {
        mTimepoint = pTimepoint;
    }

    @Override
    public long getTimepoint() {
        return mTimepoint;
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
        clear();
    }
}
