package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLPeerPointer;
import net.haesleinhuepf.clij.clearcl.backend.BackendUtils;
import net.haesleinhuepf.clij.converters.AbstractCLIJConverter;
import net.haesleinhuepf.clij.converters.CLIJConverterPlugin;
import net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * StackInterfaceToClearCLBufferConverter
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 12 2018
 */
@Plugin(type = CLIJConverterPlugin.class)
public class StackInterfaceToClearCLBufferConverter extends AbstractCLIJConverter<StackInterface, ClearCLBuffer> {

    @Override
    public ClearCLBuffer convert(StackInterface source) {
        long[] dimensions = source.getDimensions();

        coremem.enums.NativeTypeEnum dataType = source.getDataType();

        NativeTypeEnum lImageChannelType = ConverterUtilities.nativeToNativeHSLH(dataType);

        ClearCLBuffer lClearClBuffer = clij.createCLBuffer(dimensions, lImageChannelType);

        final ByteBuffer byteBuffer = source.getContiguousMemory().getByteBuffer();

        lClearClBuffer.readFrom(byteBuffer, true);

        return lClearClBuffer;
    }

    @Override
    public Class<StackInterface> getSourceType() {
        return StackInterface.class;
    }

    @Override
    public Class<ClearCLBuffer> getTargetType() {
        return ClearCLBuffer.class;
    }
}
