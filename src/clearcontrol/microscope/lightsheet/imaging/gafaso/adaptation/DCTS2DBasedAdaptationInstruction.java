package clearcontrol.microscope.lightsheet.imaging.gafaso.adaptation;

import clearcontrol.instructions.InstructionInterface;
import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.gafaso.AcquisitionStateSolution;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAdaptationInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;

/**
 * The DCTS2DBasedAdaptationInstruction measures DCTS2D slice by slice in a stack
 * acquired by the GAFASOAcquisition instruction and decides which solutions can
 * stay for the next iteration
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class DCTS2DBasedAdaptationInstruction extends GAFASOAdaptationInstructionBase {
    /**
     * INstanciates a virtual device with a given name
     *
     * @param pLightSheetMicroscope
     */
    public DCTS2DBasedAdaptationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Adaptation: GAFASO DCTS2D based adaptation", pLightSheetMicroscope);
    }

    @Override
    protected boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions) {
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        double[] qualityByPlane = new DCTS2D().computeImageQualityMetric((OffHeapPlanarStack) stack);
        double[] summedQualityPerAcquisitionStateSolution = new double[numberOfPositions];

        for (int i = 0; i < qualityByPlane.length; i++) {
            summedQualityPerAcquisitionStateSolution[i % numberOfPositions] += qualityByPlane[i];
        }

        for (int i = 0; i < numberOfPositions; i++) {
            population.getSolution(i).setFitness(summedQualityPerAcquisitionStateSolution[i]);
        }

        return true;
    }

    @Override
    public DCTS2DBasedAdaptationInstruction copy() {
        return new DCTS2DBasedAdaptationInstruction(getLightSheetMicroscope());
    }
}
