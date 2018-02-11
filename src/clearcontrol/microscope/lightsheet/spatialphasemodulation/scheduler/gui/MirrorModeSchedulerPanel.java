package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.gui;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.file.VariableFileChooser;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.MirrorModeScheduler;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class MirrorModeSchedulerPanel extends
                                                           CustomGridPane
    implements LoggingFeature
{
  public MirrorModeSchedulerPanel(MirrorModeScheduler pMirrorModeScheduler) {


    int lRow = 0;
    {
      this.add(new Label("This will reconfigure a deformable mirror using a matrix from the given folder every ... time points as configured."), 0, lRow);

      lRow++;
    }

    {
      VariableFileChooser lRootFolderChooser =
          new VariableFileChooser("Folder:",
                                  pMirrorModeScheduler.getRootFolderVariable(),
                                  true);
      GridPane.setColumnSpan(lRootFolderChooser.getLabel(),
                             Integer.valueOf(1));
      GridPane.setColumnSpan(lRootFolderChooser.getTextField(),
                             Integer.valueOf(2));
      GridPane.setColumnSpan(lRootFolderChooser.getButton(),
                             Integer.valueOf(1));
      this.add(lRootFolderChooser.getLabel(), 0, lRow);
      this.add(lRootFolderChooser.getTextField(), 1, lRow);
      this.add(lRootFolderChooser.getButton(), 3, lRow);

      lRow++;
    }

    {
      BoundedVariable<Integer>
          lDelayFramesVariable = pMirrorModeScheduler.getDelayFramesVariable();
      NumberVariableTextField<Integer> lField =
          new NumberVariableTextField<Integer>(lDelayFramesVariable.getName(),
                                               lDelayFramesVariable,
                                               lDelayFramesVariable.getMin(),
                                               lDelayFramesVariable.getMax(),
                                               lDelayFramesVariable.getGranularity());
      this.add(lField.getLabel(), 0, lRow);
      this.add(lField.getTextField(), 1, lRow);
    }
  }
}
