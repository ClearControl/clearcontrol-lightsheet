package clearcontrol.microscope.lightsheet.adaptive.schedulers.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.state.spatial.PositionListContainer;
import clearcontrol.microscope.lightsheet.state.spatial.gui.PositionListContainerPanel;
import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

/**
 * SpaceTravelPathPlanningPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2018
 */
public class SpaceTravelPathPlanningPanel extends PositionListContainerPanel {
    public SpaceTravelPathPlanningPanel(SpaceTravelScheduler pSpaceTravelScheduler) {
        super(pSpaceTravelScheduler.getTravelPathList());

        PositionListContainer lTravelPathList = pSpaceTravelScheduler.getTravelPathList();

        // add current position button
        {
            Button lAddCurrentPositionButton = new Button("+");
            lAddCurrentPositionButton.setMinWidth(35);
            lAddCurrentPositionButton.setMinHeight(35);
            lAddCurrentPositionButton.setOnAction((e) -> {
                int lSelectedIndexInMainList = lListView.getSelectionModel().getSelectedIndex();
                if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lTravelPathList.size();
                pSpaceTravelScheduler.appendCurrentPositionToPath(lSelectedIndexInMainList);
                lListView.setItems(FXCollections.observableArrayList(lTravelPathList));
            });
            add(lAddCurrentPositionButton, 3, 3);
        }

        // go to current position
        {
            Button lGotoPositionButton = new Button(">");
            lGotoPositionButton.setMinWidth(35);
            lGotoPositionButton.setMinHeight(35);
            lGotoPositionButton.setOnAction((e) -> {
                int i = lListView.getSelectionModel().getSelectedIndex();
                if (i > -1)
                {
                    pSpaceTravelScheduler.goToPosition(i);
                }
            });
            add(lGotoPositionButton, 3, 4);
        }


        addIntegerField(pSpaceTravelScheduler.getSleepAfterMotionInMilliSeconds(), mRow);
        mRow++;

    }
}
