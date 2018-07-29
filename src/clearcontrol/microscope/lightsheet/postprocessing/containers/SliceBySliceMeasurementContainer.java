package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;
import org.apache.commons.math.stat.descriptive.moment.Mean;

import java.util.Arrays;

/**
 * SliceBySliceMeasurementContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 07 2018
 */
public class SliceBySliceMeasurementContainer extends DataContainerBase {

    double[] mMeasurements = null;
    Double mMeanMeasurement = null;

    public SliceBySliceMeasurementContainer(long pTimePoint, double[] pMeasurements) {
        super(pTimePoint);
        mMeasurements = new double[pMeasurements.length];
        System.arraycopy(pMeasurements, 0, mMeasurements, 0, mMeasurements.length);
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public double[] getMeasurements() {
        double[] lMeasurements = new double[mMeasurements.length];
        System.arraycopy(mMeasurements, 0, lMeasurements, 0, mMeasurements.length);
        return lMeasurements;
    }

    public double getMeanMeasurement() {
        if (mMeanMeasurement == null) {
            mMeanMeasurement = new Mean().evaluate(mMeasurements);
        }
        return mMeanMeasurement;
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + Arrays.toString(getMeasurements());
    }
}
