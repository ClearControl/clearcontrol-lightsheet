package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.io.InterpolatedAcquisitionStateWriter;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;

import java.io.File;

/**
 * This scheduler writes the current acquisition state to a file with
 * the timepoint being part of the filename
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * April 2018
 */
public class InterpolatedAcquisitionStateLogScheduler extends SchedulerBase implements
                                                               SchedulerInterface,
                                                               LoggingFeature
{

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public InterpolatedAcquisitionStateLogScheduler()
  {
    super("IO: Log current acquisition state to disc");
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (!(mMicroscope instanceof LightSheetMicroscope)) {
      warning("I need a LightSheetMicroscope!");
      return false;
    }
    LightSheetTimelapse lTimelapse =
        (LightSheetTimelapse) mMicroscope.getDevice(LightSheetTimelapse.class, 0);

    InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState)(mMicroscope.getAcquisitionStateManager().getCurrentState());

    File tempFile = new File(lTimelapse.getWorkingDirectory(), "state_t" + pTimePoint + ".acqstate");
    System.out.println(tempFile);

    new InterpolatedAcquisitionStateWriter(tempFile, lState).write();

    return true;
  }
}
