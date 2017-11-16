package clearcontrol.microscope.lightsheet.adaptive.modules.gui;

import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationRH;
import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationZ;

/**
 *
 * @author royer
 * @author Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class AdaptationRHPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation Z panel
   *
   * @param pAdaptationRH
   *          adaptation RH module
   */
  public AdaptationRHPanel(AdaptationRH pAdaptationRH)
  {
    super(pAdaptationRH);

    addNumberTextFieldForVariable("Delta Z: ",
                                  pAdaptationRH.getDeltaZVariable(),
                                  0.0,
                                  Double.POSITIVE_INFINITY,
                                  0.001);


    addNumberTextFieldForVariable("Block size: ",
                                  pAdaptationRH.getBlockSizeVariable(),
                                  0,
                                  Integer.MAX_VALUE, 1);
  }

}
