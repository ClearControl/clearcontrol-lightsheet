package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions.CenterMaxProjectionInstruction;

/**
 * CenterMaxProjectionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class CenterMaxProjectionInstructionPanel extends
                                                 CustomGridPane
{
  public CenterMaxProjectionInstructionPanel(CenterMaxProjectionInstruction pInstruction)
  {
    int row = 0;
    addStringField(pInstruction.getMustContainStringVariable(), row++);
    addIntegerField(pInstruction.getFontSizeVariable(), row++);
    addIntegerField(pInstruction.getStartZPlaneIndex(), row++);
    addIntegerField(pInstruction.getEndZPlaneIndex(), row++);

    addCheckbox(pInstruction.getPrintSequenceNameVariable(), row++);
    addCheckbox(pInstruction.getPrintTimePointVariable(), row++);
    addDoubleField(pInstruction.getScalingVariable(), row++);

    addCheckbox(pInstruction.getAutoContrast(), row++);
    addCheckbox(pInstruction.getColorProjection(), row++);
    addCheckbox(pInstruction.getCameraOffsetCorrection(), row++);
    addCheckbox(pInstruction.getUnevenIlluminationCorrectionBeforeProjection(), row++);
    addCheckbox(pInstruction.getUnevenIlluminationCorrectionAfterProjection(), row++);

  }
}
