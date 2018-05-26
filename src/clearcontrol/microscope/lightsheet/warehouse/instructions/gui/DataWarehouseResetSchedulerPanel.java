package clearcontrol.microscope.lightsheet.warehouse.instructions.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseResetInstruction;
import javafx.scene.control.Button;

/**
 * DataWarehouseResetSchedulerPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DataWarehouseResetSchedulerPanel extends CustomGridPane {

    public DataWarehouseResetSchedulerPanel(DataWarehouseResetInstruction pScheduler) {
        Button lResetButton = new Button("Reset");
        lResetButton.setOnAction((e)-> {
            pScheduler.enqueue( - 1);
        });
        add(lResetButton, 0, 0);
    }
}
