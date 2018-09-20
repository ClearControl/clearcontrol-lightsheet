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
import ij.IJ;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.io.File;

/**
 *
 * The HighTenengradAreaBasedAdaptationInstruction measures area of maximum Tenengrad slice by slice in a stack
 * acquired by the GAFASOAcquisition instruction and decides which solutions can
 * stay for the next iteration
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class HighTenengradAreaBasedAdaptationInstruction extends GAFASOAdaptationInstructionBase {

    private final BoundedVariable<Double> tenengradBlurSigma =
            new BoundedVariable<Double>("Blur Tenengrad weights sigma (in pixels)",
                    0.0,
                    0.0,
                    Double.MAX_VALUE,
                    0.001);


    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public HighTenengradAreaBasedAdaptationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptation: GAFASO high Tenengrad area based adaptation", pLightSheetMicroscope);
    }

    @Override
    protected boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions) {
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage input = clij.converter(stack).getClearCLImage();
        ClearCLImage tenengradWeights =
                clij.createCLImage(input.getDimensions(),
                        ImageChannelDataType.Float);
        Kernels.tenengradWeightsSliceWise(clij, tenengradWeights, input);

        if (tenengradBlurSigma.get() > 0.001) {
            ClearCLImage temp = clij.createCLImage(input);
            Kernels.copy(clij, input, temp);
            Kernels.blurSlicewise(clij,
                    temp,
                    input,
                    (int) (tenengradBlurSigma.get() * 2),
                    (int) (tenengradBlurSigma.get() * 2),
                    tenengradBlurSigma.get().floatValue(),
                    tenengradBlurSigma.get().floatValue());
            temp.close();
        }

        ClearCLImage cropped = clij.createCLImage(new long[]
                {input.getWidth(),
                        input.getHeight(),
                        numberOfPositions}, tenengradWeights.getChannelDataType());

        ClearCLImage maxProjection = clij.createCLImage(new long[]
                {input.getWidth(),
                        input.getHeight()}, ImageChannelDataType.UnsignedInt16);
        ClearCLImage argMaxProjection = clij.createCLImage(new long[]
                {input.getWidth(),
                        input.getHeight()}, ImageChannelDataType.UnsignedInt16);

//        if (debug.get()) {
//            new File(getLightSheetMicroscope().getTimelapse()
//                    .getWorkingDirectory()
//                    + "/stacks/gafaso_debug/").mkdirs();
//        }

        long[] argMaxFrequencyTimesQuality = new long[numberOfPositions];
        for (int i = 0; i < input.getDepth() / numberOfPositions; i++) {
            Kernels.crop(clij,
                    tenengradWeights,
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

            // debug
//            if (debug.get()) {
//                IJ.saveAsTiff(clij.converter(argMaxProjection)
//                                .getImagePlus(),
//                        getLightSheetMicroscope().getTimelapse()
//                                .getWorkingDirectory()
//                                + "/stacks/gafaso_debug/argmax_"
//                                + stack.getMetaData().getIndex()
//                                + "_"
//                                + i
//                                + ".tif");
//            }
        }

        for (int j = 0; j < argMaxFrequencyTimesQuality.length; j++) {
            population.getSolution(j).setFitness(argMaxFrequencyTimesQuality[j]);
        }

        // cleanup
        input.close();
        tenengradWeights.close();
        cropped.close();

        maxProjection.close();
        argMaxProjection.close();
        return true;
    }

    @Override
    public HighTenengradAreaBasedAdaptationInstruction copy() {
        HighTenengradAreaBasedAdaptationInstruction copied = new HighTenengradAreaBasedAdaptationInstruction(getLightSheetMicroscope());
        copied.tenengradBlurSigma.set(tenengradBlurSigma.get());
        return copied;
    }
}
