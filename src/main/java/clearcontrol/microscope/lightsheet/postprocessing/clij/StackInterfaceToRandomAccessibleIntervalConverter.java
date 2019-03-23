package clearcontrol.microscope.lightsheet.postprocessing.clij;

import clearcontrol.stack.StackInterface;
import ij.ImagePlus;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.converters.AbstractCLIJConverter;
import net.haesleinhuepf.clij.converters.CLIJConverterPlugin;
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
public class StackInterfaceToRandomAccessibleIntervalConverter extends AbstractCLIJConverter<StackInterface, RandomAccessibleInterval> {

    @Override
    public RandomAccessibleInterval convert(StackInterface source) {
        ClearCLBuffer buffer = clij.convert(source, ClearCLBuffer.class);
        return clij.convert(buffer, RandomAccessibleInterval.class);
    }

    @Override
    public Class<StackInterface> getSourceType() {
        return StackInterface.class;
    }

    @Override
    public Class<RandomAccessibleInterval> getTargetType() {
        return RandomAccessibleInterval.class;
    }
}
