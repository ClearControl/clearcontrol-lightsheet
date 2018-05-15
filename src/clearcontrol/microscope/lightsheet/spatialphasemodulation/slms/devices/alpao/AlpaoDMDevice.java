package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.alpao;

import asdk.AlpaoDeformableMirror;
import asdk.TriggerMode;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceBase;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.ZernikeModeFactorBasedSpatialPhaseModulatorBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Matrix64F;

public class AlpaoDMDevice extends ZernikeModeFactorBasedSpatialPhaseModulatorBase
                           implements LoggingFeature
{
  private static final int cFullMatrixWidthHeight = 11;
  private static final int cActuatorResolution = 2 << 14;
  private static final int cNumberOfZernikeModeFactors = 66;

  private AlpaoDeformableMirror mAlpaoDeformableMirror;

  public AlpaoDMDevice(int pAlpaoDeviceIndex)
  {
    this(MachineConfiguration.get()
                             .getStringProperty("device.ao.dm.alpao."
                                                + pAlpaoDeviceIndex,
                                                "NULL"));

  }

  public AlpaoDMDevice(String pAlpaoSerialName)
  {
    super("ALPAO_" + pAlpaoSerialName,
          cFullMatrixWidthHeight,
          cActuatorResolution,
            cNumberOfZernikeModeFactors);

    mAlpaoDeformableMirror =
                           new AlpaoDeformableMirror(pAlpaoSerialName);
    mAlpaoDeformableMirror.setDebugPrintout(true);






    DenseMatrix64F lMatrix = null;
    if (mMatrixVariable != null)
    {
      lMatrix = mMatrixVariable.get();
    }
    mMatrixVariable = new Variable<DenseMatrix64F>("MatrixReference",
                                                   lMatrix)
    // mMatrixVariable = new Variable<DenseMatrix64F>("MatrixReference")
    {
      @Override
      public DenseMatrix64F setEventHook(final DenseMatrix64F pOldValue,
                                         final DenseMatrix64F pNewValue)
      {

        info("Setting the dm device");
        mAlpaoDeformableMirror.sendFullMatrixMirrorShapeVector(pNewValue.data);

        //info("Setting trigger disabled");
        //mAlpaoDeformableMirror.setInputTriggerMode(TriggerMode.Disabled);

        return super.setEventHook(pOldValue, pNewValue);
      }

    };

  }

  @Override
  public boolean open()
  {
    try
    {
      final boolean lOpen = mAlpaoDeformableMirror.open();
      mNumberOfActuatorsVariable =
                                 new Variable<Double>("NumberOfActuators",
                                                      (double) mAlpaoDeformableMirror.getNumberOfActuators());

      mAlpaoDeformableMirror.setInputTriggerMode(TriggerMode.Disabled);

      info("Setting ALPAO log level");
      mAlpaoDeformableMirror.setLogPrintLevel(4);

      info("Resetting ALPAO DM DACs");
      mAlpaoDeformableMirror.resetDAC();


      return lOpen;
    }
    catch (final Throwable e)
    {
      final String lErrorString =
                                "Could not open connection to ALPAO DM - "
                                  + e.getLocalizedMessage();
      severe("AO", lErrorString);
      return false;
    }

  }

  @Override
  public boolean start()
  {
    zero();
    return true;
  }

  @Override
  public void zero()
  {
    info("calling zero!");
    mAlpaoDeformableMirror.sendFlatMirrorShapeVector();
  }

  @Override
  public boolean stop()
  {
    return true;
  }

  @Override
  public boolean close()
  {
    try
    {
      mAlpaoDeformableMirror.close();
      return true;
    }
    catch (final Throwable e)
    {
      final String lErrorString =
                                "Could not close connection to ALPAO DM - "
                                  + e.getLocalizedMessage();
      severe("AO", lErrorString);
      return false;
    }
  }

  @Override
  public long getRelaxationTimeInMilliseconds()
  {
    return 1;
  }

  @Override
  public boolean setZernikeFactors(double[] pZernikeFactors) {
    pZernikeFactors = convertNollOrderToANSIOrder(pZernikeFactors);
    DenseMatrix64F lActuatorPositions = getActuatorPositions(pZernikeFactors);
    mAlpaoDeformableMirror.sendRawMirrorShapeVector(TransformMatrices.convertDense64MatrixTo1DDoubleArray(lActuatorPositions));
    info("Sending to Mirror:" + TransformMatrices.convertDense64MatrixTo1DDoubleArray(lActuatorPositions).toString());
    return true;
  }

  private double[] convertNollOrderToANSIOrder(double[] pNollOrderedZernikeFactors) {
    double[] pANSIOrderedZernikeFactors = new double[pNollOrderedZernikeFactors.length];
    for (int i = 0; i < pNollOrderedZernikeFactors.length; i++) {
      pANSIOrderedZernikeFactors[i] = pNollOrderedZernikeFactors[ZernikePolynomials.jNoll(i)];
    }
    return pANSIOrderedZernikeFactors;
  }

  public boolean setActuatorPositions(double[] pActuatorsPositions){
    double[] lActuatorPositions = pActuatorsPositions;
    mAlpaoDeformableMirror.sendRawMirrorShapeVector(lActuatorPositions);
    return true;
  }

}
