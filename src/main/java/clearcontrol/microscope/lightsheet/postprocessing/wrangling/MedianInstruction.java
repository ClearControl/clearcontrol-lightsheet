package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * DownsampleByHalfMedianInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class MedianInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after filtering", true);
    protected BoundedVariable<Integer> radius = new BoundedVariable<Integer>("Radius in pixels", 2, 1, Integer.MAX_VALUE);

    public MedianInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Median in XY", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        ClearCLBuffer inputCL = clij.convert(stack, ClearCLBuffer.class);
        ClearCLBuffer outputCL3D = clij.create(new long[]{inputCL.getWidth(), inputCL.getHeight(), inputCL.getDepth()}, inputCL.getNativeType());

        clij.op().medianSliceBySliceSphere(inputCL, outputCL3D, radius.get(), radius.get());

        StackInterface resultStack = clij.convert(outputCL3D, StackInterface.class);
        StackMetaData metaData = stack.getMetaData().clone();
        if (metaData != null) {
            metaData.setVoxelDimX(metaData.getVoxelDimX() * 2);
            metaData.setVoxelDimY(metaData.getVoxelDimY() * 2);
        }
        resultStack.setMetaData(metaData);

        inputCL.close();
        outputCL3D.close();

        return resultStack;
    }

    @Override
    public MedianInstruction copy() {
        MedianInstruction copied = new MedianInstruction(getDataWarehouse());
        copied.recycleSavedContainers.set(recycleSavedContainers.get());
        copied.radius.set(radius.get());
        return copied;
    }

    @Override
    public String getDescription() {
        return "Apply a median filter in XY.";
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
        return new Variable[]{getRecycleSavedContainers(), getRadius()};
    }

    public BoundedVariable<Integer> getRadius() {
        return radius;
    }
}