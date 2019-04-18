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
public class TopHatInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after filtering", true);
    protected BoundedVariable<Integer> radiusXY = new BoundedVariable<Integer>("Radius in XY in pixels", 2, 0, Integer.MAX_VALUE);
    protected BoundedVariable<Integer> radiusZ = new BoundedVariable<Integer>("Radius in Z in pixels", 0, 0, Integer.MAX_VALUE);

    public TopHatInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Top-hat filter", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        ClearCLBuffer inputCL = clij.convert(stack, ClearCLBuffer.class);
        ClearCLBuffer tempCL3D = clij.create(inputCL);
        ClearCLBuffer temp2CL3D = clij.create(inputCL);
        ClearCLBuffer outputCL3D = clij.create(inputCL);

        clij.op().minimumBox(inputCL, tempCL3D, radiusXY.get(), radiusXY.get(), radiusZ.get());
        clij.op().maximumBox(tempCL3D, temp2CL3D, radiusXY.get(), radiusXY.get(), radiusZ.get());
        clij.op().subtractImages(inputCL, temp2CL3D, outputCL3D);

        StackInterface resultStack = clij.convert(outputCL3D, StackInterface.class);
        StackMetaData metaData = stack.getMetaData().clone();
        if (metaData != null) {
            metaData.setVoxelDimX(metaData.getVoxelDimX() * 2);
            metaData.setVoxelDimY(metaData.getVoxelDimY() * 2);
        }
        resultStack.setMetaData(metaData);

        inputCL.close();
        tempCL3D.close();
        temp2CL3D.close();
        outputCL3D.close();

        return resultStack;
    }

    @Override
    public TopHatInstruction copy() {
        TopHatInstruction copied = new TopHatInstruction(getDataWarehouse());
        copied.recycleSavedContainers.set(recycleSavedContainers.get());
        copied.radiusXY.set(radiusXY.get());
        copied.radiusZ.set(radiusZ.get());
        return copied;
    }

    @Override
    public String getDescription() {
        return "Apply a top-hat filter.";
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
        return new Variable[]{getRecycleSavedContainers(), getRadiusXY(), getRadiusZ()};
    }

    public BoundedVariable<Integer> getRadiusXY() {
        return radiusXY;
    }

    public BoundedVariable<Integer> getRadiusZ() {
        return radiusZ;
    }
}