package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;
import coremem.enums.NativeTypeEnum;

/**
 * MaximumProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class DownsampledBackgroundSubtractedMaximumProjectionInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface {

    private BoundedVariable<Double> backgroundDeterminationBlurSigmaXY = new BoundedVariable<Double>("Sigma XY", 5.0, 0.0, Double.MAX_VALUE, 0.001);

    public DownsampledBackgroundSubtractedMaximumProjectionInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Downsampled background subtracted maximum projection", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        ClearCLIJ clij = ClearCLIJ.getInstance();
        StackMetaData metaData = stack.getMetaData().clone();
        ClearCLImage inputImage = clij.converter(stack).getClearCLImage();
        ClearCLImage downsampledInputImage = clij.createCLImage(new long[]{inputImage.getWidth() / 2, inputImage.getHeight() / 2, inputImage.getDepth()}, ImageChannelDataType.Float);
        ClearCLImage blurredInputImage = clij.createCLImage(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getChannelDataType());
        ClearCLBuffer downsampledInputBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer blurredInputBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer backgroundSubtractedBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());

        ClearCLBuffer outputMaxProjected2DBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer outputMaxProjected3DBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight(), 1}, inputImage.getNativeType());

        // downsampling
        Kernels.downsampleSliceBySliceHalfMedian(clij, inputImage, downsampledInputImage);
        if (metaData != null) {
            metaData.setVoxelDimX(metaData.getVoxelDimX() * 2);
            metaData.setVoxelDimY(metaData.getVoxelDimY() * 2);
        }

        // background subtraction
        float sigma = backgroundDeterminationBlurSigmaXY.get().floatValue();
        Kernels.blurSliceBySlice(clij, downsampledInputImage, blurredInputImage, (int)(sigma * 2), (int)(sigma * 2), sigma, sigma);

        Kernels.copy(clij, downsampledInputImage, downsampledInputBuffer);
        Kernels.copy(clij, blurredInputImage, blurredInputBuffer);

        Kernels.addWeightedPixelwise(clij, downsampledInputBuffer, blurredInputBuffer, backgroundSubtractedBuffer, 1.0f, -1.0f);

        // maximum projections
        Kernels.maxProjection(clij, backgroundSubtractedBuffer, outputMaxProjected2DBuffer);
        Kernels.copySlice(clij, outputMaxProjected2DBuffer, outputMaxProjected3DBuffer, 0);


        // get back from GPU
        StackInterface resultStack = clij.converter(outputMaxProjected3DBuffer).getStack();
        resultStack.setMetaData(stack.getMetaData().clone());

        inputImage.close();
        downsampledInputImage.close();
        blurredInputImage.close();
        downsampledInputBuffer.close();
        blurredInputBuffer.close();
        backgroundSubtractedBuffer.close();
        outputMaxProjected2DBuffer.close();
        outputMaxProjected3DBuffer.close();

        return resultStack;
    }

    @Override
    public DownsampledBackgroundSubtractedMaximumProjectionInstruction copy() {
        DownsampledBackgroundSubtractedMaximumProjectionInstruction copied = new DownsampledBackgroundSubtractedMaximumProjectionInstruction(getDataWarehouse());
        copied.backgroundDeterminationBlurSigmaXY.set(backgroundDeterminationBlurSigmaXY.get());
        return copied;
    }


    public BoundedVariable<Double> getBackgroundDeterminationBlurSigmaXY() {
        return backgroundDeterminationBlurSigmaXY;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                backgroundDeterminationBlurSigmaXY
        };
    }
}
