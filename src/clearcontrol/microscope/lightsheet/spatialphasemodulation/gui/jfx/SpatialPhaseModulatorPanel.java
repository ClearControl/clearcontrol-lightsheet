package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.ImagePane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.ejml.data.DenseMatrix64F;

/**
 * SpatialPhaseModulatorPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SpatialPhaseModulatorPanel extends CustomGridPane {

    private double[] zernikeFactors;
    private ImagePane currentZernikeCompositionViewer;

    private ImagePane currentZernikeCompositionNormalizedViewer;

    public SpatialPhaseModulatorPanel(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {

        zernikeFactors = pSpatialPhaseModulatorDeviceInterface.getZernikeFactors();

        currentZernikeCompositionViewer = new ImagePane(50, 50);
        currentZernikeCompositionViewer.setMinHeight(50);
        currentZernikeCompositionViewer.setMinWidth(50);
        GridPane.setHalignment(currentZernikeCompositionViewer, HPos.CENTER);
        add(currentZernikeCompositionViewer, 0, 0);
        add(new Label("Zernike composition"),0, 1);

        currentZernikeCompositionNormalizedViewer = new ImagePane(50, 50);
        currentZernikeCompositionNormalizedViewer.setMinHeight(50);
        currentZernikeCompositionNormalizedViewer.setMinWidth(50);
        GridPane.setHalignment(currentZernikeCompositionNormalizedViewer, HPos.CENTER);
        add(currentZernikeCompositionNormalizedViewer, 0, 3);
        add(new Label("Zernike composition normalised"),0, 4);

        refreshUI();

        Button submitToMirrorButton = new Button("Send to mirror");
        submitToMirrorButton.setOnAction((e) -> {
            pSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikeFactors);
        });
        add(submitToMirrorButton, 0, 5);


        Button resetToZeroButton = new Button("Reset to Zero");
        resetToZeroButton.setOnAction((e) -> {
            for(int i=0;i<zernikeFactors.length;i++){
                zernikeFactors[i]=0.0;
            }
            refreshUI();
            pSpatialPhaseModulatorDeviceInterface.setZernikeFactors(zernikeFactors);
        });
        add(resetToZeroButton, 0, 6);


        int maximumM = (int)Math.sqrt(pSpatialPhaseModulatorDeviceInterface.getZernikeFactors().length);

        int counter = 0;
        for (int n = 0; n <= maximumM; n++) {
            for (int m = -n; m <= n; m += 2) {
                {
                    DenseMatrix64F
                            lZernikeMatrix = TransformMatrices.multiply(
                            new ZernikePolynomialsDenseMatrix64F(50, 50, m, n), 1);
                    DenseMatrixImage
                            lMatrixImage =
                            new DenseMatrixImage(lZernikeMatrix, new BlueCyanGreenYellowOrangeRedLUT());

                    // View icon
                    ImagePane matrixViewPane = new ImagePane(50, 50, lMatrixImage);
                    matrixViewPane.setMinHeight(50);
                    matrixViewPane.setMinWidth(50);
                    GridPane.setHalignment(matrixViewPane, HPos.CENTER);
                    add(matrixViewPane, maximumM + m, n * 3, 2, 1);

                    // label + text field
                    BoundedVariable<Double> zernikeFactorVariable = new BoundedVariable<>("Z" + ZernikePolynomials.jNoll(n, m) + " (n=" + n + ", m=" + m + ") " + ZernikePolynomials.getZernikeModeName(n, m), zernikeFactors[ZernikePolynomials.jNoll(n, m)], -Double.MAX_VALUE, Double.MAX_VALUE, 0.00000000001);
                    NumberVariableTextField<Double> variableTextField = new NumberVariableTextField<Double>(zernikeFactorVariable.getName(), zernikeFactorVariable);
                    GridPane.setHalignment(variableTextField.getLabel(), HPos.CENTER);
                    GridPane.setHalignment(variableTextField.getTextField(), HPos.CENTER);
                    add(variableTextField.getLabel(), maximumM + m, n * 3 + 1, 2, 1);
                    add(variableTextField.getTextField(), maximumM + m, n * 3 + 2, 2, 1);

                    // interaction
                    final int finalM = m;
                    final int finalN = n;
                    zernikeFactorVariable.addSetListener((oldValue, newValue)->{
                        zernikeFactors[ZernikePolynomials.jNoll(finalN, finalM) - 1] = newValue;
                        refreshUI();
                    });

                    counter++;
                }
            }
        }
    }

    private void refreshUI() {
        DenseMatrix64F zernikeCompositionAsMatrix = ZernikePolynomials.zernikeComposition(zernikeFactors);

        BlueCyanGreenYellowOrangeRedLUT lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();

        DenseMatrixImage
                lMatrixImage =
                new DenseMatrixImage(zernikeCompositionAsMatrix, lLookUpTable);

        currentZernikeCompositionViewer.setImage(lMatrixImage);


        double lMin = TransformMatrices.getMinOfMatrix(zernikeCompositionAsMatrix);
        double lMax = TransformMatrices.getMaxOfMatrix(zernikeCompositionAsMatrix);

        double lFactor = 1.0 / Math.max(Math.abs(lMin), Math.abs(lMax));
        DenseMatrix64F lScaledMatrix = TransformMatrices.multiply(zernikeCompositionAsMatrix, lFactor);
        currentZernikeCompositionNormalizedViewer.setImage(new DenseMatrixImage(lScaledMatrix, lLookUpTable));




    }

}
