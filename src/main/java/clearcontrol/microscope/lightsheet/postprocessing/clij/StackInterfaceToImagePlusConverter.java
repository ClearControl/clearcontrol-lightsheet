package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.StackInterface;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
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
public class StackInterfaceToImagePlusConverter extends AbstractCLIJConverter<StackInterface, ImagePlus> {

    @Override
    public ImagePlus convert(StackInterface source) {
        ClearCLBuffer buffer = clij.convert(source, ClearCLBuffer.class);
        return clij.convert(buffer, ImagePlus.class);
    }

    @Override
    public Class<StackInterface> getSourceType() {
        return StackInterface.class;
    }

    @Override
    public Class<ImagePlus> getTargetType() {
        return ImagePlus.class;
    }
}
