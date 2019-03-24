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
import coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;

/**
 * MaximumProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class DownsampledBackgroundSubtractedMaximumProjectionInstruction extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {

    private BoundedVariable<Double> backgroundDeterminationBlurSigmaXY = new BoundedVariable<Double>("Sigma XY", 5.0, 0.0, Double.MAX_VALUE, 0.001);
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after subtracting background", true);

    public DownsampledBackgroundSubtractedMaximumProjectionInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Downsampled background subtracted maximum projection", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();
        StackMetaData metaData = stack.getMetaData().clone();
        ClearCLImage inputImage = clij.convert(stack, ClearCLImage.class);
        ClearCLImage downsampledInputImage = clij.createCLImage(new long[]{inputImage.getWidth() / 2, inputImage.getHeight() / 2, inputImage.getDepth()}, ImageChannelDataType.Float);
        ClearCLImage blurredInputImage = clij.createCLImage(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getChannelDataType());
        ClearCLBuffer downsampledInputBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer blurredInputBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer backgroundSubtractedBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());

        ClearCLBuffer outputMaxProjected2DBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight()}, downsampledInputImage.getNativeType());
        ClearCLBuffer outputMaxProjected3DBuffer = clij.createCLBuffer(new long[]{downsampledInputImage.getWidth(), downsampledInputImage.getHeight(), 1}, inputImage.getNativeType());

        // downsampling
        clij.op().downsampleSliceBySliceHalfMedian(inputImage, downsampledInputImage);
        if (metaData != null) {
            metaData.setVoxelDimX(metaData.getVoxelDimX() * 2);
            metaData.setVoxelDimY(metaData.getVoxelDimY() * 2);
        }

        // background subtraction
        float sigma = backgroundDeterminationBlurSigmaXY.get().floatValue();
        clij.op().blurSliceBySlice(downsampledInputImage, blurredInputImage, (int)(sigma * 2), (int)(sigma * 2), sigma, sigma);

        clij.op().copy(downsampledInputImage, downsampledInputBuffer);
        clij.op().copy(blurredInputImage, blurredInputBuffer);

        clij.op().addImagesWeighted(downsampledInputBuffer, blurredInputBuffer, backgroundSubtractedBuffer, 1.0f, -1.0f);

        // maximum projections
        clij.op().maximumZProjection(backgroundSubtractedBuffer, outputMaxProjected2DBuffer);
        clij.op().copySlice(outputMaxProjected2DBuffer, outputMaxProjected3DBuffer, 0);


        // get back from GPU
        StackInterface resultStack = clij.convert(outputMaxProjected3DBuffer, StackInterface.class);
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

    @Override
    public String getDescription() {
        return "Subtract background from all image stacks in a container and generate maximum projections of it.";
    }


    public BoundedVariable<Double> getBackgroundDeterminationBlurSigmaXY() {
        return backgroundDeterminationBlurSigmaXY;
    }

    public Variable<Boolean> getRecycleSavedContainers() {
        return recycleSavedContainers;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                backgroundDeterminationBlurSigmaXY,
                recycleSavedContainers
        };
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
