package clearcontrol.microscope.lightsheet.postprocessing.containers;

public class SliceBySliceMeasurementInSpaceContainer extends SliceBySliceMeasurementContainer {

    Double mX = null;
    Double mY = null;
    Double mZ = null;

    public SliceBySliceMeasurementInSpaceContainer(long pTimePoint, double pX, double pY, double pZ, double[] pMeasurement) {
        super(pTimePoint, pMeasurement);
        mX = pX;
        mY = pY;
        mZ = pZ;
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


    public String toString() {
        return this.getClass().getSimpleName() + " " + getX() + "/"  + getY() + "/"  + getZ() + " " + getMeasurement();
    }
}
