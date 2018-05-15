package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.device.VirtualDevice;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeSpatialPhaseModulatorDevice;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
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

  private SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
  private ZernikeSpatialPhaseModulatorDevice mZernikeSpatialPhaseModulatorDevice;

  public MirrorModeScheduler(ZernikeSpatialPhaseModulatorDevice pZernikeSpatialPhaseModulatorDevice) {
    super("Adaptation: Mirror mode scheduler for " + pZernikeSpatialPhaseModulatorDevice.getName());

    //mSpatialPhaseModulatorDeviceInterface = pSpatialPhaseModulatorDeviceInterface;
    mZernikeSpatialPhaseModulatorDevice = pZernikeSpatialPhaseModulatorDevice;
  }



  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  private int mTimePointCount = 0;

  @Override public boolean initialize()
  {
    mTimePointCount = 0;
    return true;
  }

  @Override public boolean enqueue(long pTimePoint)
  {



    File lFolder = mRootFolderVariable.get();
    if (lFolder == null || !lFolder.isDirectory()) {
      warning("Error: given root folder is no directory");
      return false;
    }
    long lFileIndex = mTimePointCount;

    File lFile = lFolder.listFiles()[(int)lFileIndex];

    //DenseMatrix64F lMatrix = mSpatialPhaseModulatorDeviceInterface.getMatrixReference().get();
        //new DenseMatrix64F(mSpatialPhaseModulatorDeviceInterface.getMatrixHeight(), mSpatialPhaseModulatorDeviceInterface.getMatrixWidth());


    if (mMicroscope instanceof LightSheetMicroscope) {
      ((LightSheetMicroscope) mMicroscope).getTimelapse().log("Loading " + lFile);
    }
    info("Loading " + lFile);
    DenseMatrix64F lMatrix = new DenseMatrix64FReader(lFile).getMatrix();

//    if (!lMatrixReader.read()) {
//      if (mMicroscope instanceof LightSheetMicroscope) {
//        ((LightSheetMicroscope) mMicroscope).getTimelapse().log("Error: matrix file could not be loaded");
//      }
//      warning("Error: matrix file could not be loaded");
//    }
    double[] lArray = TransformMatrices.convertDense64MatrixTo1DDoubleArray(lMatrix);
    info("Sending matrix to mirror");
//    mSpatialPhaseModulatorDeviceInterface.setZernikeFactors(lMatrix);
    mZernikeSpatialPhaseModulatorDevice.setZernikeFactors(lArray);


    info("Sent. Scheduler done");

    mTimePointCount++;
    if(mTimePointCount >= lFolder.listFiles().length)
    {
      mTimePointCount = 0;
    }
    return true;
  }
}
