package clearcontrol.microscope.lightsheet.smart.samplesearch;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.stack.StackInterface;

import java.util.ArrayList;

/**
 * SampleSearchInstructionBase
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public abstract class SampleSearchInstructionBase extends LightSheetMicroscopeInstructionBase {

    protected BoundedVariable<Double> mStepSizeInMillimetersVariable = new BoundedVariable<Double>("Step size in mm", 0.25, 0.01, Double.MAX_VALUE, 0.001);
    protected SpaceTravelInstruction mSampleCandidates;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
    public SampleSearchInstructionBase(String pDeviceName, LightSheetMicroscope pLightSheetMicroscope) {
        super(pDeviceName, pLightSheetMicroscope);
    }


    @Override
    public boolean initialize() {
        mSampleCandidates = new SpaceTravelInstruction(getLightSheetMicroscope());
        mSampleCandidates.initialize();
        return true;
    }

    public BoundedVariable<Double> getStepSizeInMillimetersVariable() {
        return mStepSizeInMillimetersVariable;
    }

    protected ArrayList<Double> measureAverageSignalIntensityAtCandiatePositions() {
        ArrayList<Position> lSampleCandidatePositionList = mSampleCandidates.getTravelPathList();


        ArrayList<Double> lAverageMeasurements = new ArrayList<Double>();

        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

        for (int i = 0; i < lSampleCandidatePositionList.size(); i++) {
            mSampleCandidates.goToPosition(i);
            SingleViewPlaneImager lImager = new SingleViewPlaneImager(getLightSheetMicroscope(), (lState.getStackZLowVariable().getMin().doubleValue() + lState.getStackZHighVariable().getMax().doubleValue()) /  2.0);
            lImager.setImageHeight(lState.getImageWidthVariable().get().intValue());
            lImager.setImageHeight(lState.getImageHeightVariable().get().intValue());
            lImager.setExposureTimeInSeconds(lState.getExposureInSecondsVariable().get().doubleValue());
            StackInterface lStack = lImager.acquire();

            ClearCLIJ clij = ClearCLIJ.getInstance();
            ClearCLImage lCLImage = clij.converter(lStack).getClearCLImage();
            double sum = Kernels.sumPixels(clij, lCLImage);
            lCLImage.close();
            double average = sum / (lState.getImageWidthVariable().get().intValue() * lState.getImageHeightVariable().get().intValue());
            lAverageMeasurements.add(average);
        }






        return lAverageMeasurements;
    }
}
