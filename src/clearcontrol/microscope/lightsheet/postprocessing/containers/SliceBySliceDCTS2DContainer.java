package clearcontrol.microscope.lightsheet.postprocessing.containers;


import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;
import org.apache.commons.math.stat.descriptive.moment.Mean;

/**
 * DCTS2DContainer
 * <p>
 * <p>
 * <p>
 * Author: @debayansaha102
 * 05 2018
 */

public class SliceBySliceDCTS2DContainer extends SliceBySliceMeasurementContainer {

    public SliceBySliceDCTS2DContainer(long pTimePoint, double[] pDCTS2D) {
        super(pTimePoint, pDCTS2D);
    }

}


