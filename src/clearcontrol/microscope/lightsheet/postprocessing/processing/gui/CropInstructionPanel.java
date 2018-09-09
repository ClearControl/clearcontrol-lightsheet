package clearcontrol.microscope.lightsheet.postprocessing.processing.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.processing.CropInstruction;

/**
 * CropInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class CropInstructionPanel extends CustomGridPane
{
  public CropInstructionPanel(CropInstruction pInstruction)
  {
    addIntegerField(pInstruction.getCropXVariable(), 0);
    addIntegerField(pInstruction.getCropYVariable(), 1);
    addIntegerField(pInstruction.getCropZVariable(), 2);
    addIntegerField(pInstruction.getCropWidthVariable(), 3);
    addIntegerField(pInstruction.getCropHeightVariable(), 4);
    addIntegerField(pInstruction.getCropDepthVariable(), 5);
  }
}
