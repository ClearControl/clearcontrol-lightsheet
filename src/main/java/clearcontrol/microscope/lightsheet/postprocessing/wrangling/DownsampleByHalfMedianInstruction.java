package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * DownsampleByHalfMedianInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class DownsampleByHalfMedianInstruction  extends ProcessAllStacksInCurrentContainerInstruction {
    public DownsampleByHalfMedianInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Downsample XY by half (median)", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage inputCL = clij.converter(stack).getClearCLImage();
        ClearCLImage outputCL3D = clij.createCLImage(new long[]{inputCL.getWidth() / 2, inputCL.getHeight() / 2, inputCL.getDepth()}, inputCL.getChannelDataType());

        Kernels.downsampleSliceBySliceHalfMedian(clij, inputCL, outputCL3D);

        StackInterface resultStack = clij.converter(outputCL3D).getStack();
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
}