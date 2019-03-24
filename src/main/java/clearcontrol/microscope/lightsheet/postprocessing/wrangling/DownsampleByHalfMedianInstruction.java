package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * DownsampleByHalfMedianInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class DownsampleByHalfMedianInstruction  extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after downsampling", true);

    public DownsampleByHalfMedianInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Downsample XY by half (median)", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        ClearCLImage inputCL = clij.convert(stack, ClearCLImage.class);
        ClearCLImage outputCL3D = clij.createCLImage(new long[]{inputCL.getWidth() / 2, inputCL.getHeight() / 2, inputCL.getDepth()}, inputCL.getChannelDataType());

        clij.op().downsampleSliceBySliceHalfMedian(inputCL, outputCL3D);

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
    public DownsampleByHalfMedianInstruction copy() {
        return new DownsampleByHalfMedianInstruction(getDataWarehouse());
    }

    @Override
    public String getDescription() {
        return "Downsample all image stacks in a container by a factor of 2 in X/Y by taking the median of 2x2 pixel blocks.";
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
        return new Variable[]{getRecycleSavedContainers()};
    }
}