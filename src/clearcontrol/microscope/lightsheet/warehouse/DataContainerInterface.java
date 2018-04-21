package clearcontrol.microscope.lightsheet.warehouse;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

import javax.xml.crypto.Data;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public interface DataContainerInterface
{
  public long getTimepoint();
  public boolean isDataComplete();
  public void dispose();
}
