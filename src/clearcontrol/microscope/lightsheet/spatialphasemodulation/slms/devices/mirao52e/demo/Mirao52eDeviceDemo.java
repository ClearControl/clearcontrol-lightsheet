package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.mirao52e.demo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.demo.DeformableMirrorDeviceDemoHelper;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.mirao52e.Mirao52EDevice;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;

import org.ejml.data.DenseMatrix64F;
import org.junit.Test;

public class Mirao52eDeviceDemo
{

  @Test
  public void demoZernicke() throws IOException, InterruptedException
  {
    final Mirao52EDevice lMirao52eDevice = new Mirao52EDevice(1);
    final DenseMatrix64F lZernickeTransformMatrix =
                                                  TransformMatrices.computeZernickeTransformMatrix(lMirao52eDevice.getMatrixWidth());

    assertTrue(lMirao52eDevice.open());

    DeformableMirrorDeviceDemoHelper.sweepModes(lMirao52eDevice,
                                                lZernickeTransformMatrix);

    assertTrue(lMirao52eDevice.close());
  }

  @Test
  public void demoCosine() throws IOException, InterruptedException
  {
    final Mirao52EDevice lMirao52eDevice = new Mirao52EDevice(1);
    final DenseMatrix64F lCosineTransformMatrix =
                                                TransformMatrices.computeCosineTransformMatrix(lMirao52eDevice.getMatrixWidth());

    assertTrue(lMirao52eDevice.open());

    DeformableMirrorDeviceDemoHelper.sweepModes(lMirao52eDevice,
                                                lCosineTransformMatrix);

    assertTrue(lMirao52eDevice.close());
  }

  @Test
  public void demoRandom() throws IOException, InterruptedException
  {
    final Mirao52EDevice lMirao52eDevice = new Mirao52EDevice(1);
    final DenseMatrix64F lCosineTransformMatrix =
                                                TransformMatrices.computeCosineTransformMatrix(lMirao52eDevice.getMatrixWidth());

    assertTrue(lMirao52eDevice.open());

    DeformableMirrorDeviceDemoHelper.playRandomShapes(lMirao52eDevice,
                                                      lCosineTransformMatrix,
                                                      10000);

    assertTrue(lMirao52eDevice.close());
  }

}
