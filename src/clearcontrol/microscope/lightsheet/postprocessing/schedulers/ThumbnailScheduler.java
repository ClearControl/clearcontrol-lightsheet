package clearcontrol.microscope.lightsheet.postprocessing.schedulers;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import ij.IJ;
import ij.ImagePlus;

import javax.xml.crypto.Data;
import java.io.File;

/**
 * The thumbnail generator create a thumbnail of the oldest image stack in the warehouse and saves it the current working directory
 *
 *
 * Author: @haesleinhuepf
 * April 2018
 */
public class ThumbnailScheduler<T extends StackInterfaceContainer> extends SchedulerBase implements LoggingFeature {

    private final Class<T> mClass;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ThumbnailScheduler(Class<T> pClass) {
        super("Post-processing: Thumbnail generator for " + pClass.getSimpleName());
        mClass = pClass;
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
        ClearCLImage lCLImage = clij.converter(lStack).getClearCLImage();
        ClearCLImage lCLMaximumProjectionImage = clij.createCLImage(new long[]{lCLImage.getWidth(), lCLImage.getHeight()}, lCLImage.getChannelDataType());

        Kernels.maxProjection(clij, lCLImage, lCLMaximumProjectionImage);

        ImagePlus lImpMaximumProjection = clij.converter(lCLMaximumProjectionImage).getImagePlus();
        lCLImage.close();
        lCLMaximumProjectionImage.close();

        new File(targetFolder + "/stacks/thumbnails/").mkdirs();


        IJ.run(lImpMaximumProjection, "8-bit", "");
        IJ.run(lImpMaximumProjection, "Enhance Contrast", "saturated=0.35");
        IJ.saveAsTiff(lImpMaximumProjection, targetFolder + "/stacks/thumbnails/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        return true;
    }
}
