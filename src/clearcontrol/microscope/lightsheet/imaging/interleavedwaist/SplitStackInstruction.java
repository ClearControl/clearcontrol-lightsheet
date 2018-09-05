package clearcontrol.microscope.lightsheet.imaging.interleavedwaist;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.hybridinterleavedopticsprefused.HybridInterleavedOpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;

/**
 * The SplitStackInstruction receives the oldest StackInterfaceContainer from the Warehouse, reads one stack
 * per camera from it and splits it into a given number of sub stacks. These stacke could then be furthere
 * processed, e.g. by Tenengrad fusion
 *
 * Todo: find a way to keep the images in the GPU. Copying back and forth may be wasting time.
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class SplitStackInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private BoundedVariable<Integer> numberOfStacks = new BoundedVariable<Integer>("Number of stacks", 1, 1,Integer.MAX_VALUE);

    public SplitStackInstruction(LightSheetMicroscope microscope) {
        super("Post-processing: split stack", microscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
        final HybridInterleavedOpticsPrefusedImageDataContainer
                lContainer = lDataWarehouse.getOldestContainer(StackInterfaceContainer.class);

        SequentialImageDataContainer resultContainer = new SequentialImageDataContainer(getLightSheetMicroscope());

        for (int d = 0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++) {
            String inputImageKey = "C" + d;
            for (String key : lContainer.keySet()) {
                if (key.contains(inputImageKey)) {
                    inputImageKey = key;
                    break;
                }
            }

            StackInterface stack = lContainer.get(inputImageKey);
            if (stack == null) {
                warning("No image stack found for C" + d);
                continue;
            }

            ClearCLIJ clij = ClearCLIJ.getInstance();
            ClearCLImage fullCLImage = clij.converter(stack).getClearCLImage();

            int numberOfStacks = getNumberOfStacks().get();

            ClearCLImage[] splitImages = new ClearCLImage[numberOfStacks + 1];
            for (int l = 0; l < numberOfStacks; l++) {
                splitImages[l] = clij.createCLImage(new long[]{
                        fullCLImage.getWidth(),
                        fullCLImage.getHeight(),
                        fullCLImage.getDepth() / numberOfStacks
                }, fullCLImage.getChannelDataType());
            }

            Kernels.splitStack(clij, fullCLImage, splitImages);

            // Fill the result container with stacks
            for (int l = 0; l < numberOfStacks; l++) {
                StackInterface resultStack = clij.converter(splitImages[l]).getStack();

                resultStack.setMetaData(stack.getMetaData().clone());
                resultStack.getMetaData().removeEntry(MetaDataChannel.Channel);
                resultStack.getMetaData().addEntry(MetaDataChannel.Channel, "sequential");
                resultStack.getMetaData().removeEntry(MetaDataAcquisitionType.AcquisitionType);
                resultStack.getMetaData().addEntry(MetaDataAcquisitionType.AcquisitionType, AcquisitionType.TimelapseSequential);
                resultStack.getMetaData().addEntry(MetaDataView.LightSheet, l);
                resultContainer.put("C" + d + "_" + l, resultStack);
            }

            fullCLImage.close();

            for (int l = 0; l < numberOfStacks; l++) {
                splitImages[l].close();
            }
        }

        getLightSheetMicroscope().getDataWarehouse().put("split_" + pTimePoint, resultContainer);

        return true;
    }

    @Override
    public SplitStackInstruction copy() {
        SplitStackInstruction copied = new SplitStackInstruction(getLightSheetMicroscope());
        copied.numberOfStacks.set(numberOfStacks.get());
        return copied;
    }

    public BoundedVariable<Integer> getNumberOfStacks() {
        return numberOfStacks;
    }
}
