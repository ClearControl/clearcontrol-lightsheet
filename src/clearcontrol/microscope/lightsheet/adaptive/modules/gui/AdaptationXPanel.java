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
   * @param pAdaptationX
   *          adaptation X module
   */
  public AdaptationXPanel(AdaptationX pAdaptationX)
  {
    super(pAdaptationX);

    addNumberTextFieldForVariable("Min X: ",
                                  pAdaptationX.getMinXVariable());

    addNumberTextFieldForVariable("Max X: ",
                                  pAdaptationX.getMaxXVariable());

  }

}
