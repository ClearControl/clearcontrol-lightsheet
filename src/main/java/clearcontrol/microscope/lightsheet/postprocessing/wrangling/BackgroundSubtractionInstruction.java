package clearcontrol.microscope.lightsheet.postprocessing.wrangling;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.postprocessing.ProcessAllStacksInCurrentContainerInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.stack.StackInterface;
import ij.IJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * BackgroundSubtractionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class BackgroundSubtractionInstruction  extends ProcessAllStacksInCurrentContainerInstruction implements PropertyIOableInstructionInterface, AutoRecyclerInstructionInterface {

    private BoundedVariable<Double> backgroundDeterminationBlurSigmaXY = new BoundedVariable<Double>("Sigma XY", 5.0, 0.0, Double.MAX_VALUE, 0.001);
    protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after subtracting background", true);

    public BackgroundSubtractionInstruction(DataWarehouse dataWarehouse) {
        super("Post-processing: Subtract background", dataWarehouse);
    }

    @Override
    protected StackInterface processStack(StackInterface stack) {
        CLIJ clij = CLIJ.getInstance();

        /*
        System.out.println("A");
        ImagePlus imp = clij.converter(stack).getImagePlus();

        IJ.run(imp, "Subtract Background...", "rolling=" + backgroundDeterminationBlurSigmaXY.get() + " sliding stack");

        StackInterface resultStack = clij.converter(imp).getStack();
        resultStack.setMetaData(stack.getMetaData().clone());
        */
        // OpenCL is broken :-(

        ClearCLBuffer inputCLI = clij.convert(stack, ClearCLBuffer.class);
        ClearCLBuffer inputCLB = clij.convert(stack, ClearCLBuffer.class);
        ClearCLBuffer backgroundCLI = clij.create(inputCLI);
        //ClearCLImage backgroundCL = clij.createCLImage(inputCL.getDimensions(), ImageChannelDataType.Float);
        //ClearCLImage backgroundCL2 = clij.createCLImage(inputCL.getDimensions(), ImageChannelDataType.Float);
        ClearCLBuffer outputCLB = clij.createCLBuffer(inputCLB);

        System.out.println("B");

        float sigma = backgroundDeterminationBlurSigmaXY.get().floatValue();
        clij.op().blurSliceBySlice(inputCLI, backgroundCLI, (int)(sigma * 2), (int)(sigma * 2), sigma, sigma);

        ClearCLBuffer backgroundCLB = clij.convert(backgroundCLI, ClearCLBuffer.class);
        System.out.println("C");
        //Kernels.multiplyScalar(clij, backgroundCL, backgroundCL2, -1.0f);
        //System.out.println("Ca");
        //Kernels.addPixelwise(clij, inputCL, backgroundCL2, outputCL);

        clij.op().addImagesWeighted(inputCLB, backgroundCLB, outputCLB, 1.0f, -1.0f);

        System.out.println("D");
        StackInterface resultStack = clij.convert(outputCLB, StackInterface.class);
        resultStack.setMetaData(stack.getMetaData().clone());

        System.out.println("E");
        inputCLI.close();
        backgroundCLI.close();
        inputCLB.close();
        backgroundCLB.close();
        outputCLB.close();
        System.out.println("F");

        return resultStack;
    }

    @Override
    public BackgroundSubtractionInstruction copy() {
        BackgroundSubtractionInstruction copied = new BackgroundSubtractionInstruction(getDataWarehouse());
        copied.backgroundDeterminationBlurSigmaXY.set(backgroundDeterminationBlurSigmaXY.get());
        return copied;
    }

    @Override
    public String getDescription() {
        return "Determine background using Gaussian blur and subtract it from the input image stacks.";
    }

    public BoundedVariable<Double> getBackgroundDeterminationBlurSigmaXY() {
        return backgroundDeterminationBlurSigmaXY;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[] {
                backgroundDeterminationBlurSigmaXY,
                recycleSavedContainers
        };
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
}
