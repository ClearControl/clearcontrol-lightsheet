package clearcontrol.microscope.lightsheet.imaging.sequential;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.stack.StackInterface;

/**
 * SequentialSingleCameraFusionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class SequentialSingleCameraFusionInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature, PropertyIOableInstructionInterface {

    private BoundedVariable<Integer> cameraIndex = new BoundedVariable<Integer>("Camera index", 0, 0, Integer.MAX_VALUE);

    private BoundedVariable<Double> blurSigmaX = new BoundedVariable<Double>("Blur sigma X in pixels", 15.0, 0.0, Double.MAX_VALUE, 0.01);
    private BoundedVariable<Double> blurSigmaY = new BoundedVariable<Double>("Blur sigma Y in pixels", 15.0, 0.0, Double.MAX_VALUE, 0.01);
    private BoundedVariable<Double> blurSigmaZ = new BoundedVariable<Double>("Blur sigma Z in pixels", 5.0, 0.0, Double.MAX_VALUE, 0.01);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public SequentialSingleCameraFusionInstruction(int cameraIndex, LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Sequential single camera fusion C" + cameraIndex, pLightSheetMicroscope);
        this.cameraIndex.set(cameraIndex);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        ClearCLIJ clij = ClearCLIJ.getInstance();

        SequentialImageDataContainer container = getLightSheetMicroscope().getDataWarehouse().getOldestContainer(SequentialImageDataContainer.class);

        int numberOfLightSheets = getLightSheetMicroscope().getNumberOfLightSheets();

        StackInterface stack = null;
        ClearCLImage[] inputImages = new ClearCLImage[numberOfLightSheets];
        for (int l = 0; l < numberOfLightSheets; l++) {
            stack = container.get("C" + cameraIndex.get() + "L" + l);
            inputImages[l] = clij.converter(stack).getClearCLImage();
        }

        float[] sigmas = new float[] {
                blurSigmaX.get().floatValue(),
                blurSigmaY.get().floatValue(),
                blurSigmaZ.get().floatValue()
        };

        ClearCLImage outputImage = clij.createCLImage(inputImages[0]);

        Kernels.tenengradFusion(clij, outputImage, sigmas, inputImages);

        StackInterface result = clij.converter(outputImage).getStack();
        if (stack != null) {
            result.setMetaData(stack.getMetaData().clone());
            result.getMetaData().removeEntry(MetaDataView.LightSheet);
        }

        FusedImageDataContainer resultContainer = new FusedImageDataContainer(pTimePoint);
        resultContainer.put("fused", result);

        getLightSheetMicroscope().getDataWarehouse().put("fused_sequential_single_camera_" + pTimePoint, resultContainer);

        return false;
    }

    @Override
    public SequentialSingleCameraFusionInstruction copy() {
        SequentialSingleCameraFusionInstruction copied = new SequentialSingleCameraFusionInstruction(cameraIndex.get(), getLightSheetMicroscope());
        copied.blurSigmaX.set(blurSigmaX.get());
        copied.blurSigmaY.set(blurSigmaY.get());
        copied.blurSigmaZ.set(blurSigmaZ.get());
        return copied;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[0];
    }

    public BoundedVariable<Integer> getCameraIndex() {
        return cameraIndex;
    }
}
