package clearcontrol.microscope.lightsheet.warehouse.containers.io.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteAllStacksAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteAllStacksAsSQYToDiscInstruction;

/**
 * WriteSpecificStackToSpecificRawFolderInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class WriteAllStacksAsSQYToDiscInstructionPanel extends
                                                                   CustomGridPane
{
  public WriteAllStacksAsSQYToDiscInstructionPanel(WriteAllStacksAsSQYToDiscInstruction pInstruction)
  {
    addCheckbox(pInstruction.getRecycleSavedContainers(), 0);
  }
}
