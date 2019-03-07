package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import io.scif.img.converters.RandomAccessConverter;
import net.haesleinhuepf.clij.clearcl.ClearCL;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.converters.AbstractCLIJConverter;
import net.haesleinhuepf.clij.converters.CLIJConverterPlugin;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.macro.modules.Clear;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.plugin.Plugin;

/**
 * StackInterfaceToClearCLBufferConverter
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = CLIJConverterPlugin.class)
public class RandomAccessibleIntervalToStackInterfaceConverter extends AbstractCLIJConverter<RandomAccessibleInterval, StackInterface> {

    @Override
    public StackInterface convert(RandomAccessibleInterval rai) {

        ClearCLBuffer source = clij.convert(rai, ClearCLBuffer.class);

        long[] dimensions = source.getDimensions();

        NativeTypeEnum dataType = source.getNativeType();

        coremem.enums.NativeTypeEnum type = ConverterUtilities.nativeHSLHToNative(dataType);


        OffHeapPlanarStack stack = new OffHeapPlanarStack(true, 1, type, 1, dimensions);

        source.writeTo(stack.getContiguousMemory().getByteBuffer(), true);

        return stack;
    }

    @Override
    public Class<StackInterface> getTargetType() {
        return StackInterface.class;
    }

    @Override
    public Class<RandomAccessibleInterval> getSourceType() {
        return RandomAccessibleInterval.class;
    }
}
