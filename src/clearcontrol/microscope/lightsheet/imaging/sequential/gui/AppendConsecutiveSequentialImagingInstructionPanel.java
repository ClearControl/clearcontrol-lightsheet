package clearcontrol.microscope.lightsheet.imaging.sequential.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.sequential.AppendConsecutiveSequentialImagingInstruction;

/**
 * AppendConsecutiveSequentialImagingInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class AppendConsecutiveSequentialImagingInstructionPanel extends
                                                                CustomGridPane
{
  public AppendConsecutiveSequentialImagingInstructionPanel(AppendConsecutiveSequentialImagingInstruction pInstruction)
  {
    addIntegerField(pInstruction.getNumberOfImages(), 0);
    addDoubleField(pInstruction.getIntervalInSeconds(), 1);
  }
}
