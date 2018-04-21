package clearcontrol.microscope.lightsheet.warehouse.containers;

import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public interface RecyclableContainer
{
  void recycle(RecyclerInterface<StackInterface, StackRequest> pRecycler);
}
