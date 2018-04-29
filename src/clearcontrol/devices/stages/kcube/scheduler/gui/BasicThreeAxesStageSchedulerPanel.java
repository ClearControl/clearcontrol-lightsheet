package clearcontrol.devices.stages.kcube.scheduler.gui;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.scheduler.BasicThreeAxesStageScheduler;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class BasicThreeAxesStageSchedulerPanel extends CustomGridPane
{
  private int mRow = 0;

  public BasicThreeAxesStageSchedulerPanel(BasicThreeAxesStageScheduler pBasicThreeAxesStageScheduler) {

    addVariableTextField(pBasicThreeAxesStageScheduler.getStartXVariable());
    addVariableTextField(pBasicThreeAxesStageScheduler.getStartYVariable());
    addVariableTextField(pBasicThreeAxesStageScheduler.getStartZVariable());
    addVariableTextField(pBasicThreeAxesStageScheduler.getStopXVariable());
    addVariableTextField(pBasicThreeAxesStageScheduler.getStopYVariable());
    addVariableTextField(pBasicThreeAxesStageScheduler.getStopZVariable());

    NumberVariableTextField<Integer> lField = new NumberVariableTextField<Integer>(pBasicThreeAxesStageScheduler.getNumberOfStepsVariable().getName(), pBasicThreeAxesStageScheduler.getNumberOfStepsVariable());
    this.add(lField.getLabel(), 0, mRow);
    this.add(lField.getTextField(), 1, mRow);
    mRow++;

  }

  private void addVariableTextField(BoundedVariable<Double> pVariable) {
    NumberVariableTextField<Double> lField = new NumberVariableTextField<Double>(pVariable.getName(), pVariable);
    this.add(lField.getLabel(), 0, mRow);
    this.add(lField.getTextField(), 1, mRow);
    mRow++;
  }

}
