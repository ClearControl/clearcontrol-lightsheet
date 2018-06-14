package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

/**
 * SingleCameraFusionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class SingleCameraFusionInstruction extends FusionInstruction {
    BoundedVariable<Integer> mCameraIndexVariable;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public SingleCameraFusionInstruction(LightSheetMicroscope pLightSheetMicroscope, int pCameraIndex) {
        super("Post-processing: Single camera fusion C" + pCameraIndex, pLightSheetMicroscope);
        mCameraIndexVariable = new BoundedVariable<Integer>("Camera index", pCameraIndex, 0, pLightSheetMicroscope.getNumberOfDetectionArms());
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        resetEngine(getLightSheetMicroscope().getNumberOfLightSheets(), 1);

        StackInterfaceContainer lContainerToFuse = new StackInterfaceContainer(pTimePoint) {
            @Override
            public boolean isDataComplete() {
                return true;
            }
        };

        String[] keys = new String[getLightSheetMicroscope().getNumberOfLightSheets()];
        StackInterfaceContainer lContainerFromWarehouse = getLightSheetMicroscope().getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);
        for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++) {
            StackInterface lStack = lContainerFromWarehouse.get("C" + mCameraIndexVariable.get() + "L" + l);
            keys[l] = "C0L" + l;
            lContainerToFuse.put(keys[l], lStack);
        }

        fuseStacks(lContainerToFuse, keys);
        return false;
    }

    @Override
    public InstructionInterface copy() {
        return new SingleCameraFusionInstruction(getLightSheetMicroscope(), mCameraIndexVariable.get());
    }

    public BoundedVariable<Integer> getCameraIndexVariable() {
        return mCameraIndexVariable;
    }
}
