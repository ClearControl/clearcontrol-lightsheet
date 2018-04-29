package clearcontrol.microscope.lightsheet.adaptive.schedulers.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
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
public class SpaceTravelPathPlanningPanel extends CustomGridPane {
    public SpaceTravelPathPlanningPanel(SpaceTravelScheduler pSpaceTravelScheduler) {


        ArrayList<SpaceTravelScheduler.Position> lTravelPathList = pSpaceTravelScheduler.getTravelPathList();
        ListView<SpaceTravelScheduler.Position> lListView = new ListView<SpaceTravelScheduler.Position>();
        lListView.setItems(FXCollections.observableArrayList(lTravelPathList));
        lListView.setMinWidth(450);

        add(lListView, 0, 0, 3, 5);

        int lRow = 0;
        {
            Button lMoveUpButton = new Button("^");
            lMoveUpButton.setMinWidth(35);
            lMoveUpButton.setMinHeight(35);
            lMoveUpButton.setOnAction((e) -> {
                int i = lListView.getSelectionModel().getSelectedIndex();
                if (i > 1)
                {
                    SpaceTravelScheduler.Position lPosition = lTravelPathList.get(i);
                    lTravelPathList.remove(i);
                    lTravelPathList.add(i - 1, lPosition);
                    lListView.setItems(FXCollections.observableArrayList(
                            lTravelPathList));
                }
            });
            add(lMoveUpButton, 3, lRow);
            lRow++;
        }

        {
            Button lMoveDownButton = new Button("v");
            lMoveDownButton.setMinWidth(35);
            lMoveDownButton.setMinHeight(35);
            lMoveDownButton.setOnAction((e) -> {
                int count = 0;
                int i = lListView.getSelectionModel().getSelectedIndex();
                if (i >= 0 && i < lTravelPathList.size() - 1)
                {
                    SpaceTravelScheduler.Position lPosition = lTravelPathList.get(i);
                    lTravelPathList.remove(i);
                    lTravelPathList.add(i + 1, lPosition);
                    lListView.setItems(FXCollections.observableArrayList(
                            lTravelPathList));
                }
            });
            add(lMoveDownButton, 3, lRow);
            lRow++;
        }


        {
            Button lMinusButton = new Button("-");
            lMinusButton.setMinWidth(35);
            lMinusButton.setMinHeight(35);
            lMinusButton.setOnAction((e) -> {
                int count = 0;
                int lSelectedIndex = lListView.getSelectionModel().getSelectedIndex();
                for (int i : lListView.getSelectionModel()
                        .getSelectedIndices()
                        .sorted())
                {
                    lTravelPathList.remove(i - count);
                    count++;
                }
                lListView.setItems(FXCollections.observableArrayList(
                        lTravelPathList));
                lListView.getSelectionModel().select(lSelectedIndex);
            });
            GridPane.setValignment(lMinusButton, VPos.BOTTOM);
            add(lMinusButton, 3, lRow);
            lRow++;
        }


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
            add(lAddCurrentPositionButton, 3, lRow);
            lRow++;
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
            add(lGotoPositionButton, 3, lRow);
            lRow++;
        }

        BoundedVariable<Double> lPositionX = new BoundedVariable<Double>("X", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
        BoundedVariable<Double> lPositionY = new BoundedVariable<Double>("Y", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
        BoundedVariable<Double> lPositionZ = new BoundedVariable<Double>("Z", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.1);


        addDoubleField(lPositionX, lRow);
        lRow++;
        addDoubleField(lPositionY, lRow);
        lRow++;
        addDoubleField(lPositionZ, lRow);
        {
            Button lManualAddPosition = new Button("+");
            lManualAddPosition.setMinWidth(35);
            lManualAddPosition.setMinHeight(35);
            lManualAddPosition.setOnAction((e) -> {
                int lSelectedIndexInMainList = lListView.getSelectionModel().getSelectedIndex();
                if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lTravelPathList.size();
                lTravelPathList.add(lSelectedIndexInMainList, new SpaceTravelScheduler.Position(lPositionX.get(), lPositionY.get(), lPositionZ.get()));
                lListView.setItems(FXCollections.observableArrayList(lTravelPathList));
            });
            add(lManualAddPosition, 3, lRow);

        }
        lRow++;

        addIntegerField(pSpaceTravelScheduler.getSleepAfterMotionInMilliSeconds(), lRow);
    }
}
