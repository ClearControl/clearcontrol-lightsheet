package clearcontrol.microscope.lightsheet.imaging.sequential.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialSingleCameraFusionInstruction;

/**
 * SequentialSingleCameraFusionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class SequentialSingleCameraFusionInstructionPanel extends
                                                          CustomGridPane
{
  public SequentialSingleCameraFusionInstructionPanel(SequentialSingleCameraFusionInstruction pInstruction)
  {
    addIntegerField(pInstruction.getCameraIndex(), 0);
  }
}
