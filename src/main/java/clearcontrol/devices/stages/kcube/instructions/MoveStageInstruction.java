package clearcontrol.devices.stages.kcube.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;

/**
 * MoveStageInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public class MoveStageInstruction extends InstructionBase implements PropertyIOableInstructionInterface {
    private BoundedVariable<Double> delta = new BoundedVariable<Double>("Move by (mm)", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);

    BasicStageInterface stage;

    public MoveStageInstruction(BasicStageInterface stage) {
        super("Smart: Move stage " + stage);
        this.stage = stage;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        stage.moveBy(delta.get(), true);
        return true;
    }

    @Override
    public MoveStageInstruction copy() {
        MoveStageInstruction copied = new MoveStageInstruction(stage);
        copied.delta.set(delta.get());
        return copied;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                delta
        };
    }
}
