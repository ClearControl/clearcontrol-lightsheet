package clearcontrol.microscope.lightsheet.configurationstate.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.configurationstate.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class ConfigurationStatePanel extends CustomGridPane
{


  public ConfigurationStatePanel(ArrayList lObjectList, int pNumberOfLightSheets) {

    int posX = 0;
    for (int x = 0; x < lObjectList.size(); x++) {
      Object lObject = lObjectList.get(x);
      if (lObject instanceof HasConfigurationState && lObject instanceof HasName)
      {
        Label lLabel = new Label(((HasName)lObject).getName());
        add(lLabel, posX + 1, 0);
        posX++;
      }
    }
    for (int y = 0; y < pNumberOfLightSheets; y++)
    {
      Label lLabel = new Label("L" + y);
      add(lLabel, 0, y + 1);
    }

    posX = 0;
    for (int x = 0; x < lObjectList.size(); x++) {
      Object lObject = lObjectList.get(x);
      if (lObject instanceof HasConfigurationState && lObject instanceof HasName)
      {
        HasConfigurationState lHasConfigurationState = (HasConfigurationState)lObject;
        if (lHasConfigurationState instanceof HasConfigurationStatePerLightSheet)
        {
          HasConfigurationStatePerLightSheet
              lHasConfigurationStatePerLightSheet =
              (HasConfigurationStatePerLightSheet) lHasConfigurationState;

          for (int y = 0; y < pNumberOfLightSheets; y++)
          {
            ConfigurationStateLabel lConfigurationStateLabel =
                new ConfigurationStateLabel("", "");
            lHasConfigurationStatePerLightSheet.addConfigurationStateChangeListener(
                new ConfigurationStatePerLightSheetChangeListener()
                {
                  @Override public void configurationStateChanged(
                      HasConfigurationState pHasConfigurationState)
                  {
                  }

                  @Override public void configurationStateOfLightSheetChanged(
                      HasConfigurationStatePerLightSheet pHasConfigurationStatePerLightSheet,
                      int pLightSheetIndex)
                  {
                    ConfigurationState
                        lConfigurationState =
                        pHasConfigurationStatePerLightSheet.getConfigurationState(
                            pLightSheetIndex);

                    String lConfigurationStateDescription = lConfigurationState.toString();

                    if (pHasConfigurationStatePerLightSheet instanceof HasStateDescriptionPerLightSheet) {
                      lConfigurationStateDescription += "\n" + ((HasStateDescriptionPerLightSheet) pHasConfigurationStatePerLightSheet).getStateDescription(pLightSheetIndex);
                    } else if (pHasConfigurationStatePerLightSheet instanceof HasStateDescription) {
                      lConfigurationStateDescription += "\n" + ((HasStateDescription) pHasConfigurationStatePerLightSheet).getStateDescription();
                    }

                    lConfigurationStateLabel.getStringVariable()
                                            .set("" + lConfigurationStateDescription);
                    lConfigurationStateLabel.setStyle(
                        "-fx-border-color:white; -fx-font-color:white; -fx-background-color: "
                        + lConfigurationState.getColor().toLowerCase()
                        + ";");
                  }
                });
            add(lConfigurationStateLabel, posX + 1, y + 1);
          }
        }
        else
        {
          ConfigurationStateLabel lConfigurationStateLabel =
              new ConfigurationStateLabel("", "");

          lHasConfigurationState.addConfigurationStateChangeListener(
              new ConfigurationStateChangeListener()
              {
                @Override public void configurationStateChanged(
                    HasConfigurationState pHasConfigurationState)
                {
                  ConfigurationState
                      lConfigurationState =
                      pHasConfigurationState.getConfigurationState();

                  String lConfigurationStateDescription = lConfigurationState.toString();

                  if (pHasConfigurationState instanceof HasStateDescription) {
                    lConfigurationStateDescription += "\n" + ((HasStateDescription) pHasConfigurationState).getStateDescription();
                  }

                  lConfigurationStateLabel.getStringVariable()
                                          .set("" + lConfigurationStateDescription);
                  lConfigurationStateLabel.setStyle(
                      "-fx-border-color:white; -fx-font-color:white; -fx-background-color: "
                      + lConfigurationState.getColor().toLowerCase()
                      + ";");
                }
              });
          GridPane.setFillHeight(lConfigurationStateLabel, true);
          GridPane.setRowSpan(lConfigurationStateLabel,
                              pNumberOfLightSheets);
          add(lConfigurationStateLabel, posX + 1, 1);
        }
        posX++;
      }
    }
  }

}
