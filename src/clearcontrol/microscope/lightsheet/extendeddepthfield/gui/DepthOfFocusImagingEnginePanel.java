package clearcontrol.microscope.lightsheet.extendeddepthfield.gui;

import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;
import clearcontrol.microscope.lightsheet.extendeddepthfield.DepthOfFocusImagingEngine;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * November 2017
 */
public class DepthOfFocusImagingEnginePanel extends VisualConsolePanel
{

  /**
   * Instantiates a visual console
   *
   * @param pDepthOfFocusImagingEngine adaptor
   */
  public DepthOfFocusImagingEnginePanel(DepthOfFocusImagingEngine pDepthOfFocusImagingEngine)
  {
    super(pDepthOfFocusImagingEngine);
  }
}
