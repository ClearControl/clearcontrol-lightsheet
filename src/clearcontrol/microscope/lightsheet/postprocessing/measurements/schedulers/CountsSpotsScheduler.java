package clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DCTS2DContainer;
import clearcontrol.microscope.lightsheet.postprocessing.containers.SpotCountContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import de.mpicbg.spimcat.spotdetection.GPUSpotDetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The CountsSpotsScheduler takes the image stack out of a the olderst of a given container type and counts spots in
 * the data set. The number of spots is then saved to a txt file in the current working directory.
 *
 * Author: @haesleinhuepf
 * 04 2018
 */
public class CountsSpotsScheduler<T extends StackInterfaceContainer> extends SchedulerBase implements LoggingFeature {
    private final Class<T> mClass;

    BoundedVariable<Double> mThreshold = new BoundedVariable<Double>("threshold", 200.0, 0.0, Double.MAX_VALUE, 0.1);
    BoundedVariable<Double> mXYDownsamplingFactor = new BoundedVariable<Double>("downsample XY by ", 0.25, 0.0, Double.MAX_VALUE, 0.001);
    BoundedVariable<Double> mZDownsamplingFactor = new BoundedVariable<Double>("downsample Z by ", 1.0, 0.0, Double.MAX_VALUE, 0.001);
    Variable<Boolean> mShowIntermediateResults = new Variable<Boolean>("show intermediate results", false);
    BoundedVariable<Double> mDoGSigmaMinued = new BoundedVariable<Double>("DoG sigma minuend", 3.0, 0.0, Double.MAX_VALUE, 0.1);
    BoundedVariable<Double> mDoGSigmaSubtrahend = new BoundedVariable<Double>("DoG sigma subtrahend", 6.0, 0.0, Double.MAX_VALUE, 0.1);
    BoundedVariable<Double> mBlurSigma = new BoundedVariable<Double>("Blur sigma", 6.0, 0.0, Double.MAX_VALUE, 0.1);
    BoundedVariable<Integer> mDoGRadius = new BoundedVariable<Integer>("DoG radius", 3, 0, Integer.MAX_VALUE);
    BoundedVariable<Integer> mBlurRadius = new BoundedVariable<Integer>("Blur radius", 12, 0, Integer.MAX_VALUE);
    BoundedVariable<Integer> mOptimaDetectionRadiud = new BoundedVariable<Integer>("Optima detection radius", 6, 0, Integer.MAX_VALUE);

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public CountsSpotsScheduler(Class<T> pClass) {
        super("Post-processing: Spot detection for " + pClass.getSimpleName());
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
        final LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
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
        ClearCLImage lSpotsImage = clij.createCLImage(new long[]{(long)(lCLImage.getWidth() * mXYDownsamplingFactor.get()), (long)(lCLImage.getHeight() * mXYDownsamplingFactor.get()), (long)(lCLImage.getDepth() * mZDownsamplingFactor.get())}, lCLImage.getChannelDataType());
        //ClearCLImage lCLMaximumProjectionImage = clij.createCLImage(new long[]{lCLImage.getWidth(), lCLImage.getHeight()}, lCLImage.getChannelDataType());

        //Kernels.maxProjection(clij, lCLImage, lCLMaximumProjectionImage);

        //ImagePlus lImpMaximumProjection = clij.converter(lCLMaximumProjectionImage).getImagePlus();
        //lCLImage.close();
        //lCLMaximumProjectionImage.close();


        ElapsedTime.measure("spot detection ", () -> {

            ClearCLImage input = lCLImage;
            ClearCLImage output = lSpotsImage;

            GPUSpotDetection lGPUSpotDetector = new GPUSpotDetection(clij, input, output, mThreshold.get().floatValue());
            lGPUSpotDetector.setBlurRadius(mBlurRadius.get());
            lGPUSpotDetector.setBlurSigma(mBlurSigma.get().floatValue());
            lGPUSpotDetector.setDogRadius(mDoGRadius.get());
            lGPUSpotDetector.setDogSigmaMinuend(mDoGSigmaMinued.get().floatValue());
            lGPUSpotDetector.setDogSigmaSubtrahend(mDoGSigmaSubtrahend.get().floatValue());
            lGPUSpotDetector.setOptimaDetectionRadius(mOptimaDetectionRadiud.get());
            lGPUSpotDetector.setShowIntermediateResults(mShowIntermediateResults.get());
            lGPUSpotDetector.setThreshold(mThreshold.get().floatValue());
            lGPUSpotDetector.exec();
            double lSpotCount = Kernels.sumPixels(clij, lSpotsImage);


            double lX = 0;
            double lY = 0;
            double lZ = 0;

            for (BasicStageInterface lStage : lLightSheetMicroscope.getDevices(BasicStageInterface.class)) {
                if (lStage.toString().contains("X")) {
                    lX = lStage.getPositionVariable().get();
                }
                if (lStage.toString().contains("Y")) {
                    lY = lStage.getPositionVariable().get();
                }
                if (lStage.toString().contains("Z")) {
                    lZ = lStage.getPositionVariable().get();
                }
            }


            SpotCountContainer lSpotCountContainer = new SpotCountContainer(pTimePoint, lX, lY, lZ, lSpotCount);

            lLightSheetMicroscope.getDataWarehouse().put("SPOTCOUNT_" + pTimePoint, lSpotCountContainer);

            String headline = "t\tX\tY\tZ\tspotcount\n";
            String resultTableLine = pTimePoint + "\t" + lX + "\t" + lY + "\t" + lZ + "\t" + lSpotCount + "\n" ;

            File lOutputFile = new File(targetFolder + "/spotcount.tsv");

            try {
                boolean existedBefore = (lOutputFile.exists());


                BufferedWriter writer = new BufferedWriter(new FileWriter(lOutputFile, true));
                if (!existedBefore) {
                    writer.write(headline);
                }
                writer.write (resultTableLine);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        lCLImage.close();
        lSpotsImage.close();

        //IJ.saveAs(lImpMaximumProjection, "JPEG", targetFolder + "/stacks/thumbnails/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".jpg");
        return true;
    }

    public BoundedVariable<Double> getBlurSigma() {
        return mBlurSigma;
    }

    public BoundedVariable<Double> getThreshold() {
        return mThreshold;
    }

    public BoundedVariable<Double> getXYDownsamplingFactor() {
        return mXYDownsamplingFactor;
    }

    public BoundedVariable<Double> getDoGSigmaMinued() {
        return mDoGSigmaMinued;
    }

    public BoundedVariable<Double> getZDownsamplingFactor() {
        return mZDownsamplingFactor;
    }

    public BoundedVariable<Double> getDoGSigmaSubtrahend() {
        return mDoGSigmaSubtrahend;
    }

    public BoundedVariable<Integer> getBlurRadius() {
        return mBlurRadius;
    }

    public BoundedVariable<Integer> getDoGRadius() {
        return mDoGRadius;
    }

    public Variable<Boolean> getShowIntermediateResults() {
        return mShowIntermediateResults;
    }
}
