package clearcontrol.devices.lasers.instructions.gui;

import clearcontrol.devices.lasers.instructions.LaserOnOffInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;

/**
 * LaserOnOffInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class LaserOnOffInstructionPanel extends CustomGridPane {
    public LaserOnOffInstructionPanel(LaserOnOffInstruction pInstruction){
        addCheckbox(pInstruction.getDebugVariable(), 0);
    }
}
