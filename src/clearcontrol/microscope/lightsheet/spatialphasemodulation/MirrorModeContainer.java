package clearcontrol.microscope.lightsheet.spatialphasemodulation;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;

import org.ejml.data.DenseMatrix64F;

/**
 * MirrorModeContainer
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 05 2018
 */
public class MirrorModeContainer extends DataContainerBase
{
  DenseMatrix64F mMirrorMode = null;

  public MirrorModeContainer(long pTimePoint)
  {
    super(pTimePoint);
  }

  @Override
  public boolean isDataComplete()
  {
    return mMirrorMode != null;
  }

  @Override
  public void dispose()
  {
    mMirrorMode = null;
  }

  public void setMirrorMode(DenseMatrix64F pMatrix)
  {
    mMirrorMode = pMatrix;
  }

  public DenseMatrix64F getMirrorMode()
  {
    return mMirrorMode;
  }
}
