package clearcontrol.microscope.lightsheet.warehouse;

import clearcontrol.core.log.LoggingFeature;

import java.util.HashMap;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class DataWarehouse extends HashMap<String, DataContainerInterface> implements
                                                           LoggingFeature
{
  @Override
  public DataContainerInterface put(String key, DataContainerInterface value) {
    if (containsKey(key)) {
      warning(key + " already exists!");
    }
    super.put(key, value);
    return value;
  }


}
