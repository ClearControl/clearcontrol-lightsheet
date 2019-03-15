package clearcontrol.microscope.lightsheet.warehouse.containers;

/**
 * DefaultStackInterfaceContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 03 2019
 */
public class DefaultStackInterfaceContainer extends StackInterfaceContainer {

    public DefaultStackInterfaceContainer(long pTimePoint) {
        super(pTimePoint);
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }
}
