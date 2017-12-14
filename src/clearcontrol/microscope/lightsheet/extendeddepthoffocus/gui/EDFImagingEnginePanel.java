package clearcontrol.microscope.lightsheet.extendeddepthoffocus.gui;

import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;
import clearcontrol.microscope.lightsheet.extendeddepthoffocus.EDFImagingEngine;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) November 2017
 */
public class EDFImagingEnginePanel extends VisualConsolePanel
{

  /**
   * Instantiates a visual console
   *
   * @param pEDFImagingEngine
   *          adaptor
   */
  public EDFImagingEnginePanel(EDFImagingEngine pEDFImagingEngine)
  {
    super(pEDFImagingEngine);
  }
}
