package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import clearcl.ClearCL;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.utilities.ImageTypeConverter;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;

/**
 * ShowInBigDataViewerInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class ShowInBigDataViewerInstruction<T extends StackInterfaceContainer, P extends RealType<P>> extends DataWarehouseInstructionBase {

    private Class<T> mTargetContainerClass;
    private static Bdv bdv = null;
    private RandomAccessibleInterval<P> rai = null;

    public ShowInBigDataViewerInstruction(Class<T> pTargetContainerClass, DataWarehouse pDataWarehouse) {
        super("Visualisation: View " + pTargetContainerClass.getSimpleName() + " in BigDataViewer", pDataWarehouse);
        mTargetContainerClass = pTargetContainerClass;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {

        T lContainer = getDataWarehouse().getOldestContainer(mTargetContainerClass);
        StackInterface stack = lContainer.get(lContainer.keySet().iterator().next());

        ClearCLIJ clij = ClearCLIJ.getInstance();

        RandomAccessibleInterval<P> newRai = clij.converter(stack).getRandomAccessibleInterval();

        if (rai == null) {
            rai = newRai;
        } else {
            LoopBuilder.setImages(rai, newRai).forEachPixel(
                    (result, back) -> {
                        result.set(back);
                    }
            );
        }

        if ( bdv == null ) {
            bdv = BdvFunctions.show(rai, "BigDataViewer");
        }

        return true;
    }

    @Override
    public InstructionInterface copy() {
        return new ShowInBigDataViewerInstruction<T, P>(mTargetContainerClass, getDataWarehouse());
    }
}
