package clearcontrol.microscope.lightsheet.imaging.gafaso.adaptation;

import autopilot.measures.FocusMeasures;
import clearcl.imagej.ClearCLIJ;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.gafaso.AcquisitionStateSolution;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAdaptationInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.stack.StackInterface;
import de.mpicbg.rhaase.spimcat.postprocessing.fijiplugins.imageanalysis.quality.MeasureQualityPerSlicePlugin;
import ij.ImagePlus;

/**
 * FocusMeasureBasedAdaptationInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class FocusMeasureBasedAdaptationInstruction extends GAFASOAdaptationInstructionBase {

    FocusMeasures.FocusMeasure focusMeasure;

    /**
     *
     * @param focusMeasure
     * @param pLightSheetMicroscope
     */
    public FocusMeasureBasedAdaptationInstruction(FocusMeasures.FocusMeasure focusMeasure, LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptation: GAFASO " + focusMeasure.name() + " based adaptation", pLightSheetMicroscope);
        this.focusMeasure = focusMeasure;
    }

    @Override
    protected boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions) {
        ClearCLIJ clij = ClearCLIJ.getInstance();
        ImagePlus input = clij.converter(stack).getImagePlus();
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        // measure focus
        double[] qualityByPlane = new MeasureQualityPerSlicePlugin(input).analyseFocusMeasure(focusMeasure);
        double[] summedQualityPerAcquisitionStateSolution = new double[numberOfPositions];

        for (int i = 0; i < qualityByPlane.length; i++) {
            summedQualityPerAcquisitionStateSolution[i % numberOfPositions] += qualityByPlane[i];
        }

        for (int i = 0; i < numberOfPositions; i++) {
            population.getSolution(i).setFitness(summedQualityPerAcquisitionStateSolution[i]);
        }

        return false;
    }

    @Override
    public FocusMeasureBasedAdaptationInstruction copy() {
        return new FocusMeasureBasedAdaptationInstruction(focusMeasure, getLightSheetMicroscope());
    }
}
