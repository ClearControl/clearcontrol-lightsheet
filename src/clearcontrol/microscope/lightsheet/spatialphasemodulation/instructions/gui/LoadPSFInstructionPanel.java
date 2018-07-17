package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.LoadPSFInstruction;
import javafx.scene.layout.GridPane;

/**
 * LoadPSFInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class LoadPSFInstructionPanel extends CustomGridPane {
    public LoadPSFInstructionPanel(LoadPSFInstruction pInstruction) {

        int lRow = 0;
        {
            VariableFileChooser lRootFolderChooser =
                    new VariableFileChooser("Folder:",
                            pInstruction.getRootFolderVariable(),
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


        BoundedVariable<Double>[] lZernikeFactors = pInstruction.getZernikeFactorVariables();
        for (int i = 0; i < lZernikeFactors.length; i++ ) {
            addDoubleField(lZernikeFactors[i], lRow);
            lRow++;
        }
    }
}
