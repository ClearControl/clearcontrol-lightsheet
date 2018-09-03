package clearcontrol.microscope.lightsheet.imaging.sequential.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.imaging.sequential.SingleCameraFusionInstruction;

/**
 * SingleCameraFusionInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 06 2018
 */
public class SingleCameraFusionInstructionPanel extends CustomGridPane
{
  public SingleCameraFusionInstructionPanel(SingleCameraFusionInstruction pInstruction)
  {
    addIntegerField(pInstruction.getCameraIndexVariable(), 0);
  }
}
