package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

import javax.xml.crypto.Data;

/**
 * This interface describes a general DataContainer which can be
 * stored in the DataWarehouse. This intermediate layer is to ensure
 * that not Objects of any kind can be stored in the DataWarehouse and
 * to ensure some systematic interface to access stored objects.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public interface DataContainerInterface
{
  public long getTimepoint();
  public boolean isDataComplete();
  public void dispose();
}
