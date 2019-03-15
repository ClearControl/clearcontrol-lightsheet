package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import ij.ImagePlus;
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
public class ImagePlusToStackInterfaceConverter extends AbstractCLIJConverter<ImagePlus, StackInterface> {

    @Override
    public StackInterface convert(ImagePlus source) {
        // Todo: Replace double-conversion by efficient one-shot conversion
        ClearCLBuffer buffer = clij.convert(source, ClearCLBuffer.class);
        StackInterface stack = clij.convert(buffer, StackInterface.class);
        buffer.close();
        return stack;
    }

    @Override
    public Class<StackInterface> getTargetType() {
        return StackInterface.class;
    }

    @Override
    public Class<ImagePlus> getSourceType() {
        return ImagePlus.class;
    }
}
