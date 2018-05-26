package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.SchedulerBase;
import clearcontrol.instructions.SchedulerInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.tables.InterpolationTables;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class AcquisitionStateResetScheduler extends SchedulerBase implements
                                                               SchedulerInterface,
                                                               LoggingFeature
{
  LightSheetMicroscope mLightSheetMicroscope;

  /**
   * INstanciates a virtual device with a given name
   *
   */
  public AcquisitionStateResetScheduler()
  {
    super("Adaptation: Reset acquisition state");
  }

  @Override public boolean initialize()
  {
    if (mMicroscope instanceof LightSheetMicroscope){
      mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
    }
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    InterpolatedAcquisitionState lAcquisitionState = (InterpolatedAcquisitionState)mLightSheetMicroscope.getAcquisitionStateManager().getCurrentState();

    for (int lLightSheetIndex = 0; lLightSheetIndex < mLightSheetMicroscope.getNumberOfLightSheets(); lLightSheetIndex++) {
      for (int cpi = 0; cpi < lAcquisitionState.getNumberOfControlPlanes(); cpi++) {
        InterpolationTables it = lAcquisitionState.getInterpolationTables();
        it.set(LightSheetDOF.IZ, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IY, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IX, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IA, cpi, lLightSheetIndex, 0);
        it.set(LightSheetDOF.IW, cpi, lLightSheetIndex, 0.45); // Todo: this value is XWing specific
        it.set(LightSheetDOF.IH, cpi, lLightSheetIndex, 500); // Todo: this value is XWing speciific
      }
    }

    return true;
  }
}
