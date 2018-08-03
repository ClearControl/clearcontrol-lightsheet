package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.TimeStampContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.projection.presentation.HalfStackProjectionPlugin;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.*;
import java.io.File;
import java.time.Duration;
import java.util.Iterator;

/**
 * HalfStackMaxProjectionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class HalfStackMaxProjectionInstruction<T extends StackInterfaceContainer> extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private final Class<T> mClass;
    private Variable<Boolean> mViewFrontVariable = new Variable<Boolean>("Front view", true);
    private Variable<Boolean> mPrintSequenceNameVariable = new Variable<Boolean>("Print sequence name", true);
    private Variable<Boolean> mPrintTimePointVariable = new Variable<Boolean>("Print time point", true);
    private Variable<String> mMustContainStringVariable = new Variable<String>("Stack lable must contain", "");
    private BoundedVariable<Double> mScalingFactorVariable = new BoundedVariable<Double>("Scaling factor", 0.5, 0.0001, Double.MAX_VALUE, 0.0001);

    private BoundedVariable<Integer> mFontSizeVariable = new BoundedVariable<Integer>("Font size", 14, 5, Integer.MAX_VALUE);

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public HalfStackMaxProjectionInstruction(Class<T> pClass, boolean pViewFront, LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Thumbnail (half stack max projection, " +(pViewFront?"front":"back") + ") generator for " + pClass.getSimpleName(), pLightSheetMicroscope);
        mClass = pClass;
        mViewFrontVariable.set(pViewFront);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        // Read oldest image from the warehouse
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        Iterator<String> iterator = lContainer.keySet().iterator();
        String key = "";
        StackInterface lStack = null;
        while(iterator.hasNext()) {
            key = iterator.next();
            if (key.toLowerCase().contains(mMustContainStringVariable.get().toLowerCase()) || getMustContainStringVariable().get().length() == 0) {
                lStack = lContainer.get(key);
                break;
            }
        }
        if (lStack == null) {
            warning("Couldn't find key '" + getMustContainStringVariable().get() + "' in containter " + lContainer + ". Skipping thumbnail creation.");
            return false;
        }

        String targetFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        long lTimePoint = lContainer.getTimepoint();
        int lDigits = 6;

        // Process the image
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ImagePlus lImagePlus = clij.converter(lStack).getImagePlus();

        if (lStack.getMetaData() != null) {
            lImagePlus.getCalibration().setUnit("micron");
            lImagePlus.getCalibration().pixelWidth = lStack.getMetaData().getVoxelDimX();
            lImagePlus.getCalibration().pixelHeight = lStack.getMetaData().getVoxelDimY();
            lImagePlus.getCalibration().pixelDepth = lStack.getMetaData().getVoxelDimZ();
        }

        HalfStackProjectionPlugin halfStackProjectionPlugin = new HalfStackProjectionPlugin();
        halfStackProjectionPlugin.setInputImage(lImagePlus);
        if (mViewFrontVariable.get()) {
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

        // downsample the image if scaling is set != 1.0
        if (Math.abs(mScalingFactorVariable.get() - 1.0) > 0.0001) {
            ClearCLImage lCLImage = clij.converter(lResultImagePlus).getClearCLImage();
            ClearCLImage lClImageScaled = clij.createCLImage(new long[]{(long)(lCLImage.getWidth() * mScalingFactorVariable.get().floatValue()), (long)(lCLImage.getHeight() * mScalingFactorVariable.get().floatValue())}, lCLImage.getChannelDataType());
            Kernels.downsample(clij, lCLImage, lClImageScaled, mScalingFactorVariable.get().floatValue(), mScalingFactorVariable.get().floatValue());
            lResultImagePlus = clij.converter(lClImageScaled).getImagePlus();
            lCLImage.close();
            lClImageScaled.close();

            if (lStack.getMetaData() != null) {
                lResultImagePlus.getCalibration().setUnit("micron");
                lResultImagePlus.getCalibration().pixelWidth = lStack.getMetaData().getVoxelDimX() / mScalingFactorVariable.get().floatValue();
                lResultImagePlus.getCalibration().pixelHeight = lStack.getMetaData().getVoxelDimY() / mScalingFactorVariable.get().floatValue();
            }

        }

        String folderName = "thumbnails_" + (mViewFrontVariable.get()?"front":"back");

        new File(targetFolder + "/stacks/" + folderName + "/").mkdirs();

        IJ.run(lResultImagePlus, "Enhance Contrast", "saturated=0.35");
        IJ.saveAsTiff(lResultImagePlus, targetFolder + "/stacks/" + folderName + "/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".tif");

        //
        if (lStack.getMetaData() != null) {
            IJ.run(lResultImagePlus, "16-bit", "");
            Font font = new Font("SanSerif", Font.PLAIN, mFontSizeVariable.get());
            ImageProcessor ip = lResultImagePlus.getProcessor();

            ip.setFont(font);
            ip.setColor(new Color(255, 255, 255));

            TimeStampContainer lStartTimeInNanoSecondsContainer = TimeStampContainer.getGlobalTimeSinceStart(getLightSheetMicroscope().getDataWarehouse(), pTimePoint, lStack);

            Duration duration = Duration.ofNanos(lStack.getMetaData().getTimeStampInNanoseconds() - lStartTimeInNanoSecondsContainer.getTimeStampInNanoSeconds());
            long s = duration.getSeconds();
            ip.drawString(String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60)) + (mPrintTimePointVariable.get()?" (tp " + pTimePoint + ")":"") + "\n" + (mPrintSequenceNameVariable.get()?key:""), 20, 30);

            lResultImagePlus.updateAndDraw();

            IJ.run(lResultImagePlus, "Scale Bar...", "width=100 height=3 font=" + mFontSizeVariable.get() + " color=White background=None location=[Lower Left]");

            new File(targetFolder + "/stacks/" + folderName + "_text/").mkdirs();
            IJ.saveAsTiff(lResultImagePlus, targetFolder + "/stacks/" + folderName + "_text/" + String.format("%0" + lDigits + "d", lTimePoint) + ".tif");
        } else {
            warning("Error: No meta data provided!");
        }

        return true;
    }

    public boolean isViewFront() {
        return mViewFrontVariable.get();
    }

    public BoundedVariable<Integer> getFontSizeVariable() {
        return mFontSizeVariable;
    }

    public Variable<String> getMustContainStringVariable() {
        return mMustContainStringVariable;
    }

    public Variable<Boolean> getViewFront() {
        return mViewFrontVariable;
    }

    public BoundedVariable<Double> getScalingVariable() {
        return mScalingFactorVariable;
    }

    public Variable<Boolean> getPrintSequenceNameVariable() {
        return mPrintSequenceNameVariable;
    }

    public Variable<Boolean> getPrintTimePointVariable() {
        return mPrintTimePointVariable;
    }

    @Override
    public HalfStackMaxProjectionInstruction copy() {
        HalfStackMaxProjectionInstruction copied = new HalfStackMaxProjectionInstruction(mClass, mViewFrontVariable.get(), getLightSheetMicroscope());
        copied.mMustContainStringVariable.set(mMustContainStringVariable.get());
        return copied;
    }
}
