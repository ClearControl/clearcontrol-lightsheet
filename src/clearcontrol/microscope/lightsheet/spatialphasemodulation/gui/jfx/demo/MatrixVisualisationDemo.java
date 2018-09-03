package clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.BlueCyanGreenYellowOrangeRedLUT;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.lut.LookUpTable;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.gui.jfx.visualisation.DenseMatrixImage;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomialsDenseMatrix64F;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) January 2018
 */
public class MatrixVisualisationDemo extends Application
{
  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage)
  {
    int lWidth = 100;
    int lHeight = 100;

    LookUpTable lLookUpTable = new BlueCyanGreenYellowOrangeRedLUT();

    Canvas lCanvas = new Canvas(700, 300);
    GraphicsContext lGraphicsContext = lCanvas.getGraphicsContext2D();
    lGraphicsContext.drawImage(new DenseMatrixImage(new ZernikePolynomialsDenseMatrix64F(111,
                                                                                         111,
                                                                                         -3,
                                                                                         5),
                                                    lLookUpTable),
                               0,
                               0,
                               lWidth,
                               lHeight);

    Pane lRootPane = new Pane(lCanvas);
    Scene lScene = new Scene(lRootPane);

    primaryStage.setScene(lScene);
    primaryStage.show();

  }
}
