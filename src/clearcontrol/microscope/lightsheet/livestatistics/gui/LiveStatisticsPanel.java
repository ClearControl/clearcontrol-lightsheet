package clearcontrol.microscope.lightsheet.livestatistics.gui;

import clearcontrol.gui.jfx.custom.visualconsole.VisualConsolePanel;
import clearcontrol.microscope.lightsheet.livestatistics.LiveStatisticsProcessor;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) December 2017
 */
public class LiveStatisticsPanel extends VisualConsolePanel
{

  /**
   * Instantiates a visual console
   *
   * @param pLiveStatisticsProcessor
   *          adaptor
   */
  public LiveStatisticsPanel(LiveStatisticsProcessor pLiveStatisticsProcessor)
  {
    super(pLiveStatisticsProcessor);
  }
}