package clearcontrol.instructions;

import clearcontrol.core.device.name.NameableInterface;
import clearcontrol.microscope.MicroscopeInterface;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public interface InstructionInterface extends NameableInterface
{
  boolean initialize();

  boolean enqueue(long pTimePoint);

  public void setMicroscope(MicroscopeInterface pMicroscope);
}
