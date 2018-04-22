package clearcontrol.microscope.lightsheet.warehouse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;
import clearcontrol.microscope.lightsheet.warehouse.containers.RecyclableContainer;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

import java.util.HashMap;
import java.util.Stack;

/**
 * The DataWarehouse represents central data storage. It allows
 * collecting a number of DataContainers containing image data grouped
 * per timepoint. It has its own recycler to ensure memory stays under
 * a certain limit.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class DataWarehouse extends HashMap<String, DataContainerInterface> implements
                                                           LoggingFeature
{
  private RecyclerInterface<StackInterface, StackRequest> mRecycler;

  public DataWarehouse (RecyclerInterface<StackInterface, StackRequest> pRecycler) {
    mRecycler = pRecycler;
  }

  @Override
  public DataContainerInterface put(String key, DataContainerInterface value) {
    if (containsKey(key)) {
      warning(key + " already exists!");
    }
    super.put(key, value);
    return value;
  }

  public <DCI extends DataContainerInterface> DCI getOldestContainer(Class pClass) {
    long lMinimumTimePoint = Long.MAX_VALUE;
    DCI lOldestContainer = null;
    for (String key : keySet()) {
      DataContainerInterface lContainer = get(key);
      if (pClass.isInstance(lContainer) && lContainer.getTimepoint() < lMinimumTimePoint) {
        lMinimumTimePoint = lContainer.getTimepoint();
        lOldestContainer = (DCI)lContainer;
      }
    }

    return lOldestContainer;
  }

  public void disposeContainer(DataContainerInterface pContainer) {
    if (pContainer instanceof RecyclableContainer) {
      ((RecyclableContainer) pContainer).recycle(mRecycler);
    } else {
      pContainer.dispose();
    }

    for (String key : keySet())
    {
      DataContainerInterface lContainer = get(key);
      if (lContainer == pContainer)
      {
        remove(key);
        return;
      }
    }
  }

  @Override public void clear()
  {
    for (DataContainerInterface lContainer : values()) {
      if (lContainer instanceof RecyclableContainer) {
        ((RecyclableContainer) lContainer).recycle(mRecycler);
      } else {
        lContainer.dispose();
      }
    }
    super.clear();
  }

  public RecyclerInterface<StackInterface, StackRequest> getRecycler() {
    return mRecycler;
  }
}
