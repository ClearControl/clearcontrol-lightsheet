package clearcontrol.microscope.lightsheet.imaging.gafaso.adaptation;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.gafaso.AcquisitionStateSolution;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAdaptationInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.stack.StackInterface;
import de.mpicbg.spimcat.spotdetection.GPUSpotDetectionSliceBySlice;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.util.HashMap;

/**
 *
 * The SpotDetectionBasedAdaptationInstruction measures number of spots slice by slice in a stack
 * acquired by the GAFASOAcquisition instruction and decides which solutions can
 * stay for the next iteration
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class SpotDetectionBasedAdaptationInstruction extends GAFASOAdaptationInstructionBase {

    BoundedVariable<Double> threshold = new BoundedVariable<Double>("Object segmentation threshold", 400.0, 0.0, Double.MAX_VALUE, 1.0);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public SpotDetectionBasedAdaptationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptation: GAFASO spot detection based adaptation", pLightSheetMicroscope);
    }

    @Override
    protected boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions) {
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        // do a slice by slice spot detection to see which condition allows to see local maxima
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage input = clij.converter(stack).getClearCLImage();
        ClearCLImage output = clij.createCLImage(input);
        GPUSpotDetectionSliceBySlice gpusdsbs = new GPUSpotDetectionSliceBySlice(clij, input, output, 400);
        gpusdsbs.exec();

        // now we count the number of cells in 16x16 sized blocks
        ClearCLImage downsampledBy2 = clij.createCLImage(new long[]{output.getWidth() / 2, output.getHeight() / 2, output.getDepth()}, output.getChannelDataType());
        ClearCLImage downsampledBy4 = clij.createCLImage(new long[]{output.getWidth() / 4, output.getHeight() / 4, output.getDepth()}, output.getChannelDataType());
        ClearCLImage downsampledBy8 = clij.createCLImage(new long[]{output.getWidth() / 8, output.getHeight() / 8, output.getDepth()}, output.getChannelDataType());
        ClearCLImage downsampledBy16 = clij.createCLImage(new long[]{output.getWidth() / 16, output.getHeight() / 16, output.getDepth()}, output.getChannelDataType());

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("src", output);
        map.put("dst", downsampledBy2);
        clij.execute(GAFASOAcquisitionInstruction.class, "reduction.cl", "downsample_3d_factor2_sum_slice_by_slice", map);

        map.clear();
        map.put("src", downsampledBy2);
        map.put("dst", downsampledBy4);
        clij.execute(GAFASOAcquisitionInstruction.class, "reduction.cl", "downsample_3d_factor2_sum_slice_by_slice", map);

        map.clear();
        map.put("src", downsampledBy4);
        map.put("dst", downsampledBy8);
        clij.execute(GAFASOAcquisitionInstruction.class, "reduction.cl", "downsample_3d_factor2_sum_slice_by_slice", map);

        map.clear();
        map.put("src", downsampledBy8);
        map.put("dst", downsampledBy16);
        clij.execute(GAFASOAcquisitionInstruction.class, "reduction.cl", "downsample_3d_factor2_sum_slice_by_slice", map);

        ClearCLImage cropped = clij.createCLImage(new long[]
                {downsampledBy16.getWidth(),
                        downsampledBy16.getHeight(),
                        numberOfPositions}, downsampledBy16.getChannelDataType());

        ClearCLImage maxProjection = clij.createCLImage(new long[]
                {downsampledBy16.getWidth(),
                        downsampledBy16.getHeight()}, ImageChannelDataType.UnsignedInt16);
        ClearCLImage argMaxProjection = clij.createCLImage(new long[]
                {downsampledBy16.getWidth(),
                        downsampledBy16.getHeight()}, ImageChannelDataType.UnsignedInt16);

        // now we slice the blocks according to the imaging conditions and take the number of blocks with
        // maxmimum number of spots. The slice with the most spots per block is promoted for further usage
        long[] argMaxFrequencyTimesQuality = new long[numberOfPositions];
        for (int i = 0; i < input.getDepth() / numberOfPositions; i++) {
            Kernels.crop(clij,
                    downsampledBy16,
                    cropped,
                    0,
                    0,
                    (int) (input.getDepth() / numberOfPositions) * i);

            Kernels.argMaxProjection(clij,
                    cropped,
                    maxProjection,
                    argMaxProjection);

            RandomAccessibleInterval<UnsignedShortType> maxImg = (RandomAccessibleInterval<UnsignedShortType>) clij.converter(maxProjection)
                    .getRandomAccessibleInterval();


            RandomAccessibleInterval<UnsignedShortType> argMaxImg =
                    (RandomAccessibleInterval<UnsignedShortType>) clij.converter(argMaxProjection)
                            .getRandomAccessibleInterval();

            RandomAccess<UnsignedShortType> randomAccess = maxImg.randomAccess();

            Cursor<UnsignedShortType> cursor = Views.iterable(argMaxImg)
                    .localizingCursor();

            while (cursor.hasNext()) {
                UnsignedShortType value = cursor.next();
                randomAccess.setPosition(cursor);
                argMaxFrequencyTimesQuality[value.get() % numberOfPositions] += randomAccess.get().get();
            }
        }

        for (int j = 0; j < argMaxFrequencyTimesQuality.length; j++) {
            population.getSolution(j).setFitness(argMaxFrequencyTimesQuality[j]);
        }

        input.close();
        output.close();
        downsampledBy2.close();
        downsampledBy4.close();
        downsampledBy8.close();
        downsampledBy16.close();
        cropped.close();

        maxProjection.close();
        argMaxProjection.close();
        return true;
    }

    @Override
    public SpotDetectionBasedAdaptationInstruction copy() {
        SpotDetectionBasedAdaptationInstruction copied = new SpotDetectionBasedAdaptationInstruction(getLightSheetMicroscope());

        return copied;
    }
}
