package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class MirrorModeScheduler extends SchedulerBase implements
                                                                            LoggingFeature
{
  private Variable<File> mRootFolderVariable =
      new Variable("RootFolder",
                   (Object) null);

  private BoundedVariable<Integer>
      mDelayFramesVariable = new BoundedVariable<Integer>("Run every ... time points", 2, 0, Integer.MAX_VALUE, 1);

  private SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;

  public MirrorModeScheduler(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {
    super("Mirror mode scheduler for " + pSpatialPhaseModulatorDeviceInterface.getName());

    mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
  }



  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  public BoundedVariable<Integer> getDelayFramesVariable()
  {
    return mDelayFramesVariable;
  }

  private int mTimePointCount = -1;

  @Override public boolean initialize()
  {
    mTimePointCount = -1;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {
    mTimePointCount ++;
    if (mTimePointCount % mDelayFramesVariable.get() > 0) {
      info("Skipping time point " + pTimePoint);
      return false;
    }


    File lFolder = mRootFolderVariable.get();
    if (lFolder == null || !lFolder.isDirectory()) {
      warning("Error: given root folder is no directory");
      return false;
    }
    long lFileIndex = (pTimePoint / mDelayFramesVariable.get()) % lFolder.listFiles().length;

    File lFile = lFolder.listFiles()[(int)lFileIndex];

    DenseMatrix64F lMatrix = mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get();
        //new DenseMatrix64F(mSpatialPhaseModulatorDeviceInterface.getMatrixHeight(), mSpatialPhaseModulatorDeviceInterface.getMatrixWidth());

    info("Loading " + lFile);

    DenseMatrix64FReader lMatrixReader = new DenseMatrix64FReader(lFile, lMatrix);
    if (!lMatrixReader.read()) {
      warning("Error: matrix file could not be loaded");
    }

    info("Sending matrix to mirror");
    mSpatialPhaseModulatorDeviceInterface.getMatrixReference().set(lMatrix);

    info("Sent. Scheduler done");
    return true;
  }
}
