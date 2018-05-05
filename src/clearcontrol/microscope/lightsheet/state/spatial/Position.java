package clearcontrol.microscope.lightsheet.state.spatial;

/**
 * Position
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class Position {
    public Position(double x, double y, double z) {
        mX = x;
        mY = y;
        mZ = z;
    }

    public String toString() {
        return mX + "/" + mY + "/" + mZ;
    }

    public double mX;
    public double mY;
    public double mZ;
}
