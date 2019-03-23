package clearcontrol.microscope.lightsheet.timelapse.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import javolution.io.Struct;

import java.util.ArrayList;

/**
 * RemovePredecessorInstructionsInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public class RemovePredecessorInstructionsInstruction extends LightSheetMicroscopeInstructionBase {
    private Variable<Integer> numberOfInstructionsToRemove = new Variable<Integer>("Number of instructions to remove ", 1);
    private Variable<Boolean> removeMyselfAsWell = new Variable<Boolean>("Remove myself as well", true);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public RemovePredecessorInstructionsInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Remove predecessor instruction", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        ArrayList<InstructionInterface> program = getLightSheetMicroscope().getTimelapse().getCurrentProgram();

        int i = (int) getLightSheetMicroscope().getTimelapse().getLastExecutedSchedulerIndexVariable().get();
        if (removeMyselfAsWell.get()) {
            program.remove(i);
        }
        i--;
        for (; i >= 0; i--) {
            program.remove(i);
        }
        getLightSheetMicroscope().getTimelapse().getLastExecutedSchedulerIndexVariable().set(i);
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        return true;
    }

    @Override
    public InstructionInterface copy() {
        RemovePredecessorInstructionsInstruction copied = new RemovePredecessorInstructionsInstruction(getLightSheetMicroscope());
        return copied;
    }

    @Override
    public String getDescription() {
        return "Removes the instruction before this one and itself in the instruction list.";
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
