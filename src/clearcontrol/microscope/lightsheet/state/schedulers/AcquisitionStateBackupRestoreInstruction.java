package clearcontrol.microscope.lightsheet.state.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.state.AcquisitionStateInterface;

import java.util.ArrayList;

/**
 * This instructions allows saving a copy of the current acquisition state
 * to a list or restoring the latest acquisition state if the list is
 * not empty.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * March 2018
 */
public class AcquisitionStateBackupRestoreInstruction extends
        InstructionBase implements
        InstructionInterface,
                                                                  LoggingFeature
{
  boolean mBackup;
  private static ArrayList<AcquisitionStateInterface> mAcquisitionStateList = new ArrayList<>();
  /**
   * INstanciates a virtual device with a given name
   *
   * @param pBackup: If true, the instructions puts a new entry in
   * the LIFO list, if false it will restore the last entry.
   */
  public AcquisitionStateBackupRestoreInstruction(boolean pBackup)
  {
    super("Adaptation: " + (pBackup? ("Backup"):("Restore")) + " acquisition state");
    mBackup = pBackup;
  }

  @Override public boolean initialize()
  {
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    if (mBackup) {
      AcquisitionStateInterface lState = mMicroscope.getAcquisitionStateManager().getCurrentState().duplicate("backup " + System.currentTimeMillis());
      mAcquisitionStateList.add(lState);
    } else {
      if (mAcquisitionStateList.size() > 0)
      {
        AcquisitionStateInterface
            lState =
            mAcquisitionStateList.get(mAcquisitionStateList.size() - 1);
        mAcquisitionStateList.remove(mAcquisitionStateList.size() - 1);
        mMicroscope.getAcquisitionStateManager().setCurrentState(lState);
      } else {
        warning("Error: Cannot restore state, list is empty.");
      }
    }
    return true;
  }

  public boolean isBackup()
  {
    return mBackup;
  }
}
