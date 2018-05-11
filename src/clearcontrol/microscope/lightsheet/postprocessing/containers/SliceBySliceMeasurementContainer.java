package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

public class SliceBySliceMeasurementContainer extends DataContainerBase {

    double[] mMeasurement = null;

    public SliceBySliceMeasurementContainer(long pTimePoint, double[] pMeasurement) {
        super(pTimePoint);
        mMeasurement = pMeasurement;
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public double[] getMeasurement() {
        return mMeasurement;
    }


    public String toString() {
        return this.getClass().getSimpleName() + " " + getMeasurement();
    }
}
