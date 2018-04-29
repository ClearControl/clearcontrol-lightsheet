package clearcontrol.devices.stages.kcube.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class BasicThreeAxesStagePanel extends CustomGridPane
{
  private BasicThreeAxesStageInterface mBasicThreeAxesStageInterface;

  private BoundedVariable<Double>
      mSlowSpeed =
      new BoundedVariable<Double>("slow speed",
                                  0.02,
                                  0.0,
                                  Double.MAX_VALUE,
                                  0.001);
  private BoundedVariable<Double>
      mFastSpeed =
      new BoundedVariable<Double>("fast speed",
                                  0.1,
                                  0.0,
                                  Double.MAX_VALUE,
                                  0.001);

  public BasicThreeAxesStagePanel(BasicThreeAxesStageInterface pBasicThreeAxesStageInterface)
  {
    mBasicThreeAxesStageInterface = pBasicThreeAxesStageInterface;

    // ---------------------------------------------------------------
    // X
    {
      Label lLabel = new Label("X/Y");
      add(lLabel, 2,2);
    }
    {
      Button lButton = new Button("<<");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveXBy(-mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 0, 2);
    }

    {
      Button lButton = new Button("<");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveXBy(-mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 1, 2);
    }

    {
      Button lButton = new Button(">");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveXBy(mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 3, 2);
    }
    {
      Button lButton = new Button(">>");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveXBy(mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 4, 2);
    }

    //----------------------------------------------------------------
    // Y
    {
      Button lButton = new Button("^\n^");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveYBy(-mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 2, 0);
    }
    {
      Button lButton = new Button("^");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveYBy(-mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 2, 1);
    }

    {
      Button lButton = new Button("v");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveYBy(mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 2, 3);
    }

    {
      Button lButton = new Button("v\nv");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveYBy(mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 2, 4);
    }

    //----------------------------------------------------------------
    // Z
    {
      Label lLabel = new Label("Z");
      add(lLabel, 5,2);
    }
    {
      Button lButton = new Button("^\n^");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveZBy(-mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 5, 0);
    }

    {
      Button lButton = new Button("^");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveZBy(-mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 5, 1);
    }

    {
      Button lButton = new Button("v");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveZBy(mSlowSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 5, 3);
    }
    {
      Button lButton = new Button("v\nv");
      styleButton(lButton);
      lButton.setOnAction(new EventHandler<ActionEvent>()
      {
        @Override public void handle(ActionEvent event)
        {
          enableAllButtons(false);
          mBasicThreeAxesStageInterface.moveZBy(mFastSpeed.get(), true);
          enableAllButtons(true);
        }
      });
      add(lButton, 5, 4);
    }

    int lRow = 0;
    int lDescriptionColumn = 8;
    int lFieldColumn = 9;
    {
      Label lLabel = new Label("X position (mm, read-only):");
      add(lLabel, lDescriptionColumn, lRow);
      NumberVariableTextField<Double>
          lTextField =
          new NumberVariableTextField<Double>("", mBasicThreeAxesStageInterface
              .getXPositionVariable(), Double.MIN_VALUE, Double.MAX_VALUE, 0.0000001);
      add(lTextField, lFieldColumn, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("Y position (mm, read-only):");
      add(lLabel, lDescriptionColumn, lRow);
      NumberVariableTextField<Double>
          lTextField =
          new NumberVariableTextField<Double>("", mBasicThreeAxesStageInterface
              .getYPositionVariable(), Double.MIN_VALUE, Double.MAX_VALUE, 0.0000001);
      add(lTextField, lFieldColumn, lRow);
      lRow++;
    }
    {
      Label lLabel = new Label("Z position (mm, read-only):");
      add(lLabel, lDescriptionColumn, lRow);
      NumberVariableTextField<Double>
          lTextField =
          new NumberVariableTextField<Double>("", mBasicThreeAxesStageInterface
              .getZPositionVariable(), Double.MIN_VALUE, Double.MAX_VALUE, 0.0000001);
      add(lTextField, lFieldColumn, lRow);
      lRow++;
    }

    {
      Label lLabel = new Label("Quick motion by (mm):");
      add(lLabel, lDescriptionColumn, lRow);
      NumberVariableTextField<Double>
          lTextField =
          new NumberVariableTextField<Double>("", mFastSpeed);
      add(lTextField, lFieldColumn, lRow);
      lRow++;
    }

    {
      Label lLabel = new Label("Slow motion by (mm):");
      add(lLabel, lDescriptionColumn, lRow);
      NumberVariableTextField<Double>
          lTextField =
          new NumberVariableTextField<Double>("", mSlowSpeed);
      add(lTextField, lFieldColumn, lRow);
      lRow++;
    }
  }

  private void enableAllButtons(boolean pEnabled)
  {
    for (Node node : this.getChildren())
    {
      node.setDisable(!pEnabled);
    }
  }

  private void styleButton(Button pButton) {
    pButton.setMinHeight(35);
    pButton.setMinWidth(35);
    pButton.setMaxHeight(35);
    pButton.setMaxWidth(35);
  }
}
