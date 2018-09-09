package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions.RandomSingleZernikeModesInstruction;

public class RandomSingleZernikeModesInstructionPanel extends
                                                      CustomGridPane
{
  public RandomSingleZernikeModesInstructionPanel(RandomSingleZernikeModesInstruction pInstruction)
  {
    for (int i = 0; i < 66; i++)
    {
      addDoubleField(pInstruction.getRangeOfZernikeCoefficientArray(i),
                     i);
    }
  }
}
