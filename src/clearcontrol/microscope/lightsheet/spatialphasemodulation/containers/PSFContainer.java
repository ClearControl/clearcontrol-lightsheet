package clearcontrol.microscope.lightsheet.spatialphasemodulation.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

/**
 * PSFContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class PSFContainer extends StackInterfaceContainer {
    public PSFContainer(long pTimePoint) {
        super(pTimePoint);
    }

    @Override
    public boolean isDataComplete() {
        return containsKey("PSF");
    }

    public StackInterface getPSF() {
        return get("PSF");
    }

    public void setPSF(StackInterface pStack) {
        put("PSF", pStack);
    }
}
