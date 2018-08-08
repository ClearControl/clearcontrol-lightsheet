package clearcontrol.microscope.lightsheet.timelapse.test;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;

import java.io.File;

/**
 * TestInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class TestInstruction extends InstructionBase implements PropertyIOableInstructionInterface {
    public Variable<File> mFile = new Variable<File>("File", new File("test.txt"));
    public Variable<String> mString = new Variable<String>("String", "test");
    public Variable<Integer> mInteger = new Variable<Integer>("Integer", 5);
    public Variable<Double> mDouble = new Variable<Double>("Double", 7.5);
    public Variable<Boolean> mBoolean = new Variable<Boolean>("Boo lean", true);

    public TestInstruction() {
        super("IO: Test instruction");
    }


    @Override
    public boolean initialize() {
        return false;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        return false;
    }

    @Override
    public InstructionInterface copy() {
        return null;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                mFile,
                mString,
                mInteger,
                mDouble,
                mBoolean
        };
    }
}
