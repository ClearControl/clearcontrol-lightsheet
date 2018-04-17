package clearcontrol.microscope.lightsheet.adaptive.schedulers.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderAlphaByVariationScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderZScheduler;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FocusFinderAlphaByVariationSchedulerPanel extends
                                                       CustomGridPane
{
  public FocusFinderAlphaByVariationSchedulerPanel(FocusFinderAlphaByVariationScheduler pFocusFinderAlphaByVariationScheduler) {
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getAlphaStepVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getNumberOfImagesToTakeVariable(), 0);
    addDoubleField(pFocusFinderAlphaByVariationScheduler.getExposureTimeInSecondsVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageWidthVariable(), 0);
    addIntegerField(pFocusFinderAlphaByVariationScheduler.getImageHeightVariable(), 0);
  }

}