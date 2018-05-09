package clearcontrol.microscope.lightsheet.postprocessing.schedulers;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.projection.presentation.HalfStackProjectionPlugin;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;

import java.io.File;

/**
 * HalfStackMaxProjectionScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class HalfStackMaxProjectionScheduler <T extends StackInterfaceContainer> extends SchedulerBase implements LoggingFeature {

    private final Class<T> mClass;
    private final boolean mViewFront;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public HalfStackMaxProjectionScheduler(Class<T> pClass, boolean pViewFront) {
        super("Post-processing: Thumbnail (half stack max projection, " +(pViewFront?"front":"back") + ") generator for " + pClass.getSimpleName());
        mClass = pClass;
        mViewFront = pViewFront;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        // Read oldest image from the warehouse
        LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
        DataWarehouse lDataWarehouse = lLightSheetMicroscope.getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        String key = lContainer.keySet().iterator().next();
        StackInterface lStack = lContainer.get(key);

        String targetFolder = lLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        long lTimePoint = lContainer.getTimepoint();
        int lDigits = 6;

        // Process the image
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ImagePlus lImagePlus = clij.converter(lStack).getImagePlus();


        HalfStackProjectionPlugin halfStackProjectionPlugin = new HalfStackProjectionPlugin();
        halfStackProjectionPlugin.setInputImage(lImagePlus);
        if (mViewFront) {
            halfStackProjectionPlugin.minSlice = 0;
            halfStackProjectionPlugin.maxSlice = lImagePlus.getNSlices() / 2;
        } else {
            halfStackProjectionPlugin.minSlice = lImagePlus.getNSlices() / 2;
            halfStackProjectionPlugin.maxSlice = lImagePlus.getNSlices() - 1;
        }
        halfStackProjectionPlugin.setSilent(true);
        halfStackProjectionPlugin.setShowResult(false);
        halfStackProjectionPlugin.run();
        ImagePlus lResultImagePlus = halfStackProjectionPlugin.getOutputImage();

        String folderName = "thumbnails_" + (mViewFront?"front":"back");

        new File(targetFolder + "/stacks/" + folderName + "/").mkdirs();

        IJ.run(lResultImagePlus, "Enhance Contrast", "saturated=0.35");
        IJ.saveAsTiff(lResultImagePlus, targetFolder + "/stacks/" + folderName + "/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        return true;
    }
}
