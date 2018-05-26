package clearcontrol.microscope.lightsheet.warehouse.containers.io.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.ReadStackInterfaceContainerFromDiscInstruction;
import javafx.scene.layout.GridPane;

/**
 * ReadStackInterfaceContainerFromDiscSchedulerPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ReadStackInterfaceContainerFromDiscSchedulerPanel extends CustomGridPane {
    public ReadStackInterfaceContainerFromDiscSchedulerPanel(ReadStackInterfaceContainerFromDiscInstruction pScheduler) {
        int lRow = 0;

        {
            VariableFileChooser lRootFolderChooser =
                    new VariableFileChooser("Folder:",
                            pScheduler.getRootFolderVariable(),
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
