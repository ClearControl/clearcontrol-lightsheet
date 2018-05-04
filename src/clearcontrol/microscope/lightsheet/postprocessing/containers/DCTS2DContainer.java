package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * DCTS2DContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DCTS2DContainer extends DataContainerBase {

    Double mX = null;
    Double mY = null;
    Double mZ = null;
    Double mDCTS2D = null;

    public DCTS2DContainer(LightSheetMicroscope pLightSheetMicroscope, double pX, double pY, double pZ, double pDCTS2D) {
        super(pLightSheetMicroscope);
        mX = pX;
        mY = pY;
        mZ = pZ;
        mDCTS2D = pDCTS2D;
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

    public Double getDCTS2D() {
        return mDCTS2D;
    }
}
