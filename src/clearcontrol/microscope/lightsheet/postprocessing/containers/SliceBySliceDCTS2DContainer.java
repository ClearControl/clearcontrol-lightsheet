package clearcontrol.microscope.lightsheet.postprocessing.containers;


import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

/**
 * DCTS2DContainer
 * <p>
 * <p>
 * <p>
 * Author: @debayansaha102
 * 05 2018
 */

public class SliceBySliceDCTS2DContainer extends DataContainerBase {

    double[] mMeasurements = null;

    public SliceBySliceDCTS2DContainer(long pTimePoint, double pX, double pY, double pZ, double[] pDCTS2D) {
        super(pTimePoint);
        mMeasurements = new double[pDCTS2D.length];
        System.arraycopy(pDCTS2D, 0, mMeasurements, 0, mMeasurements.length);
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public double[] getMeasurements() {
        return mMeasurements;
    }

}


