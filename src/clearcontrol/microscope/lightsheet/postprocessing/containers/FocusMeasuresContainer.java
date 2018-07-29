package clearcontrol.microscope.lightsheet.postprocessing.containers;

import autopilot.measures.FocusMeasures;

import java.util.Arrays;

/**
 * FocusMeasuresContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class FocusMeasuresContainer extends SliceBySliceMeasurementContainer {

    FocusMeasures.FocusMeasure mFocusMeasure;

    public FocusMeasuresContainer(long pTimePoint, FocusMeasures.FocusMeasure pFocusMeasure, double[] pMeasurement) {
        super(pTimePoint, pMeasurement);
        mFocusMeasure = pFocusMeasure;
    }

    public FocusMeasures.FocusMeasure getFocusMeasure() {
        return mFocusMeasure;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + mFocusMeasure + " " + Arrays.toString(getMeasurements());
    }
}
