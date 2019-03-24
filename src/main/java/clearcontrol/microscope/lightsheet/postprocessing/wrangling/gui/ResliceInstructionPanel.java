package clearcontrol.microscope.lightsheet.postprocessing.wrangling.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.postprocessing.wrangling.ResliceInstruction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.ArrayList;

/**
 * ResliceInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class ResliceInstructionPanel extends CustomGridPane {
    public ResliceInstructionPanel(ResliceInstruction instruction) {

        ArrayList<String> entries = new ArrayList<String>();
        entries.add("LEFT");
        entries.add("RIGHT");
        entries.add("TOP");
        entries.add("BOTTOM");
        ObservableList<String> list = FXCollections.observableArrayList(entries);

        ComboBox directionComboBox = new ComboBox(list);
        directionComboBox.getSelectionModel().select((int)instruction.getDirection().get());
        directionComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                instruction.getDirection().set(directionComboBox.getSelectionModel().getSelectedIndex());
            }
        });

        add(new Label("Direction"),0 ,0);
        add(directionComboBox, 1, 0);
        addCheckbox(instruction.getRecycleSavedContainers(), 1);


    }
}
