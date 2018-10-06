package clearcontrol.instructions;

import java.util.ArrayList;

/**
 * ExecutableInstructionList
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public class ExecutableInstructionList<M extends HasInstructions> extends ArrayList<InstructionInterface> {

    private M instructionSource;

    public ExecutableInstructionList(M instructionSource) {
        this.instructionSource = instructionSource;
    }

    public M getInstructionSource() {
        return instructionSource;
    }
}
