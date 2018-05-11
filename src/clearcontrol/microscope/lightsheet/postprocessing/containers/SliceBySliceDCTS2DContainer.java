package clearcontrol.microscope.lightsheet.postprocessing.containers;


public class SliceBySliceDCTS2DContainer extends SliceBySliceMeasurementInSpaceContainer{


    public SliceBySliceDCTS2DContainer(long pTimePoint, double pX, double pY, double pZ, double[] pDCTS2D) {
            super(pTimePoint, pX, pY, pZ, pDCTS2D);
        }
}
