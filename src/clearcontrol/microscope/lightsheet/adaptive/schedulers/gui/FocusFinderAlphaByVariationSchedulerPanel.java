package clearcontrol.microscope.lightsheet.adaptive.schedulers.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderAlphaByVariationInstruction;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FocusFinderAlphaByVariationSchedulerPanel extends
                                                       CustomGridPane
{
  public FocusFinderAlphaByVariationSchedulerPanel(FocusFinderAlphaByVariationInstruction pFocusFinderAlphaByVariationScheduler) {
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getAlphaStepVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getNumberOfImagesToTakeVariable(), 0);
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getExposureTimeInSecondsVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageWidthVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageHeightVariable(), 0);
  }

}