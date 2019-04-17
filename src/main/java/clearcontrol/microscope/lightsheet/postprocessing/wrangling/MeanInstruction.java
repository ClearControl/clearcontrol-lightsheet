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

/**
 * DownsampleByHalfMedianInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class MeanInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after filtering", true);
    protected BoundedVariable<Integer> radiusXY = new BoundedVariable<Integer>("Radius in XY in pixels", 2, 0, Integer.MAX_VALUE);
    protected BoundedVariable<Integer> radiusZ = new BoundedVariable<Integer>("Radius in Z in pixels", 0, 0, Integer.MAX_VALUE);

    public MeanInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Mean in XY", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        ClearCLBuffer inputCL = clij.convert(stack, ClearCLBuffer.class);
        ClearCLBuffer outputCL3D = clij.create(new long[]{inputCL.getWidth(), inputCL.getHeight(), inputCL.getDepth()}, inputCL.getNativeType());

        clij.op().meanBox(inputCL, outputCL3D, radiusXY.get(), radiusXY.get(), 0);

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
    public MeanInstruction copy() {
        MeanInstruction copied = new MeanInstruction(getDataWarehouse());
        copied.recycleSavedContainers.set(recycleSavedContainers.get());
        copied.radiusXY.set(radiusXY.get());
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
        return new Variable[]{getRecycleSavedContainers(), getRadiusXY()};
    }

    public BoundedVariable<Integer> getRadiusXY() {
        return radiusXY;
    }

    public BoundedVariable<Integer> getRadiusZ() {
        return radiusZ;
    }
}