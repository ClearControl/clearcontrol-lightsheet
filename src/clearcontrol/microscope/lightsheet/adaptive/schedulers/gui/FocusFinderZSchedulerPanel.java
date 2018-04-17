package clearcontrol.microscope.lightsheet.adaptive.schedulers.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.checkbox.VariableCheckBox;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderZScheduler;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class FocusFinderZSchedulerPanel extends CustomGridPane
{
  public FocusFinderZSchedulerPanel(FocusFinderZScheduler pFocusFinderZScheduler) {
    addDoubleField(pFocusFinderZScheduler.getDeltaZVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getNumberOfImagesToTakeVariable(), 0);
    addDoubleField(pFocusFinderZScheduler.getExposureTimeInSecondsVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getImageWidthVariable(), 0);
    addIntegerField(pFocusFinderZScheduler.getImageHeightVariable(), 0);
    addCheckbox(pFocusFinderZScheduler.getResetAllTheTime(), 0);
  }

}
