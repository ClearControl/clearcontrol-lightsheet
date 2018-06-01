package clearcontrol.microscope.lightsheet.adaptive.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.instructions.FocusFinderZInstruction;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FocusFinderZInstructionPanel extends CustomGridPane
{
  public FocusFinderZInstructionPanel(FocusFinderZInstruction pFocusFinderZScheduler) {
    addDoubleField(pFocusFinderZScheduler.getDeltaZVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getNumberOfImagesToTakeVariable(), 0);
    addDoubleField(pFocusFinderZScheduler.getExposureTimeInSecondsVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getImageWidthVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getImageHeightVariable(), 0);
    addCheckbox(pFocusFinderZScheduler.getResetAllTheTime(), 0);
  }

}