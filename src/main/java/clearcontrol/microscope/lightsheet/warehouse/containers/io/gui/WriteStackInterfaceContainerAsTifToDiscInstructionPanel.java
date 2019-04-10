package clearcontrol.microscope.lightsheet.warehouse.containers.io.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsTifToDiscInstruction;

/**
 * WriteStackInterfaceContainerAsTifToDiscInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2019
 */
public class WriteStackInterfaceContainerAsTifToDiscInstructionPanel extends CustomGridPane {
    public WriteStackInterfaceContainerAsTifToDiscInstructionPanel(WriteStackInterfaceContainerAsTifToDiscInstruction instruction) {
        addCheckbox(instruction.getRecycleSavedContainers(), 0);
    }
}
