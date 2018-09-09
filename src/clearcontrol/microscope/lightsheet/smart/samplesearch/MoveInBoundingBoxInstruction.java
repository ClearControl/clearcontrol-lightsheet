package clearcontrol.microscope.lightsheet.smart.samplesearch;

import java.awt.*;
import java.util.ArrayList;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.spatial.Position;

/**
 * The MoveInBoundingBoxInstruction puts a list of positions in the
 * SpaceTravelInstruction in order to scan a given bounding box.
 *
 * Author: @haesleinhuepf August 2018
 */
public class MoveInBoundingBoxInstruction extends
                                          LightSheetMicroscopeInstructionBase
                                          implements
                                          LoggingFeature,
                                          PropertyIOableInstructionInterface
{

  private BoundedVariable<Double> minXPosition =
                                               new BoundedVariable<Double>("Min X (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);
  private BoundedVariable<Double> minYPosition =
                                               new BoundedVariable<Double>("Min Y (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);
  private BoundedVariable<Double> minZPosition =
                                               new BoundedVariable<Double>("Min Z (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);
  private BoundedVariable<Double> maxXPosition =
                                               new BoundedVariable<Double>("Max X (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);
  private BoundedVariable<Double> maxYPosition =
                                               new BoundedVariable<Double>("Max Y (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);
  private BoundedVariable<Double> maxZPosition =
                                               new BoundedVariable<Double>("Max Z (in mm)",
                                                                           0.0,
                                                                           -Double.MAX_VALUE,
                                                                           Double.MAX_VALUE,
                                                                           0.001);

  private BoundedVariable<Integer> stepsX =
                                          new BoundedVariable<Integer>("Number of steps X",
                                                                       10,
                                                                       1,
                                                                       Integer.MAX_VALUE);
  private BoundedVariable<Integer> stepsY =
                                          new BoundedVariable<Integer>("Number of steps Y",
                                                                       10,
                                                                       1,
                                                                       Integer.MAX_VALUE);
  private BoundedVariable<Integer> stepsZ =
                                          new BoundedVariable<Integer>("Number of steps Z",
                                                                       10,
                                                                       1,
                                                                       Integer.MAX_VALUE);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public MoveInBoundingBoxInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Fill position list with positions in bounding box",
          pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    SpaceTravelInstruction spaceTravelInstruction =
                                                  getLightSheetMicroscope().getDevice(SpaceTravelInstruction.class,
                                                                                      0);
    if (spaceTravelInstruction == null)
    {
      warning("No SpaceTravelInstructionFound! Cancelling.");
      return false;
    }

    ArrayList<Position> list =
                             spaceTravelInstruction.getTravelPathList();
    list.clear();

    double stepSizeX = (maxXPosition.get() - minXPosition.get())
                       / (stepsX.get() - 1);
    double stepSizeY = (maxYPosition.get() - minYPosition.get())
                       / (stepsY.get() - 1);
    double stepSizeZ = (maxZPosition.get() - minZPosition.get())
                       / (stepsZ.get() - 1);

    for (int x = 0; x < stepsX.get(); x++)
    {
      for (int y = 0; y < stepsY.get(); y++)
      {
        for (int z = 0; z < stepsZ.get(); z++)
        {
          Position pos =
                       new Position(minXPosition.get()
                                    + x * stepSizeX,
                                    minYPosition.get()
                                                     + y * stepSizeY,
                                    minZPosition.get() + z
                                                         * stepSizeZ);
          list.add(pos);
        }
      }
    }

    return true;
  }

  @Override
  public MoveInBoundingBoxInstruction copy()
  {
    MoveInBoundingBoxInstruction copied =
                                        new MoveInBoundingBoxInstruction(getLightSheetMicroscope());
    copied.minXPosition.set(minXPosition.get());
    copied.minYPosition.set(minYPosition.get());
    copied.minZPosition.set(minZPosition.get());
    copied.maxXPosition.set(maxXPosition.get());
    copied.maxYPosition.set(maxYPosition.get());
    copied.maxZPosition.set(maxZPosition.get());
    copied.stepsX.set(stepsX.get());
    copied.stepsY.set(stepsY.get());
    copied.stepsZ.set(stepsZ.get());
    return copied;
  }

  public BoundedVariable<Double> getMaxXPosition()
  {
    return maxXPosition;
  }

  public BoundedVariable<Double> getMaxYPosition()
  {
    return maxYPosition;
  }

  public BoundedVariable<Double> getMaxZPosition()
  {
    return maxZPosition;
  }

  public BoundedVariable<Double> getMinXPosition()
  {
    return minXPosition;
  }

  public BoundedVariable<Double> getMinYPosition()
  {
    return minYPosition;
  }

  public BoundedVariable<Double> getMinZPosition()
  {
    return minZPosition;
  }

  public BoundedVariable<Integer> getStepsX()
  {
    return stepsX;
  }

  public BoundedVariable<Integer> getStepsY()
  {
    return stepsY;
  }

  public BoundedVariable<Integer> getStepsZ()
  {
    return stepsZ;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getMinXPosition(),
      getMinYPosition(),
      getMinZPosition(),
      getMaxXPosition(),
      getMaxYPosition(),
      getMaxZPosition(),
      getStepsX(),
      getStepsY(),
      getStepsZ() };
  }
}
