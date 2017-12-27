package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms;

/**
 * Todo: remove class / abstraction layer? It's apparently unused
 */
@Deprecated
public abstract class AbstractDeformableMirrorDevice extends
                                                     SpatialPhaseModulatorDeviceBase
{

  public AbstractDeformableMirrorDevice(final String pDeviceName,
                                        int pFullMatrixWidthHeight,
                                        int pActuatorResolution)
  {
    super(pDeviceName, pFullMatrixWidthHeight, pActuatorResolution);
  }

}
