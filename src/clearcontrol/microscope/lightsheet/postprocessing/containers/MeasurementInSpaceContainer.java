package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * MeasurementInSpaceContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class MeasurementInSpaceContainer extends DataContainerBase {
    Double mX = null;
    Double mY = null;
    Double mZ = null;
    Double mMeasurement = null;

    public MeasurementInSpaceContainer(LightSheetMicroscope pLightSheetMicroscope, double pX, double pY, double pZ, double pMeasurement) {
        super(pLightSheetMicroscope);
        mX = pX;
        mY = pY;
        mZ = pZ;
        mMeasurement = pMeasurement;
    }


    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public Double getX() {
        return mX;
    }

    public Double getY() {
        return mY;
    }

    public Double getZ() {
        return mZ;
    }

    public Double getMeasurement() {
        return mMeasurement;
    }
}
