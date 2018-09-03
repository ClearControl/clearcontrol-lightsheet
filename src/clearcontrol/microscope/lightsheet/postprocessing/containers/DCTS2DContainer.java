package clearcontrol.microscope.lightsheet.postprocessing.containers;

/**
 * DCTS2DContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class DCTS2DContainer extends MeasurementInSpaceContainer
{

  public DCTS2DContainer(long pTimePoint,
                         double pX,
                         double pY,
                         double pZ,
                         double pDCTS2D)
  {
    super(pTimePoint, pX, pY, pZ, pDCTS2D);
  }
}
