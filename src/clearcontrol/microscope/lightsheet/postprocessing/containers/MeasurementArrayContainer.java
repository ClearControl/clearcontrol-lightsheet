package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

public class MeasurementArrayContainer  extends DataContainerBase {
    private int mDepth;
    private int mHeight;
    private int mWidth;

    private double[][][] mMeasurement = null;


    public MeasurementArrayContainer(long pTimePoint, int pDepth, int pHeight, int pWidth, double[][][] pMeasurement) {
        super(pTimePoint);
        mDepth = pDepth;
        mHeight = pHeight;
        mWidth = pWidth;
        mMeasurement = pMeasurement;
    }

    @Override
    public boolean isDataComplete() {
        return true;
    }

    @Override
    public void dispose() {
    }

    public double[][][] getMeasurement() {
        return mMeasurement;
    }
    public int getDepth(){return mDepth;}
    public int getHeight(){return mHeight;}
    public int getWidth(){return mWidth;}


    public String toString() {
        return this.getClass().getSimpleName() + " " + getMeasurement();
    }
}