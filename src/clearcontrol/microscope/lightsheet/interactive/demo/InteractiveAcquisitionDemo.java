package clearcontrol.microscope.lightsheet.interactive.demo;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.interactive.InteractiveAcquisition;
import clearcontrol.microscope.lightsheet.interactive.gui.InteractiveAcquisitionToolbar;
import clearcontrol.microscope.lightsheet.simulation.LightSheetMicroscopeSimulationDevice;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.simulation.SimulationUtils;
import clearcontrol.microscope.lightsheet.state.ControlPlaneLayout;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class InteractiveAcquisitionDemo extends Application {
    @Override
    public void start(Stage primaryStage) {

        ClearCLBackendJOCL lClearCLBackend = new ClearCLBackendJOCL();
        ClearCL lClearCL = new ClearCL(lClearCLBackend);
        ClearCLDevice lClearCLDevice = lClearCL.getBestGPUDevice();
        ClearCLContext lCLearCLContext = lClearCLDevice.createContext();

        //        LightSheetMicroscope lLSMicroscope = new LightSheetMicroscope("SomeScope", lCLearCLContext, 100, 10);
        SimulatedLightSheetMicroscope lLightSheetMicroscope = new SimulatedLightSheetMicroscope("some scope", lCLearCLContext, 32, 10);


        LightSheetMicroscopeSimulationDevice lSimulatorDevice =
                SimulationUtils.getSimulatorDevice(lCLearCLContext,
                        2,
                        4,
                        1024,
                        11,
                        320,
                        320,
                        320,
                        false);


        lLightSheetMicroscope.addSimulatedDevices(false,
                true,
                true,
                lSimulatorDevice);

        AcquisitionStateManager<InterpolatedAcquisitionState> lAcquisitionStateManager;
        lAcquisitionStateManager =
                (AcquisitionStateManager<InterpolatedAcquisitionState>) lLightSheetMicroscope.addAcquisitionStateManager();
        InterpolatedAcquisitionState lAcquisitionState =
                new InterpolatedAcquisitionState("default",
                        lLightSheetMicroscope);
        lAcquisitionState.getImageWidthVariable().set(1024);
        lAcquisitionState.getImageHeightVariable()
                .set(1024);

        lAcquisitionState.setupControlPlanes(5,
                ControlPlaneLayout.Circular);
        lAcquisitionStateManager.setCurrentState(lAcquisitionState);
        lLightSheetMicroscope.addInteractiveAcquisition();

//        InteractiveAcquisition lInteractiveAcquisition = new InteractiveAcquisition("Interactive", lLightSheetMicroscope);
        InteractiveAcquisitionToolbar lInteractiveAcquisitionToolbar = new InteractiveAcquisitionToolbar(lLightSheetMicroscope.getDevice
                (InteractiveAcquisition.class, 0));

        GridPane root = new GridPane();
        root.add(lInteractiveAcquisitionToolbar, 0, 0);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void run(String[] args) {
        launch(args);
    }


    public static void main(String[] args) {
        Platform.runLater(() -> {
            new InteractiveAcquisitionDemo().start(new Stage());
        });
    }


}
