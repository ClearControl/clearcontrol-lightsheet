package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * MeasurementContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class MeasurementContainer extends DataContainerBase {
    Double mMeasurement = null;

    public MeasurementContainer(LightSheetMicroscope pLightSheetMicroscope, double pMeasurement) {
        super(pLightSheetMicroscope);
        mMeasurement = pMeasurement;
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public Double getMeasurement() {
        return mMeasurement;
    }


    public String toString() {
        return this.getClass().getSimpleName() + " " + getMeasurement();
    }
}
