package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.matrixeditors.DenseMatrixEditor;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import org.ejml.data.DenseMatrix64F;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;

public class RandomActuatorPositionScheduler extends SchedulerBase implements
        LoggingFeature {

    private SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
    private double mMax_actuatorPos;
    private double mMin_acuatorPos;

    public RandomActuatorPositionScheduler(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {
        super("Adaptation: Random actuator position scheduler for " + pSpatialPhaseModulatorDeviceInterface.getName());
        mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
        mMax_actuatorPos = 0.4;
        mMin_acuatorPos = -0.4;

    }

    @Override public boolean initialize()
    {
        return true;
    }

    @Override public boolean enqueue(long pTimePoint) {
        DenseMatrix64F lMatrix =
                mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get();

        Random rand = new Random();
        DecimalFormat df = new DecimalFormat("#.##");

        for (int i = 0; i< lMatrix.numRows; i++) {
            for (int j = 0; j< lMatrix.numCols; j++){
                double value = Double.parseDouble(df.format(mMin_acuatorPos + (mMax_actuatorPos - mMin_acuatorPos)*rand.nextDouble()));
                lMatrix.set(i,j, value);
            }
        }

        DenseMatrix64F lTargetMatrix = lMatrix.copy();
        mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(lTargetMatrix);

        info("Setting Mirror actuators" + lTargetMatrix.toString());
        return true;
    }
}