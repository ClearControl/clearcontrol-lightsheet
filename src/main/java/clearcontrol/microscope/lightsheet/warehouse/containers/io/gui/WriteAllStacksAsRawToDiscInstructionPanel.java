package clearcontrol.microscope.lightsheet.warehouse.containers.io.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteAllStacksAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteSpecificStackToSpecificRawFolderInstruction;

/**
 * WriteSpecificStackToSpecificRawFolderInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class WriteAllStacksAsRawToDiscInstructionPanel extends
                                                                   CustomGridPane
{
  public WriteAllStacksAsRawToDiscInstructionPanel(WriteAllStacksAsRawToDiscInstruction pInstruction)
  {
    addCheckbox(pInstruction.getRecycleSavedContainers(), 0);
  }
}
