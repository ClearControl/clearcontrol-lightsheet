package clearcontrol.microscope.lightsheet.imaging.singleview;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

public class ViewSingleLightSheetStackScheduler extends SchedulerBase implements
        LoggingFeature
{

    private final int mDetectionArmIndex;
    private final int mLightSheetIndex;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ViewSingleLightSheetStackScheduler(int pDetectionArmIndex, int pLightSheetIndex) {
        super("Visualisation: View C" + pDetectionArmIndex + "L" + pLightSheetIndex + " stack");
        mDetectionArmIndex = pDetectionArmIndex;
        mLightSheetIndex = pLightSheetIndex;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            return false;
        }
        DataWarehouse lDataWarehouse = ((LightSheetMicroscope) mMicroscope).getDataWarehouse();
        StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(StackInterfaceContainer.class);
        if (lContainer == null || !lContainer.isDataComplete()) {
            return false;
        }

        Stack3DDisplay lDisplay = (Stack3DDisplay) mMicroscope.getDevice(Stack3DDisplay.class, 0);
        if (lDisplay == null) {
            return false;
        }

        lDisplay.getInputStackVariable().set(lContainer.get("C" + mDetectionArmIndex + "L" + mLightSheetIndex));

        return true;
    }
}

