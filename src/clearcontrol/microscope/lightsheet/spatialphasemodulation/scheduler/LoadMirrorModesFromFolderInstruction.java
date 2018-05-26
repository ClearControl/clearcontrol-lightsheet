package clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class LoadMirrorModesFromFolderInstruction extends LightSheetMicroscopeInstruction implements
                                                                            LoggingFeature
{
  private Variable<File> mRootFolderVariable =
      new Variable("RootFolder",
                   (Object) null);

  private ZernikeModeFactorBasedSpatialPhaseModulatorBase mZernikeModeFactorBasedSpatialPhaseModulatorBase;

  public LoadMirrorModesFromFolderInstruction(ZernikeModeFactorBasedSpatialPhaseModulatorBase pZernikeModeFactorBasedSpatialPhaseModulatorBase, LightSheetMicroscope pLightSheetMicroscope) {
    super("Adaptive optics: Load mirror modes sequentially from folder and send to " + pZernikeModeFactorBasedSpatialPhaseModulatorBase.getName(), pLightSheetMicroscope);

    mZernikeModeFactorBasedSpatialPhaseModulatorBase = pZernikeModeFactorBasedSpatialPhaseModulatorBase;
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

    getLightSheetMicroscope().getTimelapse().log("Loading " + lFile);

    info("Loading " + lFile);
    DenseMatrix64F lMatrix = new DenseMatrix64FReader(lFile).getMatrix();

    double[] lArray = TransformMatrices.convertDense64MatrixTo1DDoubleArray(lMatrix);
    info("Sending matrix to mirror");

    mZernikeModeFactorBasedSpatialPhaseModulatorBase.setZernikeFactors(lArray);

    info("Sent. Scheduler done");

    mTimePointCount++;
    if(mTimePointCount >= lFolder.listFiles().length)
    {
      mTimePointCount = 0;
    }
    return true;
  }
}
