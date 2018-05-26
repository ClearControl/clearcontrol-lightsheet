package clearcontrol.microscope.lightsheet.simulation;

import java.util.ArrayList;

import clearcl.ClearCLContext;
import clearcontrol.core.variable.Variable;
import clearcontrol.devices.cameras.devices.sim.StackCameraDeviceSimulator;
import clearcontrol.devices.cameras.devices.sim.StackCameraSimulationProvider;
import clearcontrol.devices.cameras.devices.sim.providers.FractalStackProvider;
import clearcontrol.devices.filterwheel.schedulers.FilterWheelInstruction;
import clearcontrol.devices.lasers.LaserDeviceInterface;
import clearcontrol.devices.lasers.devices.sim.LaserDeviceSimulator;
import clearcontrol.devices.lasers.schedulers.LaserOnOffInstruction;
import clearcontrol.devices.lasers.schedulers.LaserPowerInstruction;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.devices.sim.FilterWheelDeviceSimulator;
import clearcontrol.devices.signalamp.ScalingAmplifierDeviceInterface;
import clearcontrol.devices.signalamp.devices.sim.ScalingAmplifierSimulator;
import clearcontrol.devices.signalgen.devices.sim.SignalGeneratorSimulatorDevice;
import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.devices.stages.StageType;
import clearcontrol.devices.stages.devices.sim.StageDeviceSimulator;
import clearcontrol.devices.stages.kcube.scheduler.BasicThreeAxesStageInstruction;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelInstruction;
import clearcontrol.devices.stages.kcube.sim.SimulatedBasicStageDevice;
import clearcontrol.devices.stages.kcube.sim.SimulatedThreeAxesStageDevice;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.instructions.*;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheet;
import clearcontrol.microscope.lightsheet.component.lightsheet.schedulers.ChangeLightSheetWidthInstruction;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.instructions.implementations.MeasureTimeInstruction;
import clearcontrol.instructions.implementations.PauseInstruction;
import clearcontrol.instructions.implementations.PauseUntilTimeAfterMeasuredTimeInstruction;
import clearcontrol.microscope.lightsheet.imaging.exposuremodulation.ExposureModulatedAcquisitionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.*;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.*;
import clearcontrol.microscope.lightsheet.imaging.sequential.*;
import clearcontrol.microscope.lightsheet.imaging.singleview.*;
import clearcontrol.microscope.lightsheet.imaging.singleview.AppendConsecutiveSingleViewImagingInstruction;
import clearcontrol.microscope.lightsheet.imaging.singleview.ViewSingleLightSheetStackInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.MeasureDCTS2DOnStackInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.SpotShiftDeterminationInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.HalfStackMaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.schedulers.MaxProjectionInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.fusion.ViewFusedStackInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsRawToDiscInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.WriteFusedImageAsTifToDiscInstruction;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.smart.samplesearch.SampleSearch1DInstructionBase;
import clearcontrol.microscope.lightsheet.smart.samplesearch.SampleSearch2DInstructionBase;
import clearcontrol.microscope.lightsheet.smart.sampleselection.DrosophilaSelectSampleJustBeforeInvaginationInstruction;
import clearcontrol.microscope.lightsheet.smart.sampleselection.SelectBestQualitySampleInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.instructions.GeneticAlgorithmMirrorModeOptimizeInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.gradientbased.GradientBasedZernikeModeOptimizerInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.LoadMirrorModesFromFolderInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.LogMirrorZernikeFactorsToFileInstruction;
import clearcontrol.microscope.lightsheet.smart.sampleselection.RestartTimelapseWhileNoSampleChosenInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.RandomZernikesInstruction;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.SequentialZernikesInstruction;
import clearcontrol.microscope.lightsheet.state.spatial.FOVBoundingBox;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.sim.SpatialPhaseModulatorDeviceSimulator;
import clearcontrol.microscope.lightsheet.state.ControlPlaneLayout;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.LightSheetAcquisitionStateInterface;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateBackupRestoreInstruction;
import clearcontrol.microscope.lightsheet.state.schedulers.AcquisitionStateResetInstruction;
import clearcontrol.microscope.lightsheet.state.schedulers.InterpolatedAcquisitionStateLogInstruction;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.schedulers.TimelapseStopInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.ReadStackInterfaceContainerFromDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseResetInstruction;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropOldestStackInterfaceContainerInstruction;
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

        addDevice(0, new LaserPowerInstruction(lLaser, 0.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 1.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 5.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 10.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 20.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 50.0));
        addDevice(0, new LaserPowerInstruction(lLaser, 100.0));

        addDevice(0, new LaserOnOffInstruction(lLaser, true));
        addDevice(0, new LaserOnOffInstruction(lLaser, false));


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

    {
      //KCubeDeviceFactory lKCubeDeviceFactory = KCubeDeviceFactory.getInstance();
      //addDevice(0, lKCubeDeviceFactory);
      //addDevice(0, lKCubeDeviceFactory.createKCubeDevice(26000318, "I3B")); // XWing LS3 beta angle

      BasicThreeAxesStageInterface lBasicThreeAxesStageInterface = new SimulatedThreeAxesStageDevice();

      addDevice(0, lBasicThreeAxesStageInterface);

      BasicThreeAxesStageInstruction lBasicThreeAxesStageScheduler = new BasicThreeAxesStageInstruction(lBasicThreeAxesStageInterface);
      addDevice(0, lBasicThreeAxesStageScheduler);

      addDevice(0, new SimulatedBasicStageDevice("X"));
      addDevice(0, new SimulatedBasicStageDevice("Y"));
      addDevice(0, new SimulatedBasicStageDevice("Z"));

      addDevice(0, new CenterSampleInXYInstruction(this));
      addDevice(0, new CenterSampleInZInstruction(this));
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


      for(int f:lFilterWheelDevice.getValidPositions()) {
        addDevice(0, new FilterWheelInstruction(lFilterWheelDevice, f));
      }

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

    // Setting up deformable mirror
    {
      SpatialPhaseModulatorDeviceSimulator lMirror = new SpatialPhaseModulatorDeviceSimulator("SimDM", 11, 1, 66);
      addDevice(0, lMirror);

      GeneticAlgorithmMirrorModeOptimizeInstruction lMirrorOptimizer = new GeneticAlgorithmMirrorModeOptimizeInstruction(lMirror, this);
      addDevice(0, lMirrorOptimizer);

      addDevice(0, new GradientBasedZernikeModeOptimizerInstruction(this, lMirror, 3));
      addDevice(0, new GradientBasedZernikeModeOptimizerInstruction(this, lMirror, 4));
      addDevice(0, new GradientBasedZernikeModeOptimizerInstruction(this, lMirror, 5));

      LogMirrorZernikeFactorsToFileInstruction lMirrorModeZernikeFactorsSaver = new LogMirrorZernikeFactorsToFileInstruction(lMirror, this);
      addDevice(0, lMirrorModeZernikeFactorsSaver);

      addDevice(0, new LoadMirrorModesFromFolderInstruction(lMirror, this));

      SequentialZernikesInstruction lSequentialZernikesScheduler =
              new SequentialZernikesInstruction(lMirror,1,0.0,5.0,-5.0);
      addDevice(0, lSequentialZernikesScheduler);

      addDevice(0, new RandomZernikesInstruction(lMirror));
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

      addDevice(0, new AcquisitionStateBackupRestoreInstruction(true, this));
      addDevice(0, new AcquisitionStateBackupRestoreInstruction(false, this));

      addDevice(0, new AcquisitionStateResetInstruction(this));

      addDevice(0, new InterpolatedAcquisitionStateLogInstruction(this));



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

    if (lTimelapse instanceof LightSheetTimelapse) {
      ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(getDevice(DataWarehouseResetInstruction.class, 0));
    }


    if (getNumberOfLightSheets() > 1) {
      addDevice(0, new InterleavedAcquisitionInstruction(this));
      addDevice(0, new InterleavedFusionInstruction(this));
      addDevice(0, new WriteInterleavedRawDataToDiscInstruction(this));
      addDevice(0, new WriteFusedImageAsRawToDiscInstruction("interleaved", this));
      addDevice(0, new WriteFusedImageAsTifToDiscInstruction("interleaved", this));
      addDevice(0, new DropOldestStackInterfaceContainerInstruction(InterleavedImageDataContainer.class, getDataWarehouse()));
      addDevice(0, new MaxProjectionInstruction<InterleavedImageDataContainer>(InterleavedImageDataContainer.class, this));


      SequentialAcquisitionInstruction
          lSequentialAcquisitionScheduler = new SequentialAcquisitionInstruction(this);
      SequentialFusionInstruction lSequentialFusionScheduler = new SequentialFusionInstruction(this);
      WriteFusedImageAsRawToDiscInstruction lWriteSequentialFusedImageToDiscScheduler = new WriteFusedImageAsRawToDiscInstruction("sequential", this);
      DropOldestStackInterfaceContainerInstruction lDropContainerScheduler = new DropOldestStackInterfaceContainerInstruction(SequentialImageDataContainer.class, getDataWarehouse());
      DropOldestStackInterfaceContainerInstruction lDropFusedContainerScheduler = new DropOldestStackInterfaceContainerInstruction(FusedImageDataContainer.class, getDataWarehouse());

      MaxProjectionInstruction<FusedImageDataContainer> lFusedMaxProjectionScheduler =  new MaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class, this);
      ViewFusedStackInstruction lViewFusedStackScheduler = new ViewFusedStackInstruction(this);

      if (lTimelapse instanceof LightSheetTimelapse)
      {
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lSequentialAcquisitionScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lSequentialFusionScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lViewFusedStackScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lWriteSequentialFusedImageToDiscScheduler);
        ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lFusedMaxProjectionScheduler);
      }
      addDevice(0, lSequentialAcquisitionScheduler);
      addDevice(0, lSequentialFusionScheduler);
      addDevice(0, new WriteSequentialRawDataToDiscInstruction(this));
      addDevice(0, lWriteSequentialFusedImageToDiscScheduler);
      addDevice(0, new WriteFusedImageAsTifToDiscInstruction("sequential", this));
      addDevice(0, lDropContainerScheduler);
      addDevice(0, new MaxProjectionInstruction<SequentialImageDataContainer>(SequentialImageDataContainer.class, this));

      addDevice(0, new OpticsPrefusedAcquisitionInstruction(this));
      addDevice(0, new OpticsPrefusedFusionInstruction(this));
      addDevice(0, new WriteOpticsPrefusedRawDataAsRawToDiscInstruction(this));
      addDevice(0, new WriteFusedImageAsRawToDiscInstruction("opticsprefused", this));
      addDevice(0, new WriteFusedImageAsTifToDiscInstruction("opticsprefused", this));
      addDevice(0, new DropOldestStackInterfaceContainerInstruction(OpticsPrefusedImageDataContainer.class, getDataWarehouse()));
      addDevice(0, new MaxProjectionInstruction<OpticsPrefusedImageDataContainer>(OpticsPrefusedImageDataContainer.class, this));

      addDevice(0, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,true, this));
      addDevice(0, new HalfStackMaxProjectionInstruction<FusedImageDataContainer>(FusedImageDataContainer.class,false, this));



      addDevice(0, lDropFusedContainerScheduler);
      addDevice(0, lViewFusedStackScheduler);
      addDevice(0, lFusedMaxProjectionScheduler);
    }


    MaxProjectionInstruction<StackInterfaceContainer> lStackMaxProjectionScheduler = new MaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class, this);

    String[] lOpticPrefusedStackKeys = new String[getNumberOfDetectionArms()];
    String[] lInterleavedStackKeys = new String[getNumberOfDetectionArms()];
    String[] lSequentialStackKeys = new String[getNumberOfDetectionArms() * getNumberOfLightSheets()];

    for (int c = 0; c < getNumberOfDetectionArms(); c++) {
      for (int l = 0; l < getNumberOfLightSheets(); l++) {
        SingleViewAcquisitionInstruction
            lSingleViewAcquisitionScheduler = new SingleViewAcquisitionInstruction(c, l, this);
        addDevice(0, lSingleViewAcquisitionScheduler);

        ViewSingleLightSheetStackInstruction lViewSingleLightSheetStackScheduler = new ViewSingleLightSheetStackInstruction(c, l, this);
        WriteSingleLightSheetImageAsRawToDiscInstruction lWriteSingleLightSheetImageToDiscScheduler = new WriteSingleLightSheetImageAsRawToDiscInstruction(c, l, this);


        if (lTimelapse instanceof LightSheetTimelapse && ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().size() == 0)
        {
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lSingleViewAcquisitionScheduler);
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lViewSingleLightSheetStackScheduler);
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lWriteSingleLightSheetImageToDiscScheduler);
          ((LightSheetTimelapse) lTimelapse).getListOfActivatedSchedulers().add(lStackMaxProjectionScheduler);
        }

        addDevice(0, lViewSingleLightSheetStackScheduler);
        addDevice(0, lWriteSingleLightSheetImageToDiscScheduler);

        addDevice(0, new ExposureModulatedAcquisitionInstruction(c, l, this));
        lSequentialStackKeys[c * getNumberOfLightSheets() + l] = "C" + c + "L" + l;
        addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(new String[]{"C" + c + "L" + l}, this));
      }
      lOpticPrefusedStackKeys[c] = "C" + c + "opticsprefused";
      lInterleavedStackKeys[c] = "C" + c + "interleaved";
    }

    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(new String[]{"default"}, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(new String[]{"sequential"}, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(new String[]{"interleaved"}, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(new String[]{"opticsprefused"}, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(lOpticPrefusedStackKeys, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(lSequentialStackKeys, this));
    addDevice(0, new ReadStackInterfaceContainerFromDiscInstruction(lInterleavedStackKeys, this));

    addDevice(0, lStackMaxProjectionScheduler);
    addDevice(0, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,true, this));
    addDevice(0, new HalfStackMaxProjectionInstruction<StackInterfaceContainer>(StackInterfaceContainer.class,false, this));


    addDevice(0, new CountsSpotsInstruction<FusedImageDataContainer>(FusedImageDataContainer.class, this));
    addDevice(0, new CountsSpotsInstruction<StackInterfaceContainer>(StackInterfaceContainer.class, this));

    addDevice(0, new MeasureDCTS2DOnStackInstruction<FusedImageDataContainer>(FusedImageDataContainer.class, this));
    addDevice(0, new MeasureDCTS2DOnStackInstruction<StackInterfaceContainer>(StackInterfaceContainer.class, this));

    addDevice(0, new SpotShiftDeterminationInstruction(this));

    addDevice(0, new PauseInstruction());

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
      addDevice(0, new PauseInstruction(pauseTimes[i]));
    }
    for (int k = 0; k < timeMeasurementKeys.length; k++)
    {
      addDevice(0, new MeasureTimeInstruction(timeMeasurementKeys[k]));
      for (int i = 0; i < pauseTimes.length; i++)
      {
        addDevice(9,
                  new PauseUntilTimeAfterMeasuredTimeInstruction(
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
          addDevice(0, new FocusFinderZInstruction(
              l,
              d,
              cpi, this));
          addDevice(0, new FocusFinderAlphaByVariationInstruction(
              l,
              d,
              cpi, this));
        }
        addDevice(0, new ControlPlaneFocusFinderAlphaByVariationInstruction(d, cpi, this));
        addDevice(0, new ControlPlaneFocusFinderZInstruction(d, cpi, this));
      }
    }

    addDevice(0, new XWingRapidAutoFocusInstruction(this));
    addDevice(0, new SpaceTravelInstruction(this));

    addDevice(0, new FOVBoundingBox(this));
    addDevice(0, new SampleSearch1DInstructionBase(this));
    addDevice(0, new SampleSearch2DInstructionBase(this));
    addDevice(0, new SelectBestQualitySampleInstruction(this));
    addDevice(0, new DrosophilaSelectSampleJustBeforeInvaginationInstruction(this));
    addDevice(0, new RestartTimelapseWhileNoSampleChosenInstruction(this));

    addDevice(0, new AppendConsecutiveHyperDriveImagingInstruction(100, 5, this));
    addDevice(0, new AppendConsecutiveHyperDriveImagingInstruction(100, 10, this));
    addDevice(0, new AppendConsecutiveHyperDriveImagingInstruction(100, 15, this));

    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(10, 15, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(10, 30, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(30, 30, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(90, 30, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(120, 30, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(30, 60, this));
    addDevice(0, new AppendConsecutiveOpticsPrefusedImagingInstruction(30, 80, this));

    addDevice(0, new AppendConsecutiveInterleavedImagingInstruction(10, 30, this));
    addDevice(0, new AppendConsecutiveInterleavedImagingInstruction(10, 60, this));
    addDevice(0, new AppendConsecutiveInterleavedImagingInstruction(10, 90, this));

    addDevice(0, new AppendConsecutiveSequentialImagingInstruction(10, 30, this));
    addDevice(0, new AppendConsecutiveSequentialImagingInstruction(10, 60, this));
    addDevice(0, new AppendConsecutiveSequentialImagingInstruction(10, 90, this));

    addDevice(0, new AppendConsecutiveSingleViewImagingInstruction(0,0, 10, 10, this));
    addDevice(0, new AppendConsecutiveSingleViewImagingInstruction(0,0, 10, 30, this));
    addDevice(0, new AppendConsecutiveSingleViewImagingInstruction(0,0, 10, 60, this));



    addDevice(0, new AppendConsecutiveHybridImagingInstruction(200, 5, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(200, 10, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(200, 15, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(200, 30, 60, this));

    addDevice(0, new AppendConsecutiveHybridImagingInstruction(360, 5, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(360, 10, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(360, 15, 60, this));
    addDevice(0, new AppendConsecutiveHybridImagingInstruction(360, 30, 60, this));



    addDevice(0, new TimelapseStopInstruction(this));

    addDevice(0, new ChangeLightSheetWidthInstruction(this, 0));
    addDevice(0, new ChangeLightSheetWidthInstruction(this, 0.15));
    addDevice(0, new ChangeLightSheetWidthInstruction(this, 0.3));
    addDevice(0, new ChangeLightSheetWidthInstruction(this, 0.45));
  }

}
