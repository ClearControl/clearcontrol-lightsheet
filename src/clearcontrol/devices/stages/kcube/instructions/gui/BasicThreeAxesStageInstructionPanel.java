package clearcontrol.devices.stages.kcube.instructions.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.instructions.BasicThreeAxesStageInstruction;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class BasicThreeAxesStageInstructionPanel extends CustomGridPane
{
  private int mRow = 0;

  public BasicThreeAxesStageInstructionPanel(BasicThreeAxesStageInstruction pBasicThreeAxesStageScheduler) {

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

    addCheckbox(pBasicThreeAxesStageScheduler.getRestartAfterFinishVariable(), mRow);
    mRow++;

  }

  private void addVariableTextField(BoundedVariable<Double> pVariable) {
    NumberVariableTextField<Double> lField = new NumberVariableTextField<Double>(pVariable.getName(), pVariable);
    this.add(lField.getLabel(), 0, mRow);
    this.add(lField.getTextField(), 1, mRow);
    mRow++;
  }

}
