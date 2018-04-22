package clearcontrol.microscope.lightsheet.simulation;

import java.util.ArrayList;

import clearcl.ClearCLContext;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.devices.sim.StackCameraDeviceSimulator;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationProvider;
import clearcontrol.devices.cameras.devices.sim.providers.FractalStackProvider;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.devices.sim.LaserDeviceSimulator;
import clearcontrol.devices.lasers.schedulers.LaserOnOffScheduler;
import clearcontrol.devices.lasers.schedulers.LaserPowerScheduler;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.sim.FilterWheelDeviceSimulator;
import clearcontrol.devices.signalamp.ScalingAmplifierDeviceInterface;
import clearcontrol.devices.signalamp.devices.sim.ScalingAmplifierSimulator;
import clearcontrol.devices.signalgen.devices.sim.SignalGeneratorSimulatorDevice;
import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.sim.StageDeviceSimulator;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.ControlPlaneFocusFinderAlphaByVariationScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.ControlPlaneFocusFinderZScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderAlphaByVariationScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderZScheduler;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.MeasureTimeScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.PauseScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.implementations.PauseUntilTimeAfterMeasuredTimeScheduler;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedFusionScheduler;
import clearcontrol.microscope.lightsheet.imaging.interleaved.WriteInterleavedRawDataToDiscScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedFusionScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.WriteOpticsPrefusedRawDataToDiscScheduler;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialFusionScheduler;
import clearcontrol.microscope.lightsheet.imaging.sequential.WriteSequentialRawDataToDiscScheduler;
import clearcontrol.microscope.lightsheet.imaging.singleview.SingleViewAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.singleview.WriteSingleLightSheetImageToDiscScheduler;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageToDiscScheduler;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.state.ControlPlaneLayout;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateBackupRestoreScheduler;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateResetScheduler;
import clearcontrol.microscope.lightsheet.state.schedulers.InterpolatedAcquisitionStateLogScheduler;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DropOldestStackInterfaceContainerScheduler;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.sourcesink.sink.RawFileStackSink;

/**
 * Simulated lightsheet microscope
 *
 * @author royer
 */
public class SimulatedLightSheetMicroscope extends
                                           LightSheetMicroscope
{

  /**
   * Instantiates a simulated lightsheet microscope
   * 
   * @param pDeviceName
   *          device name
   * @param pStackFusionContext
   *          ClearCL context for stack fusion
   * @param pMaxStackProcessingQueueLength
   *          max stack processing queue length
   * @param pThreadPoolSize
   *          thread pool size
   */
  public SimulatedLightSheetMicroscope(String pDeviceName,
                                       ClearCLContext pStackFusionContext,
                                       int pMaxStackProcessingQueueLength,
                                       int pThreadPoolSize)
  {
    super(pDeviceName,
          pStackFusionContext,
          pMaxStackProcessingQueueLength,
          pThreadPoolSize);

  }

  /**
   * Assembles the microscope
   * 
   * @param pDummySimulation
   *          true-> uses a dummy simulation instead of the embryo
   * @param pXYZRStage
   *          XYZR Stage
   * @param pSharedLightSheetControl
   *          true -> shared lightsheet control
   * @param pSimulatorDevice
   *          simulator device
   */
  public void addSimulatedDevices(boolean pDummySimulation,
                                  boolean pXYZRStage,
                                  boolean pSharedLightSheetControl,
                                  LightSheetMicroscopeSimulationDevice pSimulatorDevice)
  {

    int lNumberOfDetectionArms =
                               pSimulatorDevice.getSimulator()
                                               .getNumberOfDetectionArms();
    int lNumberOfLightSheets =
                             pSimulatorDevice.getSimulator()
                                             .getNumberOfLightSheets();

    // Setting up lasers:
    {
      int[] lLaserWavelengths = new int[]
      { 488, 594 };
      ArrayList<LaserDeviceInterface> lLaserList = new ArrayList<>();
      for (int l = 0; l < lLaserWavelengths.length; l++)
      {
        LaserDeviceInterface lLaser =
                                    new LaserDeviceSimulator("Laser "
                                                             + lLaserWavelengths[l],
                                                             l,
                                                             lLaserWavelengths[l],
                                                             100);
        lLaserList.add(lLaser);
        addDevice(l, lLaser);

        addDevice(0, new LaserPowerScheduler(lLaser, 0.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 1.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 5.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 10.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 20.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 50.0));
        addDevice(0, new LaserPowerScheduler(lLaser, 100.0));

        addDevice(0, new LaserOnOffScheduler(lLaser, true));
        addDevice(0, new LaserOnOffScheduler(lLaser, false));


      }
    }

    // Setting up Stage:
    if (pXYZRStage)
    {
      StageDeviceSimulator lStageDeviceSimulator =
                                                 new StageDeviceSimulator("Stage",
                                                                          StageType.XYZR,
                                                                          true);
      lStageDeviceSimulator.addXYZRDOFs();
      lStageDeviceSimulator.setSpeed(0.8);

      addDevice(0, lStageDeviceSimulator);
    }

    // Setting up Filterwheel:
    {
      int[] lFilterWheelPositions = new int[]
      { 0, 1, 2, 3 };
      FilterWheelDeviceInterface lFilterWheelDevice =
                                                    new FilterWheelDeviceSimulator("FilterWheel",
                                                                                   lFilterWheelPositions);
      lFilterWheelDevice.setPositionName(0, "405 filter");
      lFilterWheelDevice.setPositionName(1, "488 filter");
      lFilterWheelDevice.setPositionName(2, "561 filter");
      lFilterWheelDevice.setPositionName(3, "594 filter");
      getDeviceLists().addDevice(0, lFilterWheelDevice);
    }

    // Setting up trigger:

    Variable<Boolean> lTrigger =
                               new Variable<Boolean>("CameraTrigger",
                                                     false);

    ArrayList<StackCameraDeviceSimulator> lCameraList =
                                                      new ArrayList<>();

    // Setting up cameras:
    {

      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        final StackCameraDeviceSimulator lCamera =
                                                 new StackCameraDeviceSimulator("StackCamera"
                                                                                + c,
                                                                                lTrigger);

        long lMaxWidth = pSimulatorDevice.getSimulator()
                                         .getCameraRenderer(c)
                                         .getMaxWidth();

        long lMaxHeight = pSimulatorDevice.getSimulator()
                                          .getCameraRenderer(c)
                                          .getMaxHeight();

        lCamera.getMaxWidthVariable().set(lMaxWidth);
        lCamera.getMaxHeightVariable().set(lMaxHeight);
        lCamera.getStackWidthVariable().set(lMaxWidth / 2);
        lCamera.getStackHeightVariable().set(lMaxHeight);
        lCamera.getExposureInSecondsVariable().set(0.010);

        // lCamera.getStackVariable().addSetListener((o,n)->
        // {System.out.println("camera output:"+n);} );

        addDevice(c, lCamera);

        lCameraList.add(lCamera);
      }
    }

    // Scaling Amplifier:
    {
      ScalingAmplifierDeviceInterface lScalingAmplifier1 =
                                                         new ScalingAmplifierSimulator("ScalingAmplifier1");
      addDevice(0, lScalingAmplifier1);

      ScalingAmplifierDeviceInterface lScalingAmplifier2 =
                                                         new ScalingAmplifierSimulator("ScalingAmplifier2");
      addDevice(1, lScalingAmplifier2);
    }

    // Signal generator:

    {
      SignalGeneratorSimulatorDevice lSignalGeneratorSimulatorDevice =
                                                                     new SignalGeneratorSimulatorDevice();

      // addDevice(0, lSignalGeneratorSimulatorDevice);
      lSignalGeneratorSimulatorDevice.getTriggerVariable()
                                     .sendUpdatesTo(lTrigger);/**/

      final LightSheetSignalGeneratorDevice lLightSheetSignalGeneratorDevice =
                                                                             LightSheetSignalGeneratorDevice.wrap(lSignalGeneratorSimulatorDevice,
                                                                                                                  pSharedLightSheetControl);

      addDevice(0, lLightSheetSignalGeneratorDevice);
    }

    // setting up staging score visualization:

    /*final ScoreVisualizerJFrame lVisualizer = ScoreVisualizerJFrame.visualize("LightSheetDemo",
                                                                              lStagingScore);/**/

    // Setting up detection arms:

    {
      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        final DetectionArm lDetectionArm = new DetectionArm("D" + c);
        lDetectionArm.getPixelSizeInMicrometerVariable()
                     .set(pSimulatorDevice.getSimulator()
                                          .getPixelWidth(c));

        addDevice(c, lDetectionArm);
      }
    }

    // Setting up lightsheets:
    {
      for (int l = 0; l < lNumberOfLightSheets; l++)
      {
        final LightSheet lLightSheet =
                                     new LightSheet("I" + l,
                                                    9.4,
                                                    getNumberOfLaserLines());
        addDevice(l, lLightSheet);

      }
    }

    // Setting up lightsheets selector
    {
      LightSheetOpticalSwitch lLightSheetOpticalSwitch =
                                                       new LightSheetOpticalSwitch("OpticalSwitch",
                                                                                   lNumberOfLightSheets);

      addDevice(0, lLightSheetOpticalSwitch);
    }

    // Setting up simulator:
    {
      // Now that the microscope has been setup, we can connect the simulator to
      // it:

      // first, we connect the devices in the simulator so that parameter
      // changes
      // are forwarded:
      pSimulatorDevice.connectTo(this);

      // second, we make sure that the simulator is used as provider for the
      // simulated cameras:
      for (int c = 0; c < lNumberOfDetectionArms; c++)
      {
        StackCameraSimulationProvider lStackProvider;
        if (pDummySimulation)
          lStackProvider = new FractalStackProvider();
        else
          lStackProvider = pSimulatorDevice.getStackProvider(c);
        lCameraList.get(c)
                   .setStackCameraSimulationProvider(lStackProvider);
      }
    }

  }

  /**
   * Adds standard devices such as the acquisition state manager, calibrator and
   * Timelapse
   */
  @SuppressWarnings("unchecked")
  public void addStandardDevices(int pNumberOfControlPlanes)
  {

    // Adding calibrator:
    {
      CalibrationEngine lCalibrator = addCalibrator();
      lCalibrator.load();
    }

    // Setting up acquisition state manager:
    {
      AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>> lAcquisitionStateManager;
      lAcquisitionStateManager =
                               (AcquisitionStateManager<LightSheetAcquisitionStateInterface<?>>) addAcquisitionStateManager();
      InterpolatedAcquisitionState lAcquisitionState =
                                                     new InterpolatedAcquisitionState("default",
                                                                                      this);
      lAcquisitionState.setupControlPlanes(pNumberOfControlPlanes,
                                           ControlPlaneLayout.Circular);
      lAcquisitionState.copyCurrentMicroscopeSettings();
      lAcquisitionStateManager.setCurrentState(lAcquisitionState);
      addInteractiveAcquisition();

      addDevice(0, new AcquisitionStateBackupRestoreScheduler(true));
      addDevice(0, new AcquisitionStateBackupRestoreScheduler(false));

      addDevice(0, new AcquisitionStateResetScheduler());

      addDevice(0, new InterpolatedAcquisitionStateLogScheduler());



      // Adding adaptive engine device:
      {
        AdaptationStateEngine.setup(this, lAcquisitionState);
      }

    }

    // Adding timelapse device:
    TimelapseInterface lTimelapse = addTimelapse();
    lTimelapse.getAdaptiveEngineOnVariable().set(false);

    lTimelapse.addFileStackSinkType(RawFileStackSink.class);
    //lTimelapse.addFileStackSinkType(SqeazyFileStackSink.class);


    if (getNumberOfLightSheets() > 1) {
      addDevice(0, new InterleavedAcquisitionScheduler());
      addDevice(0, new InterleavedFusionScheduler());
      addDevice(0, new WriteInterleavedRawDataToDiscScheduler(getNumberOfDetectionArms()));
      addDevice(0, new WriteFusedImageToDiscScheduler("interleaved"));

      SequentialAcquisitionScheduler
          lSequentialAcquisitionScheduler = new SequentialAcquisitionScheduler();
      SequentialFusionScheduler lSequentialFusionScheduler = new SequentialFusionScheduler();
      WriteFusedImageToDiscScheduler lWriteSequentialFusedImageToDiscScheduler = new WriteFusedImageToDiscScheduler("sequential");
      if (lTimelapse instanceof LightSheetTimelapse)
      {
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lSequentialAcquisitionScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lSequentialFusionScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lWriteSequentialFusedImageToDiscScheduler);
      }
      addDevice(0, lSequentialAcquisitionScheduler);
      addDevice(0, lSequentialFusionScheduler);
      addDevice(0, new WriteSequentialRawDataToDiscScheduler(getNumberOfDetectionArms(), getNumberOfLightSheets()));
      addDevice(0, lWriteSequentialFusedImageToDiscScheduler);

      addDevice(0, new OpticsPrefusedAcquisitionScheduler());
      addDevice(0, new OpticsPrefusedFusionScheduler());
      addDevice(0, new WriteOpticsPrefusedRawDataToDiscScheduler(getNumberOfDetectionArms()));
      addDevice(0, new WriteFusedImageToDiscScheduler("opticsprefused"));
    }


    for (int c = 0; c < getNumberOfDetectionArms(); c++) {
      for (int l = 0; l < getNumberOfLightSheets(); l++) {
        SingleViewAcquisitionScheduler
            lScheduler = new SingleViewAcquisitionScheduler(c, l);
        addDevice(0, lScheduler);
        if (lTimelapse instanceof LightSheetTimelapse && ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().size() == 0)
        {
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lScheduler);
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(new WriteSingleLightSheetImageToDiscScheduler(c, l));
        }
        addDevice(0, new WriteSingleLightSheetImageToDiscScheduler(c, l));

      }
    }

    if (lTimelapse instanceof LightSheetTimelapse)
    {
      ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers()
                                        .add(getDevice(DropOldestStackInterfaceContainerScheduler.class, 0));
      ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers()
                                        .add(getDevice(DropOldestStackInterfaceContainerScheduler.class, 0));
    }


    addDevice(0, new PauseScheduler());

    int[] pauseTimes = {
        1000,  // 1 s
        10000, // 10 s
        30000, // 30 s
        60000, // 1 min
        300000, // 5 min
        600000, // 10 min
        3600000 // 1 h
        };
    String[] timeMeasurementKeys = {"A", "B", "C"};
    for (int i = 0; i < pauseTimes.length; i++)
    {
      addDevice(0, new PauseScheduler(pauseTimes[i]));
    }
    for (int k = 0; k < timeMeasurementKeys.length; k++)
    {
      addDevice(0, new MeasureTimeScheduler(timeMeasurementKeys[k]));
      for (int i = 0; i < pauseTimes.length; i++)
      {
        addDevice(9,
                  new PauseUntilTimeAfterMeasuredTimeScheduler(
                      timeMeasurementKeys[k],
                      pauseTimes[i]));
      }
    }

    int lNumberOfControlPlanes = ((InterpolatedAcquisitionState)(getAcquisitionStateManager().getCurrentState())).getNumberOfControlPlanes();
    for (int cpi = 0; cpi < lNumberOfControlPlanes; cpi++)
    {
      for (int d = 0; d < getNumberOfDetectionArms(); d++)
      {
        for (int l = 0; l < getNumberOfLightSheets(); l++)
        {
          addDevice(0, new FocusFinderZScheduler(
              l,
              d,
              cpi));
          addDevice(0, new FocusFinderAlphaByVariationScheduler(
              l,
              d,
              cpi));
        }
        addDevice(0, new ControlPlaneFocusFinderAlphaByVariationScheduler(d, cpi));
        addDevice(0, new ControlPlaneFocusFinderZScheduler(d, cpi));
      }
    }



  }

}
