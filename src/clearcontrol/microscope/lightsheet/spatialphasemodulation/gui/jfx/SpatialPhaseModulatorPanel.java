package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.gui.jfx.var.textfield.NumberVariableTextField;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.ImagePane;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.TransformMatrices;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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

    public SpatialPhaseModulatorPanel(SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface) {

        double[] zernikeFactors = pSpatialPhaseModulatorDeviceInterface.getZernikeFactors();

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
                    add(matrixViewPane, maximumM + m, n * 3);

                    // label + text field
                    BoundedVariable<Double> zernikeFactorVariable = new BoundedVariable<>("Z" + counter + "(n=" + n + ", m=" + m + ")", zernikeFactors[counter], -Double.MAX_VALUE, Double.MAX_VALUE, 0.00000000001);
                    NumberVariableTextField<Double> variableTextField = new NumberVariableTextField<Double>(zernikeFactorVariable.getName(), zernikeFactorVariable);
                    add(variableTextField.getLabel(), maximumM + m, n * 3 + 1);
                    add(variableTextField.getTextField(), maximumM + m, n * 3 + 2);

                    counter++;
                }
            }
        }
    }
}
