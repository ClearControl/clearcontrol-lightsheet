package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;

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
        super("Post-processing: Split image data from hybrid interleaved/optics-prefused acquisition", pLightSheetMicroscope);
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
            for (int l = 0; l < numberOfLightSheets + 1 ; l++) {
                splitImages[l] = clij.createCLImage(new long[]{
                        fullCLImage.getWidth(),
                        fullCLImage.getHeight(),
                        fullCLImage.getDepth() / numberOfLightSheets
                }, fullCLImage.getChannelDataType());
            }

            Kernels.splitStack(clij, fullCLImage, splitImages);

            // Fill the virtual sequential container
            for (int l = 0; l < numberOfLightSheets; l++) {
                StackInterface lVirtualSequentialStack = clij.converter(splitImages[l]).getStack();

                lVirtualSequentialStack.setMetaData(lStack.getMetaData().clone());
                lVirtualSequentialStack.getMetaData().removeEntry(MetaDataChannel.Channel);
                lVirtualSequentialStack.getMetaData().addEntry(MetaDataChannel.Channel, "sequential");
                lVirtualSequentialStack.getMetaData().removeEntry(MetaDataAcquisitionType.AcquisitionType);
                lVirtualSequentialStack.getMetaData().addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimelapseSequential);
                lVirtualSequentialStack.getMetaData().addEntry(MetaDataView.LightSheet, l);
                lSequentialContainer.put("C" + d + "L" + l, lVirtualSequentialStack);
            }

            // Fill the virtual opticsprefused container
            StackInterface lOpticsPrefusedStack = clij.converter(splitImages[splitImages.length - 1]).getStack();
            lOpticsPrefusedStack.setMetaData(lStack.getMetaData().clone());
            lOpticsPrefusedStack.getMetaData().removeEntry(MetaDataChannel.Channel);
            lOpticsPrefusedStack.getMetaData().addEntry(MetaDataChannel.Channel, "opticsprefused");
            lOpticsPrefusedStack.getMetaData().removeEntry(MetaDataAcquisitionType.AcquisitionType);
            lOpticsPrefusedStack.getMetaData().addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimeLapseOpticallyCameraFused);
            lOpticsPrefusedContainer.put("C" + d + "opticsprefused", lOpticsPrefusedStack);


        }

        getLightSheetMicroscope().getDataWarehouse().put("virtual_sequential_" + pTimePoint, lSequentialContainer);
        getLightSheetMicroscope().getDataWarehouse().put("virtual_opticsprefused_" + pTimePoint, lOpticsPrefusedContainer);


        //StackInterface lFusedStack = fuseStacks(lContainer, lInputImageKeys);
        //if (lFusedStack == null) {
        //    return false;
        //}
        //storeFusedContainer(lFusedStack);
        return true;
    }

    @Override
    public HybridInterleavedOpticsPrefusedSplitImageDataInstruction copy() {
        return new HybridInterleavedOpticsPrefusedSplitImageDataInstruction(getLightSheetMicroscope());
    }
}


