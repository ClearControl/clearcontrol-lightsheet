package clearcontrol.microscope.lightsheet.gui;

import java.util.ArrayList;
import javafx.stage.Stage;

import clearcontrol.devices.stages.BasicThreeAxesStageInterface;
import clearcontrol.devices.stages.kcube.gui.BasicThreeAxesStagePanel;
import clearcontrol.devices.stages.kcube.gui.KCubePane;
import clearcontrol.devices.stages.kcube.impl.KCubeDevice;
import clearcontrol.devices.stages.kcube.instructions.BasicThreeAxesStageInstruction;
import clearcontrol.devices.stages.kcube.instructions.gui.BasicThreeAxesStageInstructionPanel;
import clearcontrol.gui.video.video2d.Stack2DDisplay;
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
import clearcontrol.microscope.lightsheet.processor.LightSheetFastFusionProcessor;
import clearcontrol.microscope.lightsheet.processor.gui.LightSheetFastFusionProcessorPanel;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorDevice;
import clearcontrol.microscope.lightsheet.signalgen.gui.LightSheetSignalGeneratorPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.SpatialPhaseModulatorPanel;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
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

    // addPanelMappingEntry(LiveStatisticsProcessor.class,
    // LiveStatisticsPanel.class,
    // MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(LightSheetFastFusionProcessor.class,
                         LightSheetFastFusionProcessorPanel.class,
                         MicroscopeNodeType.Other);

    // addToolbarMappingEntry(OfflineFastFusionEngine.class,
    // OfflineFastFusionPanel.class);
    //
    // addPanelMappingEntry(FilterWheelDeviceInterface.class,
    // FilterWheelDevicePanel.class,
    // MicroscopeNodeType.FilterWheel);
    //
    // addPanelMappingEntry(LoadMirrorModesFromFolderInstruction.class,
    // LoadMirrorModesFromFolderInstructionPanel.class,
    // MicroscopeNodeType.AdaptiveOptics);

    // addPanelMappingEntry(AnythingDevice.class,
    // AnythingPanel.class,
    // MicroscopeNodeType.FilterWheel);
    //
    // addPanelMappingEntry(FocusFinderAlphaByVariationInstruction.class,
    // FocusFinderAlphaByVariationInstructionPanel.class,
    // MicroscopeNodeType.AdaptiveOptics);
    //
    // addPanelMappingEntry(FocusFinderZInstruction.class,
    // FocusFinderZInstructionPanel.class,
    // MicroscopeNodeType.AdaptiveOptics);
    //
    // addPanelMappingEntry(SpaceTravelInstruction.class,
    // SpaceTravelInstructionPanel.class,
    // MicroscopeNodeType.Stage);

    addPanelMappingEntry(KCubeDevice.class,
                         KCubePane.class,
                         MicroscopeNodeType.Stage);

    addPanelMappingEntry(BasicThreeAxesStageInterface.class,
                         BasicThreeAxesStagePanel.class,
                         MicroscopeNodeType.Stage);

    addPanelMappingEntry(BasicThreeAxesStageInstruction.class,
                         BasicThreeAxesStageInstructionPanel.class,
                         MicroscopeNodeType.Stage);

    // addPanelMappingEntry(CountsSpotsInstruction.class,
    // CountSpotsInstructionPanel.class,
    // MicroscopeNodeType.Other);
    //
    // addPanelMappingEntry(GeneticAlgorithmMirrorModeOptimizeInstruction.class,
    // GeneticAlgorithmMirrorModeOptimizeInstructionPanel.class,
    // MicroscopeNodeType.AdaptiveOptics);
    //
    // addPanelMappingEntry(ExposureModulatedAcquisitionInstruction.class,
    // ExposureModulatedAcquisitionInstructionPanel.class,
    // MicroscopeNodeType.Acquisition);
    //
    // addPanelMappingEntry(ReadStackInterfaceContainerFromDiscInstruction.class,
    // ReadStackInterfaceContainerFromDiscInstructionPanel.class,
    // MicroscopeNodeType.Acquisition);

    addPanelMappingEntry(SpatialPhaseModulatorDeviceInterface.class,
                         SpatialPhaseModulatorPanel.class,
                         MicroscopeNodeType.AdaptiveOptics);
    //
    // addPanelMappingEntry(DataWarehouseResetInstruction.class,
    // DataWarehouseResetInstructionPanel.class,
    // MicroscopeNodeType.Scripting);

    ArrayList<Stack2DDisplay> lDisplayDeviceList =
                                                 get2DDisplayDeviceList();
    for (Stack2DDisplay lDisplay : pLightSheetMicroscope.getDevices(Stack2DDisplay.class))
    {
      if (!lDisplayDeviceList.contains(lDisplay))
      {
        lDisplayDeviceList.add(lDisplay);
      }
    }
  }

  @Override
  public void setup()
  {
    super.setup();
  }

}
