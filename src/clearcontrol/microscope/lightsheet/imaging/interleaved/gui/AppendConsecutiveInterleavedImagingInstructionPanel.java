package clearcontrol.microscope.lightsheet.imaging.interleaved.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.interleaved.AppendConsecutiveInterleavedImagingInstruction;

/**
 * AppendConsecutiveInterleavedImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class AppendConsecutiveInterleavedImagingInstructionPanel extends
                                                                 CustomGridPane
{
  public AppendConsecutiveInterleavedImagingInstructionPanel(AppendConsecutiveInterleavedImagingInstruction pInstruction)
  {
    addIntegerField(pInstruction.getNumberOfImages(), 0);
    addDoubleField(pInstruction.getIntervalInSeconds(), 1);
  }
}
