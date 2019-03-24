package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * ResliceInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class ResliceInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface {

    public final static Integer LEFT = 0;
    public final static Integer RIGHT = 1;
    public final static Integer TOP = 2;
    public final static Integer BOTTOM = 3;

    Variable<Integer> direction = new Variable<Integer>("Direction", 0);
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after reslicing", true);

    public ResliceInstruction(DataWarehouse pDataWarehouse) {
        super("Post-processing: Reslicing", pDataWarehouse);
    }



    protected StackInterface processStack(StackInterface stack) {

        CLIJ clij = CLIJ.getInstance();
        ClearCLImage originalCL = clij.convert(stack, ClearCLImage.class);
        ClearCLImage reslicedCL = null;

        StackMetaData metaData = stack.getMetaData().clone();
        if (direction.get() == LEFT) {
            reslicedCL = clij.createCLImage(new long[] {originalCL.getHeight(), originalCL.getDepth(), originalCL.getWidth()}, originalCL.getChannelDataType());
            clij.op().resliceLeft(originalCL, reslicedCL);
            double temp = metaData.getVoxelDimX();
            metaData.setVoxelDimX(metaData.getVoxelDimY());
            metaData.setVoxelDimY(metaData.getVoxelDimZ());
            metaData.setVoxelDimZ(temp);
        } else if (direction.get() == RIGHT) {
            reslicedCL = clij.createCLImage(new long[] {originalCL.getHeight(), originalCL.getDepth(), originalCL.getWidth()}, originalCL.getChannelDataType());
            clij.op().resliceRight(originalCL, reslicedCL);
            double temp = metaData.getVoxelDimX();
            metaData.setVoxelDimX(metaData.getVoxelDimY());
            metaData.setVoxelDimY(metaData.getVoxelDimZ());
            metaData.setVoxelDimZ(temp);
        } else if (direction.get() == TOP) {
            reslicedCL = clij.createCLImage(new long[] {originalCL.getWidth(), originalCL.getDepth(), originalCL.getHeight()}, originalCL.getChannelDataType());
            clij.op().resliceTop(originalCL, reslicedCL);
            double temp = metaData.getVoxelDimY();
            metaData.setVoxelDimY(metaData.getVoxelDimZ());
            metaData.setVoxelDimZ(temp);
        } else { // Bottom
            reslicedCL = clij.createCLImage(new long[] {originalCL.getWidth(), originalCL.getDepth(), originalCL.getHeight()}, originalCL.getChannelDataType());
            clij.op().resliceBottom(originalCL, reslicedCL);
            double temp = metaData.getVoxelDimY();
            metaData.setVoxelDimY(metaData.getVoxelDimZ());
            metaData.setVoxelDimZ(temp);
        }
        StackInterface resultStack = clij.convert(reslicedCL, StackInterface.class);
        resultStack.setMetaData(metaData);
        originalCL.close();
        reslicedCL.close();
        return resultStack;
    }

    @Override
    public ResliceInstruction copy() {
        ResliceInstruction copied = new ResliceInstruction(getDataWarehouse());
        copied.direction.set(direction.get());
        return copied;
    }

    @Override
    public String getDescription() {
        return "Reslices all image stacks in a given container.";
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                direction,
                recycleSavedContainers
        };
    }

    public Variable<Boolean> getRecycleSavedContainers() {
        return recycleSavedContainers;
    }

    public Variable<Integer> getDirection() {
        return direction;
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
}
