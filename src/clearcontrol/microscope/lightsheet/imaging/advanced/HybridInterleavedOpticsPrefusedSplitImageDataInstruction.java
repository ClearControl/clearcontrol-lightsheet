package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedFusionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.fusion.FusionInstruction;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

/**
 * HybridInterleavedOpticsPrefusedSplitImageDataInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class HybridInterleavedOpticsPrefusedSplitImageDataInstruction extends LightSheetMicroscopeInstructionBase implements
        InstructionInterface,
        LoggingFeature
{
    /**
     * INstanciates a virtual device with a given name
     *
     */
    public HybridInterleavedOpticsPrefusedSplitImageDataInstruction(LightSheetMicroscope pLightSheetMicroscope)
    {
        super("Post-processing: Split image data from hybrid interleaved/optics-Prefused acquisition", pLightSheetMicroscope);
    }


    @Override
    public boolean initialize() {
        return true;
    }

    @Override public boolean enqueue(long pTimePoint)
    {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
        final HybridInterleavedOpticsPrefusedImageDataContainer
                lContainer = lDataWarehouse.getOldestContainer(HybridInterleavedOpticsPrefusedImageDataContainer.class);

        SequentialImageDataContainer lSequentialContainer = new SequentialImageDataContainer(getLightSheetMicroscope());
        OpticsPrefusedImageDataContainer lOpticsPrefusedContainer = new OpticsPrefusedImageDataContainer(getLightSheetMicroscope());

        for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++) {
            String lInputImageKey = "C" + d + "hybrid_interleaved_opticsprefused";

            StackInterface lStack = lContainer.get(lInputImageKey);

            ClearCLIJ clij = ClearCLIJ.getInstance();
            ClearCLImage fullCLImage = clij.converter(lStack).getClearCLImage();

            int numberOfLightSheets = getLightSheetMicroscope().getNumberOfLightSheets();

            ClearCLImage[] splitImages = new ClearCLImage[numberOfLightSheets + 1];
            for (int l = 0; l < numberOfLightSheets + 1 ; l++)
                    clij.createCLImage(new long[] {
               fullCLImage.getWidth(),
               fullCLImage.getHeight(),
               fullCLImage.getDepth() / numberOfLightSheets
            }, fullCLImage.getChannelDataType());

            Kernels.splitStack(clij, fullCLImage, splitImages);

            // Fill the virtual sequential container
            for (int l = 0; l < numberOfLightSheets; l++) {
                StackInterface lVirtualSequentialStack = clij.converter(splitImages[l]).getStack();
                lSequentialContainer.put("C" + d + "L" + l, lVirtualSequentialStack);
            }

            // Fill the virtual opticsprefused container
            StackInterface lOpticsPrefusedStack = clij.converter(splitImages[splitImages.length - 1]).getStack();
            lOpticsPrefusedContainer.put("C" + d + "opticsprefused", lOpticsPrefusedStack);


        }

        getLightSheetMicroscope().getDataWarehouse().put("virtual_sequential", lSequentialContainer);
        getLightSheetMicroscope().getDataWarehouse().put("virtual_opticsprefused", lOpticsPrefusedContainer);


        //StackInterface lFusedStack = fuseStacks(lContainer, lInputImageKeys);
        //if (lFusedStack == null) {
        //    return false;
        //}
        //storeFusedContainer(lFusedStack);
        return true;
    }

    @Override
    public InterleavedFusionInstruction copy() {
        return new InterleavedFusionInstruction(getLightSheetMicroscope());
    }
}


