package clearcontrol.microscope.lightsheet.smart.samplesearch.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.smart.samplesearch.MoveInBoundingBoxInstruction;

/**
 * MoveInBoundingBoxInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class MoveInBoundingBoxInstructionPanel extends CustomGridPane {
    public MoveInBoundingBoxInstructionPanel(MoveInBoundingBoxInstruction instruction) {
        addDoubleField(instruction.getMinXPosition(), 0);
        addDoubleField(instruction.getMinYPosition(), 1);
        addDoubleField(instruction.getMinZPosition(), 2);

        addDoubleField(instruction.getMaxXPosition(), 3);
        addDoubleField(instruction.getMaxYPosition(), 4);
        addDoubleField(instruction.getMaxZPosition(), 5);

        addIntegerField(instruction.getStepsX(), 6);
        addIntegerField(instruction.getStepsY(), 7);
        addIntegerField(instruction.getStepsZ(), 8);
    }
 }
