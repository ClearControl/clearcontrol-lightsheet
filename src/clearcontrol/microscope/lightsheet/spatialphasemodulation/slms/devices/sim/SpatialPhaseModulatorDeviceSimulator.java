package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim;

import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.device.sim.SimulationDeviceInterface;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.io.DenseMatrix64FReader;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceBase;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import org.ejml.data.DenseMatrix64F;

import java.io.File;

public class SpatialPhaseModulatorDeviceSimulator extends
                                                  ZernikeModeFactorBasedSpatialPhaseModulatorBase
                                                  implements
                                                  LoggingFeature,
                                                  SimulationDeviceInterface
{

  File lMirrorModeDirectory =
          MachineConfiguration.get()
                  .getFolder("MirrorModes");
  DenseMatrix64F mFlatMatrix;
  DenseMatrix64F mInfluenceMatrix;


  public SpatialPhaseModulatorDeviceSimulator(String pDeviceName,
                                              int pFullMatrixWidthHeight,
                                              int pActuatorResolution,
                                              int pNumberOfZernikeFactors)
  {
    super(pDeviceName, pFullMatrixWidthHeight, pActuatorResolution, pNumberOfZernikeFactors);
    DenseMatrix64F lMatrix = null;
    if (mMatrixVariable != null)
    {
      lMatrix = mMatrixVariable.get();
    }
    mMatrixVariable = new Variable<DenseMatrix64F>("MatrixReference",
                                                   lMatrix)
    {
      @Override
      public DenseMatrix64F setEventHook(final DenseMatrix64F pOldValue,
                                         final DenseMatrix64F pNewValue)
      {
        if (isSimLogging())
          info("Device: %s received new data: %s",
               getName(),
               pNewValue);

        return super.setEventHook(pOldValue, pNewValue);
      }

    };
    System.out.println("HELLOO");
    mFlatMatrix = new DenseMatrix64FReader(new File(lMirrorModeDirectory, getName() + "_flat.json")).getMatrix();
    System.out.println("BYE");
    mInfluenceMatrix = new DenseMatrix64FReader(new File(lMirrorModeDirectory, getName() + "_influence.json")).getMatrix();
    System.out.println("BYE BYE");
  }

  @Override
  public void zero()
  {

  }

  @Override
  public void setMode(int pU, int pV, double pValue)
  {

  }

  @Override
  public long getRelaxationTimeInMilliseconds()
  {
    return 1;
  }


  @Override
  public boolean setZernikeFactors(double[] pZernikeFactors) {
    info("Sending factors to simulated mirror: " + pZernikeFactors);
    setZernikeFactorsInternal(pZernikeFactors);

    DenseMatrix64F lActuators = getActuatorPositions(pZernikeFactors);
    System.out.println("ACTUATOR POSTIONS SENT TO MIRROR: "+lActuators);
    return true;
  }

  @Override
  public boolean start()
  {
    return true;
  }

  @Override
  public boolean stop()
  {
    return true;
  }

  public DenseMatrix64F getActuatorPositions(double[] pZernikeFactors){
    DenseMatrix64F lZernikeFactorsMatrix = new DenseMatrix64F(66,1);
    DenseMatrix64F lZernikeFactors = TransformMatrices.convert1DDoubleArrayToDense64RowMatrix(pZernikeFactors);


    if(lZernikeFactors.numRows == lZernikeFactors.numRows){
      lZernikeFactorsMatrix = lZernikeFactors;
    }
    else{
      for( int y=0; y<lZernikeFactorsMatrix.numRows;y++){
        if(y<lZernikeFactors.numRows){
          lZernikeFactorsMatrix.set(y,0, lZernikeFactors.get(y,0));
        }
        else{
          lZernikeFactorsMatrix.set(y,0, 0);
        }
      }
    }


    DenseMatrix64F lActuators = TransformMatrices.multiplyMatrix(mInfluenceMatrix,lZernikeFactorsMatrix);
    System.out.println(lActuators);
    //DenseMatrix64F lActuators = TransformMatrices.sum(TransformMatrices.multiplyMatrix(mInfluenceMatrix,lZernikeFactorsMatrix),TransformMatrices.transposeMatrix(mFlatMatrix)) ;

    return lActuators;
  }

}
