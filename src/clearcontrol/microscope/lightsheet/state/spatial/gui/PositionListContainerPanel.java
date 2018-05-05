package clearcontrol.microscope.lightsheet.state.spatial.gui;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.state.spatial.PositionListContainer;
import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

/**
 * PositionListContainerPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class PositionListContainerPanel extends CustomGridPane {
    protected ListView<Position> lListView;
    protected int mRow = 0;

    public PositionListContainerPanel(PositionListContainer pPositionListContainer) {

        ArrayList<Position> lTravelPathList = pPositionListContainer;
        lListView = new ListView<Position>();
        lListView.setItems(FXCollections.observableArrayList(lTravelPathList));
        lListView.setMinWidth(450);

        add(lListView, 0, 0, 3, 5);

        {
            Button lMoveUpButton = new Button("^");
            lMoveUpButton.setMinWidth(35);
            lMoveUpButton.setMinHeight(35);
            lMoveUpButton.setOnAction((e) -> {
                int i = lListView.getSelectionModel().getSelectedIndex();
                if (i > 1)
                {
                    Position lPosition = lTravelPathList.get(i);
                    lTravelPathList.remove(i);
                    lTravelPathList.add(i - 1, lPosition);
                    lListView.setItems(FXCollections.observableArrayList(
                            lTravelPathList));
                }
            });
            add(lMoveUpButton, 3, mRow);
            mRow++;
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
                    Position lPosition = lTravelPathList.get(i);
                    lTravelPathList.remove(i);
                    lTravelPathList.add(i + 1, lPosition);
                    lListView.setItems(FXCollections.observableArrayList(
                            lTravelPathList));
                }
            });
            add(lMoveDownButton, 3, mRow);
            mRow++;
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
            add(lMinusButton, 3, mRow);
            mRow++;
        }


        mRow = 5;

        BoundedVariable<Double> lPositionX = new BoundedVariable<Double>("X", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.0001);
        BoundedVariable<Double> lPositionY = new BoundedVariable<Double>("Y", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.0001);
        BoundedVariable<Double> lPositionZ = new BoundedVariable<Double>("Z", 0.0,  -Double.MAX_VALUE, Double.MAX_VALUE, 0.0001);


        addDoubleField(lPositionX, mRow);
        mRow++;
        addDoubleField(lPositionY, mRow);
        mRow++;
        addDoubleField(lPositionZ, mRow);
        {
            Button lManualAddPosition = new Button("+");
            lManualAddPosition.setMinWidth(35);
            lManualAddPosition.setMinHeight(35);
            lManualAddPosition.setOnAction((e) -> {
                int lSelectedIndexInMainList = lListView.getSelectionModel().getSelectedIndex();
                if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lTravelPathList.size();
                lTravelPathList.add(lSelectedIndexInMainList, new Position(lPositionX.get(), lPositionY.get(), lPositionZ.get()));
                lListView.setItems(FXCollections.observableArrayList(lTravelPathList));
            });
            add(lManualAddPosition, 3, mRow);

        }
        mRow++;


    }
}
