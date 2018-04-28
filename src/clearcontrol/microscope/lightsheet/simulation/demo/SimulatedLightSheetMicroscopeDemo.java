package clearcontrol.microscope.lightsheet.simulation.demo;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.schedulers.LaserOnOffScheduler;
import clearcontrol.devices.lasers.schedulers.LaserPowerScheduler;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler.GeneticAlgorithmMirrorModeOptimizeScheduler;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.LogMirrorModeToFileScheduler;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim.SpatialPhaseModulatorDeviceSimulator;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import javafx.application.Application;
import javafx.stage.Stage;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.ClearCLBackends;
import clearcontrol.core.concurrent.executors.AsynchronousExecutorFeature;
import clearcontrol.core.concurrent.thread.ThreadSleep;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.microscope.lightsheet.gui.LightSheetMicroscopeGUI;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.simulation.SimulationUtils;

/**
 * Simulated lightsheet microscope demo
 *
 * @author royer
 */
public class SimulatedLightSheetMicroscopeDemo extends Application
                                               implements
                                               AsynchronousExecutorFeature
{

  @Override
  public void start(Stage pPrimaryStage)
  {
    pPrimaryStage.show();

    Runnable lRunnable = () -> {

      boolean lDummySimulation = false;
      boolean lUniformFluorescence = false;

      boolean l2DDisplayFlag = true;
      boolean l3DDisplayFlag = true;

      int lMaxNumberOfStacks = 32;

      int lMaxCameraResolution = 1024;

      int lNumberOfLightSheets = 4;
      int lNumberOfDetectionArms = 2;

      float lDivisionTime = 11f;

      int lPhantomWidth = 320;
      int lPhantomHeight = lPhantomWidth;
      int lPhantomDepth = lPhantomWidth;

      int lNumberOfControlPlanes = 7;

      ClearCL lClearCL =
                       new ClearCL(ClearCLBackends.getBestBackend());

      for (ClearCLDevice lClearCLDevice : lClearCL.getAllDevices())
        System.out.println(lClearCLDevice.getName());

      MachineConfiguration lMachineConfiguration =
                                                 MachineConfiguration.get();

      ClearCLContext lSimulationContext =
                                        getClearCLDeviceByName(lClearCL,
                                                               lMachineConfiguration.getStringProperty("clearcl.device.simulation",
                                                                                                       "HD"));

      ClearCLContext lMicroscopeContext =
                                        getClearCLDeviceByName(lClearCL,
                                                               lMachineConfiguration.getStringProperty("clearcl.device.fusion",
                                                                                                       "HD"));

      LightSheetMicroscopeSimulationDevice lSimulatorDevice =
                                                            SimulationUtils.getSimulatorDevice(lSimulationContext,
                                                                                               lNumberOfDetectionArms,
                                                                                               lNumberOfLightSheets,
                                                                                               lMaxCameraResolution,
                                                                                               lDivisionTime,
                                                                                               lPhantomWidth,
                                                                                               lPhantomHeight,
                                                                                               lPhantomDepth,
                                                                                               lUniformFluorescence);

      SimulatedLightSheetMicroscope lMicroscope =
                                                new SimulatedLightSheetMicroscope("SimulatedLightSheetMicroscope",
                                                                                  lMicroscopeContext,
                                                                                  lMaxNumberOfStacks,
                                                                                  1);

      lMicroscope.addSimulatedDevices(lDummySimulation,
                                      true,
                                      true,
                                      lSimulatorDevice);

      lMicroscope.addStandardDevices(lNumberOfControlPlanes);


      // Setting up deformable mirror
      /*{
        SpatialPhaseModulatorDeviceSimulator lMirror = new SpatialPhaseModulatorDeviceSimulator("SimDM", 11, 1);
        lMicroscope.addDevice(0, lMirror);

        GeneticAlgorithmMirrorModeOptimizeScheduler lMirrorOptimizer = new GeneticAlgorithmMirrorModeOptimizeScheduler(lMirror);
        lMicroscope.addDevice(0, lMirrorOptimizer);

        LogMirrorModeToFileScheduler lMirrorModeSaver = new LogMirrorModeToFileScheduler(lMirror);
        lMicroscope.addDevice(0, lMirrorModeSaver);
      }*/


      LightSheetTimelapse lLightSheetTimelapse = lMicroscope.getDevice(LightSheetTimelapse.class, 0);
      LaserDeviceInterface lLaser = lMicroscope.getDevice(
          LaserDeviceInterface.class, 0);
      lLightSheetTimelapse.getListOfActivatedSchedulers().add(0, new LaserOnOffScheduler(lLaser, true));
      lLightSheetTimelapse.getListOfActivatedSchedulers().add(0, new LaserPowerScheduler(lLaser, 10));

      InterpolatedAcquisitionState lState =
          (InterpolatedAcquisitionState) lMicroscope.getAcquisitionStateManager().getCurrentState();
      lState.getStackZLowVariable().set(0);
      lState.getStackZHighVariable().set(20);



      if (lMicroscope.open())
        if (lMicroscope.start())
        {

          LightSheetMicroscopeGUI lMicroscopeGUI =
                                                 new LightSheetMicroscopeGUI(lMicroscope,
                                                                             pPrimaryStage,
                                                                             l2DDisplayFlag,
                                                                             l3DDisplayFlag);

          lMicroscopeGUI.setup();

          assertTrue(lMicroscopeGUI.open());

          ThreadSleep.sleep(1000, TimeUnit.MILLISECONDS);
          lMicroscopeGUI.waitForVisible(true, 1L, TimeUnit.MINUTES);

          lMicroscopeGUI.connectGUI();

          lMicroscopeGUI.waitForVisible(false, null, null);

          lMicroscopeGUI.disconnectGUI();
          lMicroscopeGUI.close();

          lMicroscope.stop();
          lMicroscope.close();
        }

      try
      {
        lSimulatorDevice.getSimulator().close();

        lClearCL.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

    };

    executeAsynchronously(lRunnable);

  }

  protected ClearCLContext getClearCLDeviceByName(ClearCL pClearCL,
                                                  String lDeviceName)
  {
    ClearCLDevice lSimulationGPUDevice =
                                       pClearCL.getFastestGPUDeviceForImages(); // (lDeviceName);
    ClearCLContext lSimulationContext =
                                      lSimulationGPUDevice.createContext();
    return lSimulationContext;
  }

  /**
   * Main
   * 
   * @param args
   *          NA
   */
  public static void main(String[] args)
  {
    launch(args);
  }
}
