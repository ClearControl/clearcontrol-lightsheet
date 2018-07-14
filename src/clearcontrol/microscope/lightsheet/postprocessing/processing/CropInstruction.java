package clearcontrol.microscope.lightsheet.postprocessing.processing;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * CropInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 06 2018
 */
public class CropInstruction extends DataWarehouseInstructionBase {

    private BoundedVariable<Integer> mCropXVariable = new BoundedVariable<Integer>("Crop X", 0, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mCropYVariable = new BoundedVariable<Integer>("Crop Y", 0, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mCropZVariable = new BoundedVariable<Integer>("Crop Z", 0, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mCropWidthVariable = new BoundedVariable<Integer>("Crop width", 256, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mCropHeightVariable = new BoundedVariable<Integer>("Crop height", 256, 0, Integer.MAX_VALUE);
    private BoundedVariable<Integer> mCropDepthVariable = new BoundedVariable<Integer>("Crop depth", 1, 0, Integer.MAX_VALUE);

    public CropInstruction(DataWarehouse pDataWarehouse, int pCropX, int pCropY, int pCropWidth, int pCropHeight) {
        super("Post-processing: Crop", pDataWarehouse);
        mCropXVariable.set(pCropX);
        mCropYVariable.set(pCropY);
        mCropWidthVariable.set(pCropWidth);
        mCropHeightVariable.set(pCropHeight);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        StackInterfaceContainer lSourceContainer = getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);
        StackInterfaceContainer lTargetContainer = new StackInterfaceContainer(pTimePoint) {
            @Override
            public boolean isDataComplete() {
                return true;
            }
        };

        for (String key : lSourceContainer.keySet()) {
            StackInterface lStack = lSourceContainer.get(key);

            ClearCLIJ clij = ClearCLIJ.getInstance();
            ClearCLImage src = clij.converter(lStack).getClearCLImage();
            ClearCLImage dst = clij.createCLImage(new long[]{(long) mCropWidthVariable.get(), (long) mCropHeightVariable.get(), (long)mCropDepthVariable.get()},
                    src.getChannelDataType());

            Kernels.crop(clij, src, dst, mCropXVariable.get(), mCropYVariable.get(), mCropZVariable.get());
            //clij.show(dst, "Processing Quality On");

            StackInterface lCroppedStack = clij.converter(dst).getOffHeapPlanarStack();
            lCroppedStack.copyMetaDataFrom(lStack);
            dst.close();
            src.close();

            lTargetContainer.put(key, lCroppedStack);
        }
        getDataWarehouse().put("crop_" + pTimePoint, lTargetContainer);

        return true;
    }

    @Override
    public InstructionInterface copy() {
        return new CropInstruction(getDataWarehouse(), mCropXVariable.get(), mCropXVariable.get(), mCropWidthVariable.get(), mCropHeightVariable.get());
    }

    public BoundedVariable<Integer> getCropXVariable() {
        return mCropXVariable;
    }

    public BoundedVariable<Integer> getCropYVariable() {
        return mCropYVariable;
    }

    public BoundedVariable<Integer> getCropZVariable() {
        return mCropZVariable;
    }

    public BoundedVariable<Integer> getCropWidthVariable() {
        return mCropWidthVariable;
    }

    public BoundedVariable<Integer> getCropHeightVariable() {
        return mCropHeightVariable;
    }

    public BoundedVariable<Integer> getCropDepthVariable() {
        return mCropDepthVariable;
    }
}
