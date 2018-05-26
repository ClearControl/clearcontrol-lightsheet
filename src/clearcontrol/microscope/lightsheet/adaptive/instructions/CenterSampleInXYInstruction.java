package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcl.imagej.ClearCLIJ;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.StackInterface;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.process.ImageStatistics;

public class CenterSampleInXYInstruction extends
        LightSheetMicroscopeInstruction implements
        InstructionInterface,
        LoggingFeature {

    private InterpolatedAcquisitionState mInterpolatedAcquisitionState;

    public CenterSampleInXYInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Center sample in XY", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        mInterpolatedAcquisitionState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();
        return true;
    }

    @Override
    public boolean enqueue(long l) {
        mInterpolatedAcquisitionState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

        // Take an image and show it
        //mInterpolatedAcquisitionState.getImageHeightVariable().set(2048);
        //mInterpolatedAcquisitionState.getImageWidthVariable().set(2048);

        //SingleViewAcquisitionInstruction lImage = mLightSheetMicroscope.getDevice(SingleViewAcquisitionInstruction.class, 0);
        //lImage.setMicroscope(mLightSheetMicroscope);

        ClearCLIJ clij = ClearCLIJ.getInstance();

        BasicStageInterface lStageX = null;
        BasicStageInterface lStageY = null;

        for (BasicStageInterface lStage : getLightSheetMicroscope().getDevices(BasicStageInterface.class)) {
            if (lStage.toString().contains("X")) {
                lStageX = lStage;
            }
            if (lStage.toString().contains("Y")) {
                lStageY = lStage;
            }
        }
        if (lStageX == null || lStageY == null) {
            warning("did not find stage interfaces!");
            return false;
        }

        double lZ = (mInterpolatedAcquisitionState.getStackZLowVariable().get().doubleValue() + mInterpolatedAcquisitionState.getStackZHighVariable().get().doubleValue()) / 2;

        SingleViewPlaneImager imager = new SingleViewPlaneImager(getLightSheetMicroscope(), (int) lZ);
        imager.setImageHeight(2048);
        imager.setImageWidth(2048);
        imager.setExposureTimeInSeconds(0.02);
        imager.setLightSheetIndex(0);
        imager.setDetectionArmIndex(0);

        // acquire an image
        StackInterface acquiredImageStack = imager.acquire();
        //clij.show(acquiredImageStack, "before test shift");
        ImagePlus impBeforeShift = clij.converter(acquiredImageStack).getImagePlus();

        // define a test-shift and move the stage
        double realShiftX = -0.01;
        double realShiftY = -0.01;
        lStageX.moveBy(realShiftX, true);
        lStageY.moveBy(realShiftY, true);

        // acquire another image after shifting
        acquiredImageStack = imager.acquire();
        //clij.show(acquiredImageStack, "after test shift");
        ImagePlus impAfterShift = clij.converter(acquiredImageStack).getImagePlus();

        // calculate center of mass for both images
        ImageStatistics beforeStats = impBeforeShift.getStatistics(Measurements.CENTER_OF_MASS);
        ImageStatistics afterStats = impAfterShift.getStatistics(Measurements.CENTER_OF_MASS);

        // determine how many pixels the image was shifted
        double virtualShiftX = afterStats.xCenterOfMass - beforeStats.xCenterOfMass;
        double virtualShiftY = afterStats.yCenterOfMass - beforeStats.yCenterOfMass;

        // define the virtual position where to the sample should be centered
        double wishPositionX = impBeforeShift.getWidth() / 2;
        double wishPositionY = impBeforeShift.getHeight() / 2;

        // transform the relative virtual shift to reel distances
        double realShiftToDoX = -(afterStats.xCenterOfMass - wishPositionX) / virtualShiftX * realShiftX;
        double realShiftToDoY = -(afterStats.yCenterOfMass - wishPositionY) / virtualShiftY * realShiftY;

        // center the sampe
        lStageX.moveBy(realShiftToDoX, true);
        lStageY.moveBy(realShiftToDoY, true);

        // measure final position
        acquiredImageStack = imager.acquire();
        ImagePlus impFinalShift = clij.converter(acquiredImageStack).getImagePlus();
        //clij.show(acquiredImageStack, "after final shift");

        ImageStatistics finalStats = impFinalShift.getStatistics(Measurements.CENTER_OF_MASS);
        double finalX = finalStats.xCenterOfMass;
        double finalY = finalStats.yCenterOfMass;
        info("finalX " + finalX + " (should be " + wishPositionX + ")");
        info("finalY " + finalY + " (should be " + wishPositionY + ")");

        return false;
    }
}
