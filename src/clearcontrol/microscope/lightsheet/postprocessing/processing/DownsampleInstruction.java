package clearcontrol.microscope.lightsheet.postprocessing.processing;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.StackMetaData;

import javax.management.ObjectName;
import java.lang.management.PlatformLoggingMXBean;
import java.util.List;

/**
 * DownsampleInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class DownsampleInstruction extends DataWarehouseInstructionBase implements PropertyIOableInstructionInterface {

    private BoundedVariable<Double> mDownSampleFactorX = new BoundedVariable<Double>("Factor X", 0.5, 0.0, 1.0, 0.0001);
    private BoundedVariable<Double> mDownSampleFactorY = new BoundedVariable<Double>("Factor Y", 0.5, 0.0, 1.0, 0.0001);
    private BoundedVariable<Double> mDownSampleFactorZ = new BoundedVariable<Double>("Factor Z", 0.5, 0.0, 1.0, 0.0001);

    public DownsampleInstruction(DataWarehouse pDataWarehouse) {
        super("Post-processing: Downsampling", pDataWarehouse);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        StackInterfaceContainer lContainer = getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);

        StackInterfaceContainer lResultContainer = new StackInterfaceContainer(pTimePoint) {
            @Override
            public boolean isDataComplete() {
                return true;
            }
        };

        for (String key : lContainer.keySet()) {
            StackInterface stack = lContainer.get(key);

            ClearCLIJ clij = ClearCLIJ.getInstance();

            ClearCLImage lCLImage = clij.converter(stack).getClearCLImage();
            ClearCLImage lClImageScaled = clij.createCLImage(new long[]{(long) (lCLImage.getWidth() * mDownSampleFactorX.get().floatValue()), (long) (lCLImage.getHeight() * mDownSampleFactorY.get().floatValue()), (long) (lCLImage.getDepth() * mDownSampleFactorZ.get().floatValue())}, lCLImage.getChannelDataType());
            Kernels.downsample(clij, lCLImage, lClImageScaled, mDownSampleFactorX.get().floatValue(), mDownSampleFactorY.get().floatValue(), mDownSampleFactorZ.get().floatValue());
            stack = clij.converter(lClImageScaled).getStack();
            lCLImage.close();
            lClImageScaled.close();

            lResultContainer.put(key, stack);

            // todo: save/convert meta data
        }

        getDataWarehouse().put("downsampled_" + pTimePoint, lResultContainer);

        return true;
    }

    @Override
    public DownsampleInstruction copy() {
        DownsampleInstruction copied = new DownsampleInstruction(getDataWarehouse());
        copied.mDownSampleFactorX.set(mDownSampleFactorX.get());
        copied.mDownSampleFactorY.set(mDownSampleFactorY.get());
        copied.mDownSampleFactorZ.set(mDownSampleFactorZ.get());
        return copied;
    }

    @Override
    public Variable[] getProperties() {
        return new Variable[]{
                mDownSampleFactorX,
                mDownSampleFactorY,
                mDownSampleFactorZ
        };
    }

    public BoundedVariable<Double> getDownSampleFactorX() {
        return mDownSampleFactorX;
    }

    public BoundedVariable<Double> getDownSampleFactorY() {
        return mDownSampleFactorY;
    }

    public BoundedVariable<Double> getDownSampleFactorZ() {
        return mDownSampleFactorZ;
    }
}
