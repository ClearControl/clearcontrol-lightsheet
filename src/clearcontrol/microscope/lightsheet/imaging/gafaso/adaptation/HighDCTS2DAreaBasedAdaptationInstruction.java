package clearcontrol.microscope.lightsheet.imaging.gafaso.adaptation;

import autopilot.measures.FocusMeasures;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.gafaso.AcquisitionStateSolution;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAdaptationInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.quality.MeasureQualityInTilesPlugin;
import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * HighDCTS2DAreaBasedAdaptationInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class HighDCTS2DAreaBasedAdaptationInstruction extends GAFASOAdaptationInstructionBase {


    /**
     *
     * @param pLightSheetMicroscope
     */
    public HighDCTS2DAreaBasedAdaptationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptation: GAFASO high DCTS area based adaptation", pLightSheetMicroscope);
    }

    @Override
    protected boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions) {
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ImagePlus input = clij.converter(stack).getImagePlus();
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        int tileSize = 16;

        // Measure quality
        MeasureQualityInTilesPlugin mqt = new MeasureQualityInTilesPlugin(input, input.getWidth() / tileSize, input.getHeight() / tileSize);
        mqt.setSilent(true);
        mqt.setShowResult(false);
        ImagePlus qualityImage = mqt.analyseFocusMeasure(FocusMeasures.FocusMeasure.SpectralNormDCTEntropyShannon);
        ClearCLImage qualityCLImage = clij.converter(qualityImage).getClearCLImage();


        // measure areas
        ClearCLImage cropped = clij.createCLImage(new long[]
                {qualityCLImage.getWidth(),
                        qualityCLImage.getHeight(),
                        numberOfPositions}, ImageChannelDataType.Float);

        ClearCLImage maxProjection = clij.createCLImage(new long[]
                {qualityCLImage.getWidth(),
                        qualityCLImage.getHeight()}, ImageChannelDataType.Float);
        ClearCLImage argMaxProjection = clij.createCLImage(new long[]
                {qualityCLImage.getWidth(),
                        qualityCLImage.getHeight()}, ImageChannelDataType.Float);

        long[] argMaxFrequencyTimesQuality = new long[numberOfPositions];
        for (int i = 0; i < qualityCLImage.getDepth() / numberOfPositions; i++) {
            Kernels.crop(clij,
                    qualityCLImage,
                    cropped,
                    0,
                    0,
                    (int) (qualityCLImage.getDepth() / numberOfPositions) * i);

            Kernels.argMaxProjection(clij,
                    cropped,
                    maxProjection,
                    argMaxProjection);

            //clij.show(cropped, "cropped");
            //clij.show(maxProjection, "max");
            //clij.show(argMaxProjection, "argmax");

            RandomAccessibleInterval<FloatType> maxImg = (RandomAccessibleInterval<FloatType>) clij.converter(maxProjection)
                    .getRandomAccessibleInterval();


            RandomAccessibleInterval<FloatType> argMaxImg =
                    (RandomAccessibleInterval<FloatType>) clij.converter(argMaxProjection)
                            .getRandomAccessibleInterval();

            //RandomAccess<FloatType> randomAccess = maxImg.randomAccess();

            Cursor<FloatType> cursor = Views.iterable(argMaxImg)
                    .localizingCursor();

            while (cursor.hasNext()) {
                FloatType value = cursor.next();
                //randomAccess.setPosition(cursor);
                argMaxFrequencyTimesQuality[((int)value.get()) % numberOfPositions] ++; //= randomAccess.get().get();
            }
        }

        for (int j = 0; j < argMaxFrequencyTimesQuality.length; j++) {
            population.getSolution(j).setFitness(argMaxFrequencyTimesQuality[j]);
        }

        // cleanup
        qualityCLImage.close();
        cropped.close();

        maxProjection.close();
        argMaxProjection.close();

        return false;
    }

    @Override
    public HighDCTS2DAreaBasedAdaptationInstruction copy() {
        return new HighDCTS2DAreaBasedAdaptationInstruction(getLightSheetMicroscope());
    }
}
