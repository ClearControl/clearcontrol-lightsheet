package clearcontrol.microscope.lightsheet.adaptive.modules.gui;

import clearcontrol.microscope.lightsheet.adaptive.modules.AdaptationX;

/**
 *
 *
 * @author royer
 */
public class AdaptationXPanel extends StandardAdaptationModulePanel
{

  /**
   * Instantiates an adaptation X panel
   * 
   * @param AdaptationX
   *          adaptation X module
   */
  public AdaptationXPanel(AdaptationX AdaptationX)
  {
    super(AdaptationX);

    addNumberTextFieldForVariable("Min X: ",
                                  AdaptationX.getMinXVariable(),
                                  Double.NEGATIVE_INFINITY,
                                  Double.POSITIVE_INFINITY,
                                  1d);

    addNumberTextFieldForVariable("Max X: ",
                                  AdaptationX.getMaxXVariable(),
                                  Double.NEGATIVE_INFINITY,
                                  Double.POSITIVE_INFINITY,
                                  1d);

  }

}
