package clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcl.imagej.utilities.ImageTypeConverter;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.FocusMeasuresContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.utils.DoubleArrayImageImgConverter;
import ij.measure.ResultsTable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * The MeasureImageQualityInstruction allows measuring the image quality metrics published in
 *
 * Loic A. Royer, William C. Lemon, Raghav K. Chhetri, Yinan Wan, Michael Coleman, Eugene Myers
 * and Philipp J. Keller.: Real-Time Adaptive Light-Sheet Microscopy Recovers High Resolution
 * in Large Living Organisms. Nat Biotechnol. 2016
 *
 * It is a wrapper around the Fiji plugin
 * https://github.com/SpimCat/imagequalitymetrics-microscopeautopilot
 *
 * Author: haesleinhuepf
 * July 2018
 */
public class MeasureImageQualityInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature, PropertyIOableInstructionInterface {

    private Variable<String> mKeyMustContainString = new Variable<String>("Image key", "");
    private HashMap<FocusMeasures.FocusMeasure, Variable<Boolean>> mSelectedFeaturesMap;

    private static final FocusMeasures.FocusMeasure[] cDefaultFeatures = {FocusMeasures.FocusMeasure.SpectralNormDCTEntropyShannon, FocusMeasures.FocusMeasure.StatisticMean, FocusMeasures.FocusMeasure.StatisticMax, FocusMeasures.FocusMeasure.StatisticVariance};

    private ResultsTable resultsTable;

    private HashMap<FocusMeasures.FocusMeasure, Double> mFeatureSums;

    public MeasureImageQualityInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Measure image quality", pLightSheetMicroscope);

        mSelectedFeaturesMap = new HashMap<>();

        List<FocusMeasures.FocusMeasure> defaultSelection = Arrays.asList(cDefaultFeatures);

        FocusMeasures.FocusMeasure[] focusMeasuresArray = FocusMeasures.getFocusMeasuresArray();
        for (int i = 0; i < focusMeasuresArray.length; i++) {
            boolean defaultValue = defaultSelection.contains(focusMeasuresArray[i]);

            mSelectedFeaturesMap.put(focusMeasuresArray[i], new Variable<Boolean>(focusMeasuresArray[i].getLongName(), defaultValue));
        }
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {

        StackInterfaceContainer lContainer = getLightSheetMicroscope().getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);

        Iterator<String> iterator = lContainer.keySet().iterator();
        String key = "";
        StackInterface lStack = null;
        while(iterator.hasNext()) {
            key = iterator.next();
            if (key.toLowerCase().contains(mKeyMustContainString.get().toLowerCase()) || mKeyMustContainString.get().length() == 0) {
                lStack = lContainer.get(key);
                break;
            }
        }
        if (lStack == null) {
            warning("Couldn't find key '" + mKeyMustContainString.get() + "' in containter " + lContainer + ". Skipping image quality measurement.");
            return false;
        }

        ClearCLIJ clij = ClearCLIJ.getInstance();

        // todo: conversion without GPU should be faster...
        ClearCLImage lUnsignedShortImage = clij.converter(lStack).getClearCLImage();
        ClearCLImage lFloatImage = clij.createCLImage(lUnsignedShortImage.getDimensions(), ImageChannelDataType.Float);

        Kernels.copy(clij, lUnsignedShortImage, lFloatImage);

        RandomAccessibleInterval<FloatType> floatData = (RandomAccessibleInterval<FloatType>) clij.converter(lFloatImage).getRandomAccessibleInterval();

        lUnsignedShortImage.close();
        lFloatImage.close();

        resultsTable = new ResultsTable();
        mFeatureSums = new HashMap<FocusMeasures.FocusMeasure, Double>();



        int numDimensions = floatData.numDimensions();

        if (numDimensions == 2) {
            resultsTable.incrementCounter();
            process2D(floatData, 0);
        } else if (numDimensions == 3) {
            int numberOfSlices = (int) floatData.dimension(2);

            for (int z = 0; z < numberOfSlices; z++)
            {
                System.out.println("Slice " + z);
                RandomAccessibleInterval<FloatType>
                        slice = Views.hyperSlice(floatData, 2, z);

                resultsTable.incrementCounter();

                process2D(slice, z);
            }
        }

        // save result to disc
        String targetFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        resultsTable.save(targetFolder + "/imageQuality" + pTimePoint + ".xls");

        // save mean average to disc
        String meanStatsFilename = targetFolder + "/imageQuality_mean.csv";
        String headline = "";
        File lOutputFile = new File(meanStatsFilename);
        boolean existedBefore = (lOutputFile.exists());

        String contentline = "";
        for (FocusMeasures.FocusMeasure focusMeasure : mSelectedFeaturesMap.keySet()) {
            if (mSelectedFeaturesMap.get(focusMeasure).get()) {
                double sumFeatureValue = mFeatureSums.get(focusMeasure);
                double meanFeatureValue = sumFeatureValue / resultsTable.getCounter();

                if (headline.length() == 0) {
                    contentline = "" + pTimePoint;
                    headline = "timepoint";
                }
                contentline = contentline + "," + meanFeatureValue;
                headline = headline + "," + focusMeasure.getLongName();
            }
        }
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(lOutputFile, true));
            if (!existedBefore) {
                writer.write(headline + "\n");
            }
            writer.write (contentline + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // save result to data warehouse
        for (FocusMeasures.FocusMeasure focusMeasure : mSelectedFeaturesMap.keySet()) {
            if (mSelectedFeaturesMap.get(focusMeasure).get()) {
                double[] measurements = new double[resultsTable.getCounter()];
                for (int i = 0; i < measurements.length; i++) {
                    measurements[i] = resultsTable.getValue(focusMeasure.getLongName(), i);
                }

                FocusMeasuresContainer lMeasurementContainer = new FocusMeasuresContainer(pTimePoint, focusMeasure, measurements);
                getLightSheetMicroscope().getDataWarehouse().put(focusMeasure.getLongName() + "_" + pTimePoint, lMeasurementContainer);
            }
        }

        return true;
    }


    private void process2D(RandomAccessibleInterval<FloatType> img, int slice) {
        resultsTable.addValue("slice", slice);

        DoubleArrayImage image = new DoubleArrayImageImgConverter(Views.iterable(img)).getDoubleArrayImage();


        for (FocusMeasures.FocusMeasure focusMeasure : mSelectedFeaturesMap.keySet()) {
            if (mSelectedFeaturesMap.get(focusMeasure).get()) {
                System.out.println("Determining " + focusMeasure.getLongName());
                double focusMeasureValue = FocusMeasures.computeFocusMeasure(focusMeasure, image);
                resultsTable.addValue(focusMeasure.getLongName(), focusMeasureValue);
                if (mFeatureSums.keySet().contains(focusMeasure)) {
                    focusMeasureValue += mFeatureSums.get(focusMeasure);
                    mFeatureSums.remove(focusMeasure);
                }
                mFeatureSums.put(focusMeasure, focusMeasureValue);
            }
        }
    }


    @Override
    public InstructionInterface copy() {
        MeasureImageQualityInstruction copied = new MeasureImageQualityInstruction(getLightSheetMicroscope());
        copied.mKeyMustContainString.set(mKeyMustContainString.get());


        for (FocusMeasures.FocusMeasure focusMeasure : mSelectedFeaturesMap.keySet()) {
            copied.mSelectedFeaturesMap.get(focusMeasure).set(mSelectedFeaturesMap.get(focusMeasure).get());
        }

        return copied;
    }

    public HashMap<FocusMeasures.FocusMeasure, Variable<Boolean>> getSelectedFeaturesMap() {
        return mSelectedFeaturesMap;
    }

    public Variable<String> getKeyMustContainString() {
        return mKeyMustContainString;
    }

    @Override
    public Variable[] getProperties() {
        Variable[] variables = new Variable[mSelectedFeaturesMap.keySet().size() + 1];
        int i = 0;
        for (FocusMeasures.FocusMeasure key : mSelectedFeaturesMap.keySet()) {
            variables[i] = mSelectedFeaturesMap.get(key);
            i++;
        }
        variables[i] = mKeyMustContainString;
        return variables;
    }
}
