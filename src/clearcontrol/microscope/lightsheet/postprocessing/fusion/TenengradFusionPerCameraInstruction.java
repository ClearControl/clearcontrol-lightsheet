package clearcontrol.microscope.lightsheet.postprocessing.fusion;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcl.imagej.utilities.ImageTypeConverter;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.util.ArrayList;

/**
 * The TenengradFusionPerCameraInstruction fuses all images in a container coming from the same camera.
 * It can fuse images from several cameras if it find them in the container.
 *
 * Assumptions:
 * - The cameras deliver images of type UnsignedShort
 * - All Images in the container have the same size
 *
 * Thus, it can be used to fuse images resulting from sequential imaging.
 *
 * Author: @haesleinhuepf
 * August 2018
 */
public class TenengradFusionPerCameraInstruction extends LightSheetMicroscopeInstructionBase implements PropertyIOableInstructionInterface {

    private BoundedVariable<Double> blurWeightSigmaX = new BoundedVariable<Double>("Blur weights X sigma in pixels", 15.0, 0.0, Double.MAX_VALUE, 0.01);
    private BoundedVariable<Double> blurWeightSigmaY = new BoundedVariable<Double>("Blur weights Y sigma in pixels", 15.0, 0.0, Double.MAX_VALUE, 0.01);
    private BoundedVariable<Double> blurWeightSigmaZ = new BoundedVariable<Double>("Blur weights Z sigma in pixels", 5.0, 0.0, Double.MAX_VALUE, 0.01);

    private BoundedVariable<Double> weightExponent = new BoundedVariable<Double>("Weight exponent", 1.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.001);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public TenengradFusionPerCameraInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Tenengrad fusion per camera", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        ClearCLIJ clij = ClearCLIJ.getInstance();

        StackInterfaceContainer containerIn = getLightSheetMicroscope().getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);

        float[] weightBlurSigmas = {
                blurWeightSigmaX.get().floatValue(),
                blurWeightSigmaY.get().floatValue(),
                blurWeightSigmaZ.get().floatValue()
        };

        TenengradFusedStackInterfaceContainer containerOut = new TenengradFusedStackInterfaceContainer(pTimePoint);

        for (int c = 0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
        {
            ArrayList<ClearCLImage> images = new ArrayList<ClearCLImage>();

            for (String key : containerIn.keySet()) {
                if (key.toLowerCase().startsWith("c" + c)) {
                    // Get UnsignedShort stack from container
                    StackInterface stack = containerIn.get(key);
                    RandomAccessibleInterval<UnsignedShortType> rai = clij.converter(stack).getRandomAccessibleInterval();

                    // Convert it to a float CLImage
                    ClearCLImage clImage = clij.createCLImage(stack.getDimensions(), ImageChannelDataType.Float);
                    ImageTypeConverter.copyRandomAccessibleIntervalToClearCLImage(rai, clImage);

                    // store the float image in the list;
                    images.add(clImage);
                }
            }

            if (images.size() == 0) {
                // no images from a given camera
                continue;
            }

            ClearCLImage fusionResult = clij.createCLImage(images.get(0).getDimensions(), ImageChannelDataType.Float);
            ClearCLImage fusionResultAsUnsignedShort = clij.createCLImage(fusionResult.getDimensions(), ImageChannelDataType.UnsignedInt16);

            ClearCLImage[] imagesIn = new ClearCLImage[images.size()];
            images.toArray(imagesIn);

            // fusion
            Kernels.tenengradFusion(clij, fusionResult, weightBlurSigmas, weightExponent.get().floatValue(), imagesIn);

            // Result conversion / storage
            Kernels.copy(clij, fusionResult, fusionResultAsUnsignedShort);
            StackInterface result = clij.converter(fusionResultAsUnsignedShort).getStack();

            containerOut.put("C" + c + "_tenengrad_fused", result);

            // cleanup
            for (ClearCLImage image : images) {
                image.close();
            }
            fusionResult.close();
            fusionResultAsUnsignedShort.close();
        }

        getLightSheetMicroscope().getDataWarehouse().put("tenengrad_fused_" + pTimePoint, containerOut);


        return true;
    }

    @Override
    public TenengradFusionPerCameraInstruction copy() {
        return new TenengradFusionPerCameraInstruction(getLightSheetMicroscope());
    }

    public BoundedVariable<Double> getBlurWeightSigmaX() {
        return blurWeightSigmaX;
    }

    public BoundedVariable<Double> getBlurWeightSigmaY() {
        return blurWeightSigmaY;
    }

    public BoundedVariable<Double> getBlurWeightSigmaZ() {
        return blurWeightSigmaZ;
    }

    public BoundedVariable<Double> getWeightExponent() {
        return weightExponent;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[]{
                getBlurWeightSigmaX(),
                getBlurWeightSigmaY(),
                getBlurWeightSigmaZ()
        };
    }
}
