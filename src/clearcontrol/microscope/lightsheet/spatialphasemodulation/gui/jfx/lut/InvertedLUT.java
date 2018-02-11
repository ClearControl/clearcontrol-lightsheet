package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut;

import javafx.scene.paint.Color;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class InvertedLUT implements LookUpTable
{
  private LookUpTable mLookUpTable;

  public InvertedLUT(LookUpTable pLookUpTable) {
    mLookUpTable = pLookUpTable;
  }

  @Override public Color getColor(float pIndex)
  {
    return mLookUpTable.getColor(1.0f - pIndex);
  }
}
