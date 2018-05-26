package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.instructions.GeneticAlgorithmMirrorModeOptimizeInstruction;

/**
 * GeneticAlgorithmMirrorModeOptimizeInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class GeneticAlgorithmMirrorModeOptimizeInstructionPanel extends CustomGridPane {
    public GeneticAlgorithmMirrorModeOptimizeInstructionPanel(GeneticAlgorithmMirrorModeOptimizeInstruction pGAScheduler) {
        addIntegerField(pGAScheduler.getNumberOfEpochsPerTimePoint(), 0);
        addIntegerField(pGAScheduler.getNumberOfMutations(), 1);
        addIntegerField(pGAScheduler.getPopulationSize(), 2);
        addDoubleField(pGAScheduler.getPositionZ(), 3);
        addDoubleField(pGAScheduler.getZernikeRangeFactor(), 4);
    }
}
