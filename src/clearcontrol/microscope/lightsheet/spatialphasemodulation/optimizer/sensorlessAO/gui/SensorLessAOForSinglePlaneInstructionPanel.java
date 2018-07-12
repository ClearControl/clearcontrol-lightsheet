package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.gui;

import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.sensorlessAO.SensorLessAOForSinglePlaneInstruction;

public class SensorLessAOForSinglePlaneInstructionPanel extends CustomGridPane {
    public SensorLessAOForSinglePlaneInstructionPanel(SensorLessAOForSinglePlaneInstruction pInstruction) {
        addIntegerField(pInstruction.getZernikeFactor(), 0);
        addDoubleField(pInstruction.getPositionZ(), 1);
        addDoubleField(pInstruction.getstepSize(), 2);
        addIntegerField(pInstruction.getNumberOfTilesX(), 3);
        addIntegerField(pInstruction.getmNumberOfTilesY(),4);
    }
}

