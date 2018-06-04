package clearcontrol.microscope.lightsheet.gui;

import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.devices.stages.kcube.gui.BasicThreeAxesStagePanel;
import clearcontrol.devices.stages.kcube.gui.KCubePane;
import clearcontrol.devices.stages.kcube.impl.KCubeDevice;
import clearcontrol.devices.stages.kcube.scheduler.BasicThreeAxesStageScheduler;
import clearcontrol.devices.stages.kcube.scheduler.gui.BasicThreeAxesStageSchedulerPanel;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderAlphaByVariationScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.FocusFinderZScheduler;
import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.gui.FocusFinderAlphaByVariationSchedulerPanel;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.gui.FocusFinderZSchedulerPanel;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.gui.SpaceTravelPathPlanningPanel;
import clearcontrol.microscope.lightsheet.imaging.exposuremodulation.ExposureModulatedAcquisitionScheduler;
import clearcontrol.microscope.lightsheet.imaging.exposuremodulation.gui.ExposureModulatedAcquisitionSchedulerPanel;
import clearcontrol.microscope.lightsheet.interactive.gui.InteractiveAcquisitionStatisticsPanel;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.gui.SpotDetectionSchedulerPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.SpatialPhaseModulatorPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler.GeneticAlgorithmMirrorModeOptimizeScheduler;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.scheduler.gui.GeneticAlgorithmMirrorModeOptimizeSchedulerPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.LoadMirrorModesFromFolderScheduler;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.scheduler.gui.LoadMirrorModesFromFolderSchedulerPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.ReadStackInterfaceContainerFromDiscScheduler;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.gui.ReadStackInterfaceContainerFromDiscSchedulerPanel;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.DataWarehouseResetScheduler;
import clearcontrol.microscope.lightsheet.warehouse.schedulers.gui.DataWarehouseResetSchedulerPanel;
import javafx.stage.Stage;

import clearcontrol.anything.AnythingDevice;
import clearcontrol.anything.gui.AnythingPanel;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;
import clearcontrol.devices.optomech.filterwheels.gui.jfx.FilterWheelDevicePanel;
import clearcontrol.microscope.adaptive.AdaptiveEngine;
import clearcontrol.microscope.gui.MicroscopeGUI;
import clearcontrol.microscope.gui.halcyon.MicroscopeNodeType;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.AdaptationStateEngine;
import clearcontrol.microscope.lightsheet.adaptive.gui.AdaptationStateEnginePanel;
import clearcontrol.microscope.lightsheet.adaptive.gui.LightSheetAdaptiveEnginePanel;
import clearcontrol.microscope.lightsheet.calibrator.CalibrationEngine;
import clearcontrol.microscope.lightsheet.calibrator.gui.CalibrationEnginePanel;
import clearcontrol.microscope.lightsheet.calibrator.gui.CalibrationEngineToolbar;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmInterface;
import clearcontrol.microscope.lightsheet.component.detection.gui.DetectionArmPanel;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetInterface;
import clearcontrol.microscope.lightsheet.component.lightsheet.gui.LightSheetPanel;
import clearcontrol.microscope.lightsheet.interactive.InteractiveAcquisition;
import clearcontrol.microscope.lightsheet.interactive.gui.InteractiveAcquisitionToolbar;
import clearcontrol.microscope.lightsheet.livestatistics.LiveStatisticsProcessor;
import clearcontrol.microscope.lightsheet.livestatistics.gui.LiveStatisticsPanel;
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.OfflineFastFusionEngine;
import clearcontrol.microscope.lightsheet.processor.gui.LightSheetFastFusionProcessorPanel;
import clearcontrol.microscope.lightsheet.processor.gui.OfflineFastFusionPanel;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.signalgen.gui.LightSheetSignalGeneratorPanel;
import clearcontrol.microscope.lightsheet.state.gui.AcquisitionStateManagerPanel;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.timelapse.gui.LightSheetTimelapseToolbar;
import clearcontrol.microscope.state.AcquisitionStateManager;
import clearcontrol.microscope.timelapse.gui.TimelapsePanel;
import clearcontrol.microscope.timelapse.timer.TimelapseTimerInterface;

/**
 * Lightsheet microscope Ggraphical User Interface (GUI)
 *
 * @author royer
 */
public class LightSheetMicroscopeGUI extends MicroscopeGUI
{

  /**
   * Instanciates a lightsheet microscope GUI given a lightsheet microscope and
   * two flags determining whether to setup 2D and 3D displays.
   * 
   * @param pLightSheetMicroscope
   *          lightsheet microscope
   * @param pPrimaryStage
   *          JFX primary stage
   * @param p2DDisplay
   *          true -> setup 2D display
   * @param p3DDisplay
   *          true -> setup 3D display
   */
  public LightSheetMicroscopeGUI(LightSheetMicroscope pLightSheetMicroscope,
                                 Stage pPrimaryStage,
                                 boolean p2DDisplay,
                                 boolean p3DDisplay)
  {
    super(pLightSheetMicroscope,
          LSMNodeType.values(),
          pPrimaryStage,
          p2DDisplay,
          p3DDisplay);

    addPanelMappingEntry(LightSheetInterface.class,
                         LightSheetPanel.class,
                         LSMNodeType.LightSheet);

    addPanelMappingEntry(DetectionArmInterface.class,
                         DetectionArmPanel.class,
                         LSMNodeType.DetectionArm);

    /*addPanelMappingEntry(InteractiveAcquisition.class,
                         InteractiveAcquisitionPanel.class,
                         MicroscopeNodeType.Acquisition);/**/

    addPanelMappingEntry(AcquisitionStateManager.class,
                         AcquisitionStateManagerPanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(TimelapseTimerInterface.class,
                         TimelapsePanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetSignalGeneratorDevice.class,
                         LightSheetSignalGeneratorPanel.class,
                         MicroscopeNodeType.Other);

    /*addHalcyonMappingEntry(	AutoPilotInterface.class,
    												AutoPilotPanel.class,
    												MicroscopeNodeType.Acquisition);/**/

    addToolbarMappingEntry(InteractiveAcquisition.class,
                           InteractiveAcquisitionToolbar.class);

    addPanelMappingEntry(InteractiveAcquisition.class,
            InteractiveAcquisitionStatisticsPanel.class,
            MicroscopeNodeType.Acquisition);

    addToolbarMappingEntry(CalibrationEngine.class,
                           CalibrationEngineToolbar.class);

    addPanelMappingEntry(CalibrationEngine.class,
                         CalibrationEnginePanel.class,
                         MicroscopeNodeType.Acquisition);

    addToolbarMappingEntry(LightSheetTimelapse.class,
                           LightSheetTimelapseToolbar.class);

    addPanelMappingEntry(AdaptiveEngine.class,
                         LightSheetAdaptiveEnginePanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(AdaptationStateEngine.class,
                         AdaptationStateEnginePanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetFastFusionProcessor.class,
                         LightSheetFastFusionProcessorPanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LiveStatisticsProcessor.class,
                         LiveStatisticsPanel.class,
                         MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetFastFusionProcessor.class,
                         LightSheetFastFusionProcessorPanel.class,
                         MicroscopeNodeType.Other);

    addToolbarMappingEntry(OfflineFastFusionEngine.class,
                           OfflineFastFusionPanel.class);

    addPanelMappingEntry(FilterWheelDeviceInterface.class,
                         FilterWheelDevicePanel.class,
                         MicroscopeNodeType.FilterWheel);

    addPanelMappingEntry(LoadMirrorModesFromFolderScheduler.class,
                         LoadMirrorModesFromFolderSchedulerPanel.class,
                         MicroscopeNodeType.AdaptiveOptics);

    addPanelMappingEntry(AnythingDevice.class,
                         AnythingPanel.class,
                         MicroscopeNodeType.FilterWheel);

    addPanelMappingEntry(FocusFinderAlphaByVariationScheduler.class,
                         FocusFinderAlphaByVariationSchedulerPanel.class,
                         MicroscopeNodeType.AdaptiveOptics);

    addPanelMappingEntry(FocusFinderZScheduler.class,
                         FocusFinderZSchedulerPanel.class,
                         MicroscopeNodeType.AdaptiveOptics);

    addPanelMappingEntry(SpaceTravelScheduler.class,
                         SpaceTravelPathPlanningPanel.class,
                         MicroscopeNodeType.Stage);

    addPanelMappingEntry(KCubeDevice.class,
                        KCubePane.class,
                        MicroscopeNodeType.Stage);

    addPanelMappingEntry(BasicThreeAxesStageInterface.class,
                        BasicThreeAxesStagePanel.class,
                        MicroscopeNodeType.Stage);

    addPanelMappingEntry(BasicThreeAxesStageScheduler.class,
                        BasicThreeAxesStageSchedulerPanel.class,
                        MicroscopeNodeType.Stage);

    addPanelMappingEntry(CountsSpotsScheduler.class,
                        SpotDetectionSchedulerPanel.class,
                        MicroscopeNodeType.Other);

    addPanelMappingEntry(GeneticAlgorithmMirrorModeOptimizeScheduler.class,
                        GeneticAlgorithmMirrorModeOptimizeSchedulerPanel.class,
                        MicroscopeNodeType.AdaptiveOptics);

    addPanelMappingEntry(ExposureModulatedAcquisitionScheduler.class,
            ExposureModulatedAcquisitionSchedulerPanel.class,
            MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(ReadStackInterfaceContainerFromDiscScheduler.class,
            ReadStackInterfaceContainerFromDiscSchedulerPanel.class,
            MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(SpatialPhaseModulatorDeviceInterface.class,
            SpatialPhaseModulatorPanel.class,
            MicroscopeNodeType.AdaptiveOptics);

    addPanelMappingEntry(DataWarehouseResetScheduler.class,
            DataWarehouseResetSchedulerPanel.class,
            MicroscopeNodeType.Scripting);

  }

  @Override
  public void setup()
  {
    super.setup();
  }

}
