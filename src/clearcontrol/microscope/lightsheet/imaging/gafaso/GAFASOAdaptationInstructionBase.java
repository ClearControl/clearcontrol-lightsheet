package clearcontrol.microscope.lightsheet.imaging.gafaso;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.containers.ProjectionCommentContainer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropAllContainersOfTypeInstruction;
import clearcontrol.stack.StackInterface;

/**
 * The GAFASOAdaptationInstructionBase read the most recent stack acquired by an GAFASOAcquisitionInstruction
 * and hands it over to a given implementation to decided which solutions can stay for the next round of acquisition.
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public abstract class GAFASOAdaptationInstructionBase extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    // debugging: todo: remove or set false per default
    private final Variable<Boolean> debug =
            new Variable<Boolean>("Debug",
                    true);

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName           device name
     * @param pLightSheetMicroscope
     */
    public GAFASOAdaptationInstructionBase(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope) {
        super(pDeviceName, pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {

        GAFASOStackInterfaceContainer gafasoContainer = getLightSheetMicroscope().getDataWarehouse().getNewestContainer(GAFASOStackInterfaceContainer.class);
        String key = gafasoContainer.keySet().iterator().next();

        StackInterface stack = gafasoContainer.get(key);

        GAFASOAcquisitionInstruction gafasoAcquisitionInstruction = getLightSheetMicroscope().getDevice(GAFASOAcquisitionInstruction.class, 0);
        int numberOfPositions = gafasoContainer.getNumberOfPositions();

        determineFitnessOfSolutions(gafasoAcquisitionInstruction, stack, numberOfPositions);
        Population<AcquisitionStateSolution> population = gafasoAcquisitionInstruction.getPopulation();

        // ---------------------------------------------------------------------
        // postprocessing: analyse image quality and advance population

        // debug
        if (debug.get())
        {
            new DropAllContainersOfTypeInstruction(ProjectionCommentContainer.class,
                    getLightSheetMicroscope().getDataWarehouse()).enqueue(pTimePoint);
            String comment = "";
            for (int i = 0; i < numberOfPositions; i++)
            {
                comment =
                        comment + population.getSolution(i).toString() + "\n";
                info(population.getSolution(i).toString());
            }
            getLightSheetMicroscope().getDataWarehouse()
                    .put("comment_" + pTimePoint,
                            new ProjectionCommentContainer(pTimePoint,
                                    comment));
        }

        population = population.runEpoch();
        fixLightSheetIndexOverflow(population, numberOfPositions);


        return true;
    }

    private void fixLightSheetIndexOverflow(Population<AcquisitionStateSolution> population, int numberOfPositions)
    {
        // fix illumination arm index overflow

        for (int i = 0; i < numberOfPositions; i++)
        {
            AcquisitionStateSolution solution = population.getSolution(i);
            if (!solution.state.keySet().contains(LightSheetDOF.II))
            {
                // If the light sheet index is not optimized, we can leave here.
                return;
            }
            if (solution.state.get(LightSheetDOF.II) < 0)
            {
                solution.state.remove(LightSheetDOF.II);
                solution.state.put(LightSheetDOF.II,
                        (double) getLightSheetMicroscope().getNumberOfLightSheets()
                                - 1);
            }
            if (solution.state.get(LightSheetDOF.II) >= getLightSheetMicroscope().getNumberOfLightSheets())
            {
                solution.state.remove(LightSheetDOF.II);
                solution.state.put(LightSheetDOF.II, 0.0);
            }
        }

    }


    protected abstract boolean determineFitnessOfSolutions(GAFASOAcquisitionInstruction gafasoAcquisitionInstruction, StackInterface stack, int numberOfPositions);
}
