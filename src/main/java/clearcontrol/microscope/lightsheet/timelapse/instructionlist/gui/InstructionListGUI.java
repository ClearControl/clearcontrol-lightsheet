package clearcontrol.microscope.lightsheet.timelapse.instructionlist.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.gui.InstructionListBuilderGUI;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.instructionlist.InstructionList;

/**
 * InstructionListGUI
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 10 2018
 */
public class InstructionListGUI extends CustomGridPane {
    public InstructionListGUI(InstructionList list) {
        CustomGridPane lSchedulerChecklistGridPane = new InstructionListBuilderGUI<LightSheetMicroscope>(list);

        add(lSchedulerChecklistGridPane, 0, mRow, 4, 1);
    }
}
