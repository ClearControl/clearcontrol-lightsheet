package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.haesleinhuepf.clij.converters.AbstractCLIJConverter;
import net.haesleinhuepf.clij.converters.CLIJConverterPlugin;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import org.scijava.plugin.Plugin;

import java.nio.ByteBuffer;

/**
 * StackInterfaceToClearCLBufferConverter
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = CLIJConverterPlugin.class)
public class StackInterfaceToClearCLImageConverter extends AbstractCLIJConverter<StackInterface, ClearCLImage> {

    @Override
    public ClearCLImage convert(StackInterface source) {
        long[] dimensions = source.getDimensions();

        coremem.enums.NativeTypeEnum dataType = source.getDataType();

        ImageChannelDataType type = ConverterUtilities.nativeTypeToImageChannelDataType(dataType);

        ClearCLImage lClearClBuffer = clij.createCLImage(dimensions, type);

        final ByteBuffer byteBuffer = source.getContiguousMemory().getByteBuffer();

        lClearClBuffer.readFrom(byteBuffer, true);


        return lClearClBuffer;
    }

    @Override
    public Class<StackInterface> getSourceType() {
        return StackInterface.class;
    }

    @Override
    public Class<ClearCLImage> getTargetType() {
        return ClearCLImage.class;
    }
}
