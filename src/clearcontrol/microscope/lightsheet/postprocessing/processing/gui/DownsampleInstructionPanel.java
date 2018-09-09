package clearcontrol.microscope.lightsheet.postprocessing.processing.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.processing.DownsampleInstruction;

/**
 * DownsampleInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class DownsampleInstructionPanel extends CustomGridPane
{
  public DownsampleInstructionPanel(DownsampleInstruction pInstruction)
  {
    addDoubleField(pInstruction.getDownSampleFactorX(), 0);
    addDoubleField(pInstruction.getDownSampleFactorX(), 1);
    addDoubleField(pInstruction.getDownSampleFactorX(), 2);
  }
}
