package clearcontrol.instructions.implementations.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.instructions.implementations.PauseUntilAFileAppearsInstruction;
import javafx.scene.layout.GridPane;

public class PauseUntilAFileAppearsInstructionPanel extends CustomGridPane {
    public PauseUntilAFileAppearsInstructionPanel(PauseUntilAFileAppearsInstruction instruction) {

        int lRow = 0;
        addIntegerField(instruction.getPauseTimeInMilliseconds(),
                lRow);
        lRow ++;

        {
            VariableFileChooser lRootFolderChooser =
                    new VariableFileChooser("Folder:",
                            instruction.getRootFolderVariable(),
                            true);
            GridPane.setColumnSpan(lRootFolderChooser.getLabel(),
                    Integer.valueOf(1));
            GridPane.setColumnSpan(lRootFolderChooser.getTextField(),
                    Integer.valueOf(2));
            GridPane.setColumnSpan(lRootFolderChooser.getButton(),
                    Integer.valueOf(1));
            this.add(lRootFolderChooser.getLabel(), 0, lRow);
            this.add(lRootFolderChooser.getTextField(), 1, lRow);
            this.add(lRootFolderChooser.getButton(), 3, lRow);

            lRow++;
        }
    }
}
