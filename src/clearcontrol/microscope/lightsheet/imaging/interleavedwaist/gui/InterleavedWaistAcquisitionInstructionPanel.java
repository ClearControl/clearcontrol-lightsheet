package clearcontrol.microscope.lightsheet.imaging.interleavedwaist.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.interleavedwaist.InterleavedWaistAcquisitionInstruction;

/**
 * InterleavedWaistAcquisitionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class InterleavedWaistAcquisitionInstructionPanel extends
                                                         CustomGridPane
{
  public InterleavedWaistAcquisitionInstructionPanel(InterleavedWaistAcquisitionInstruction instruction)
  {
    int row = 0;
    addIntegerField(instruction.getLightSheetIndex(), row++);
    row++;
    for (int i =
               0; i < instruction.getLightSheetXPositions().length; i++)
    {
      addDoubleField(instruction.getLightSheetXPositions()[i], row);
      row++;
      addDoubleField(instruction.getLightSheetYPositions()[i], row);
      row++;
      addDoubleField(instruction.getLightSheetDeltaZPositions()[i],
                     row);
      row++;
    }
  }
}
