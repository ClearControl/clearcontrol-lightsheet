package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.defocusdiversity.DefocusDiversityInstruction;

/**
 * DefocusDiversityInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 07 2018
 */
public class DefocusDiversityInstructionPanel extends CustomGridPane
{
  public DefocusDiversityInstructionPanel(DefocusDiversityInstruction pInstruction)
  {
    addDoubleField(pInstruction.getStepSize(), 0);
    addIntegerField(pInstruction.getDetectionArmIndex(), 1);
    addIntegerField(pInstruction.getLightsheetIndex(), 2);
  }
}
