package clearcontrol.microscope.lightsheet.imaging.opticsprefused.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.AppendConsecutiveHybridImagingInstruction;

/**
 * AppendConsecutiveHybridImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class AppendConsecutiveHybridImagingInstructionPanel extends
                                                            CustomGridPane
{
  public AppendConsecutiveHybridImagingInstructionPanel(AppendConsecutiveHybridImagingInstruction pInstruction)
  {
    addIntegerField(pInstruction.getNumberOfImages(), 0);
    addDoubleField(pInstruction.getFirstHalfIntervalInSeconds(), 1);
    addDoubleField(pInstruction.getSecondHalfIntervalInSeconds(), 2);
  }
}
