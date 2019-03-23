package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.converters.AbstractCLIJConverter;
import net.haesleinhuepf.clij.converters.CLIJConverterPlugin;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
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
public class ClearCLImageToStackInterfaceConverter extends AbstractCLIJConverter<ClearCLImage, StackInterface> {

    @Override
    public StackInterface convert(ClearCLImage source) {


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
    public Class<ClearCLImage> getSourceType() {
        return ClearCLImage.class;
    }
}
