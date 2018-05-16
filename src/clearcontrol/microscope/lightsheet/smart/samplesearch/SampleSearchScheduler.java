package clearcontrol.microscope.lightsheet.smart.samplesearch;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.stack.StackInterface;

import java.util.ArrayList;

/**
 * SampleSearchScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public abstract class SampleSearchScheduler extends SchedulerBase {

    protected BoundedVariable<Double> mStepSizeInMillimetersVariable = new BoundedVariable<Double>("Step size in mm", 0.25, 0.01, Double.MAX_VALUE, 0.001);
    protected SpaceTravelScheduler mSampleCandidates;
    protected LightSheetMicroscope mLightSheetMicroscope;

    /**
     * INstanciates a virtual device with a given name
     *
     * @param pDeviceName device name
     */
    public SampleSearchScheduler(String pDeviceName) {
        super(pDeviceName);
    }


    @Override
    public boolean initialize() {
        if (mMicroscope instanceof  LightSheetMicroscope) {
            mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
        }
        mSampleCandidates = new SpaceTravelScheduler();
        mSampleCandidates.setMicroscope(mMicroscope);
        mSampleCandidates.initialize();
        return true;
    }

    public BoundedVariable<Double> getStepSizeInMillimetersVariable() {
        return mStepSizeInMillimetersVariable;
    }

    protected ArrayList<Double> measureAverageSignalIntensityAtCandiatePositions() {
        ArrayList<Position> lSampleCandidatePositionList = mSampleCandidates.getTravelPathList();


        ArrayList<Double> lAverageMeasurements = new ArrayList<Double>();

        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();

        for (int i = 0; i < lSampleCandidatePositionList.size(); i++) {
            mSampleCandidates.goToPosition(i);
            SingleViewPlaneImager lImager = new SingleViewPlaneImager(mLightSheetMicroscope, (lState.getStackZLowVariable().getMin().doubleValue() + lState.getStackZHighVariable().getMax().doubleValue()) /  2.0);
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
