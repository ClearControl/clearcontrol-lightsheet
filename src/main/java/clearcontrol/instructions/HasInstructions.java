package clearcontrol.instructions;

import java.util.ArrayList;

/**
 * HasInstructions
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public interface HasInstructions {
    public InstructionInterface getInstruction(String... mustContainString);
    public ArrayList<InstructionInterface> getInstructions(String... mustContainString);
}
