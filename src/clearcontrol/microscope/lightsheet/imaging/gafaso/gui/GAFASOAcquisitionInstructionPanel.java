package clearcontrol.microscope.lightsheet.imaging.gafaso.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.gafaso.GAFASOAcquisitionInstruction;

/**
 * GAFASOAcquisitionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class GAFASOAcquisitionInstructionPanel extends CustomGridPane {
    public GAFASOAcquisitionInstructionPanel(GAFASOAcquisitionInstruction instruction) {
        int row = 0;

        addIntegerField(instruction.getDetectionArmIndex(),row++);
        addIntegerField(instruction.getLightSheetIndex(),row++);

        addIntegerField(instruction.getPopulationSize(), row++);

        addCheckbox(instruction.getOptimizeAlpha(), row++);
        addDoubleField(instruction.getStepSizeAlpha(), row++);
        addCheckbox(instruction.getOptimizeX(), row++);
        addDoubleField(instruction.getStepSizeX(), row++);
        addCheckbox(instruction.getOptimizeZ(), row++);
        addDoubleField(instruction.getStepSizeZ(), row++);

        addCheckbox(instruction.getDebug(), row++);
    }
}
