package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
import clearcontrol.gui.video.video3d.Stack3DDisplay;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

/**
 * ViewStack2DInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class ViewStack2DInstruction extends LightSheetMicroscopeInstructionBase {

    private Variable<String> mKeyToShowVariable = new Variable<String>("Stack to view", "C0L0");
    private BoundedVariable<Integer> mViewerIndexVariable = new BoundedVariable<Integer>("Viewer index", 0, 0, Integer.MAX_VALUE);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public ViewStack2DInstruction(String pStackKey, int pViewerIndex, LightSheetMicroscope pLightSheetMicroscope) {
        super("Visualisation: View stack '" + pStackKey + "' in 2D viewer", pLightSheetMicroscope);
        mKeyToShowVariable.set(pStackKey);
        mViewerIndexVariable.set(pViewerIndex);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
        StackInterfaceContainer lContainer = lDataWarehouse.getOldestContainer(StackInterfaceContainer.class);
        if (lContainer == null) {
            return false;
        }

        Stack2DDisplay lDisplay = (Stack2DDisplay) getLightSheetMicroscope().getDevice(Stack2DDisplay.class, mViewerIndexVariable.get());

        if (lDisplay == null) {
            return false;
        }

        lDisplay.getInputStackVariable().set(lContainer.get(mKeyToShowVariable.get()));


        return true;
    }

    @Override
    public InstructionInterface copy() {
        return new ViewStack2DInstruction(mKeyToShowVariable.get(), mViewerIndexVariable.get(), getLightSheetMicroscope());
    }

    public BoundedVariable<Integer> getViewerIndexVariable() {
        return mViewerIndexVariable;
    }

    public Variable<String> getKeyToShowVariable() {
        return mKeyToShowVariable;
    }
}
