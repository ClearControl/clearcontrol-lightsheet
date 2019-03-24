package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * MaximumProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class MaximumProjectionInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after projecting", true);


    public MaximumProjectionInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Maximum projection", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        ClearCLImage inputCL = clij.convert(stack, ClearCLImage.class);
        ClearCLImage outputCL2D = clij.createCLImage(new long[]{inputCL.getWidth(), inputCL.getHeight()}, inputCL.getChannelDataType());
        ClearCLImage outputCL3D = clij.createCLImage(new long[]{inputCL.getWidth(), inputCL.getHeight(), 1}, inputCL.getChannelDataType());

        clij.op().maximumZProjection(inputCL, outputCL2D);
        clij.op().copySlice(outputCL2D, outputCL3D, 0);

        StackInterface resultStack = clij.convert(outputCL3D, StackInterface.class);
        resultStack.setMetaData(stack.getMetaData().clone());

        inputCL.close();
        outputCL2D.close();
        outputCL3D.close();

        return resultStack;
    }

    @Override
    public MaximumProjectionInstruction copy() {
        return new MaximumProjectionInstruction(getDataWarehouse());
    }

    @Override
    public String getDescription() {
        return "Produce maximum projections of all image stacks in a given container.";
    }

    public Variable<Boolean> getRecycleSavedContainers() {
        return recycleSavedContainers;
    }

    @Override
    public Class[] getProducedContainerClasses() {
        return new Class[]{StackInterfaceContainer.class};
    }

    @Override
    public Class[] getConsumedContainerClasses() {
        if (!recycleSavedContainers.get()) {
            return new Class[0];
        }
        return new Class[]{StackInterfaceContainer.class};
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {recycleSavedContainers};
    }


}
