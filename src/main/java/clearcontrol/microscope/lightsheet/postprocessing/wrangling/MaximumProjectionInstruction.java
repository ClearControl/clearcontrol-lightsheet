package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

/**
 * MaximumProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class MaximumProjectionInstruction extends ProcessAllStacksInCurrentContainerInstruction {
    public MaximumProjectionInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Maximum projection", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage inputCL = clij.converter(stack).getClearCLImage();
        ClearCLImage outputCL2D = clij.createCLImage(new long[]{inputCL.getWidth(), inputCL.getHeight()}, inputCL.getChannelDataType());
        ClearCLImage outputCL3D = clij.createCLImage(new long[]{inputCL.getWidth(), inputCL.getHeight(), 1}, inputCL.getChannelDataType());

        Kernels.maxProjection(clij, inputCL, outputCL2D);
        Kernels.copySlice(clij, outputCL2D, outputCL3D, 0);

        StackInterface resultStack = clij.converter(outputCL3D).getStack();
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
}
