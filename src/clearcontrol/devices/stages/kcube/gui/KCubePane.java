package clearcontrol.devices.stages.kcube.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.impl.KCubeDevice;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class KCubePane extends CustomGridPane
  {
    KCubeDevice mKCubeDevice;

  public KCubePane(KCubeDevice pKCubeDevice)
    {
      //super(pKCubeDevice);
      mKCubeDevice = pKCubeDevice;

      int lRow = 0;
      System.out.println("Setting up K-cube Panel");

      Label lMinPos = new Label();
      add(lMinPos, 0, lRow);
      lRow++;

      Label lMaxPos = new Label();
      add(lMaxPos, 0, lRow);
      lRow++;

      Label lCurPos = new Label();
      add(lCurPos, 0, lRow);
      lRow++;

      {
        Button lButton = new Button("Read state");
        lButton.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override public void handle(ActionEvent event)
          {
            lMinPos.setText("" + mKCubeDevice.getMinPosition());
            lMaxPos.setText("" + mKCubeDevice.getMaxPosition());
            lCurPos.setText("" + mKCubeDevice.getCurrentPosition());
          }
        });
        add(lButton, 0, lRow);
        lRow++;
      }
      //BoundedVariable<Double > lPositionVariable = new BoundedVariable<Double>(mKCubeDevice.getName(), mKCubeDevice.getCurrentPosition(), mKCubeDevice.getMinPosition(), mKCubeDevice.getMaxPosition(), 0.0001);
      //NumberVariableTextField l


      lRow = 0;
      BoundedVariable<Double>
          lStepVariable =
          new BoundedVariable<Double>(mKCubeDevice.getName(),
                                      0.001,
                                      Double.MIN_VALUE,
                                      Double.MAX_VALUE,
                                      0.0001);

      {
        Button lButton = new Button("Up");
        lButton.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override public void handle(ActionEvent event)
          {
            mKCubeDevice.moveBy(lStepVariable.get(), false);
          }
        });
        add(lButton, 1, lRow);
        lRow++;
      }

      {
        NumberVariableTextField
            lStepTextField =
            new NumberVariableTextField("Step", lStepVariable);
        add(lStepTextField, 1, 1);
        lRow++;
      }

      {
        Button lButton = new Button("Down");
        lButton.setOnAction(new EventHandler<ActionEvent>()
        {
          @Override public void handle(ActionEvent event)
          {
            mKCubeDevice.moveBy(-1 * lStepVariable.get(), false);
          }
        });
        add(lButton, 1, lRow);
        lRow++;
      }

    }

  }
