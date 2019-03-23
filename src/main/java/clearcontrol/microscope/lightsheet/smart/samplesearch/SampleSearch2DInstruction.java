package clearcontrol.microscope.lightsheet.smart.samplesearch;

import java.util.ArrayList;

import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.state.spatial.FOVBoundingBox;
import clearcontrol.microscope.lightsheet.state.spatial.Position;

/**
 * The SampleSearch2DInstruction searches for samples in a given rectangle in X/Y
 * plane. It determines potential positions with that 2D space. Afterwards, images
 * are taken at all of them and mean intensity is measured. With the 2D array of
 * intensities, local maxima a searched then. The corresponding positions are
 * finally stored in a position list.
 *
 * Author: @haesleinhuepf
 * May 2018
 */
public class SampleSearch2DInstruction extends
                                       SampleSearchInstructionBase
{

  public SampleSearch2DInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Search samples in a bounding box in XY plane",
          pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    // determine the bounding box configuration from the microscope
    FOVBoundingBox fov =
                        getLightSheetMicroscope().getDevice(FOVBoundingBox.class,
                                                            0);

    // collect potential positions, where samples could be
    ArrayList<Position> travelPathList = fov.getTravelPathList();

    Position startPosition = travelPathList.get(0);
    Position endPosition = travelPathList.get(travelPathList.size()
                                                - 1);

    double distance = Math.sqrt(
                                Math.pow(startPosition.mX
                                         - endPosition.mX, 2)
                                + Math.pow(startPosition.mY
                                           - endPosition.mY, 2)
                                + Math.pow(startPosition.mZ
                                           - endPosition.mZ, 2));

    int steps = (int) (distance / stepSizeInMillimetersVariable.get()
                       + 1);

    double stepX =
                 (endPosition.mX - startPosition.mX) / (steps - 1);
    double stepY =
                 (endPosition.mY - startPosition.mY) / (steps - 1);
    double stepZ =
                 (endPosition.mZ - startPosition.mZ) / (steps - 1);

    for (int sx = 0; sx < steps; sx++)
    {
      for (int sy = 0; sy < steps; sy++)
      {
        Position lPosition =
                           new Position(startPosition.mX
                                        + sx * stepX,
                                        startPosition.mY
                                                      + sy * stepY,
                                        startPosition.mZ + (sx + sy)
                                                            / 2.0
                                                            * stepZ);
        sampleCandidates.getTravelPathList().add(lPosition);
      }
    }

    SpaceTravelInstruction spaceTravelInstruction =
                                                 getLightSheetMicroscope().getDevice(SpaceTravelInstruction.class,
                                                                                     0);
    ArrayList<Position> detectedSamplesPositionList =
                                                     spaceTravelInstruction.getTravelPathList();
    detectedSamplesPositionList.clear();

    // get the list of mean intensities
    ArrayList<Double> averageSignalIntensities =
                                                measureAverageSignalIntensityAtCandiatePositions();

    // search for local maxima
    for (int sx = 1; sx < steps - 1; sx++)
    {
      for (int sy = 1; sy < steps - 1; sy++)
      {

        boolean localMaximum = true;
        if ((averageSignalIntensities.get(sx * steps
                                           + sy) < averageSignalIntensities.get(sx
                                                                                 * steps
                                                                                 + sy
                                                                                 + 1))
            || (averageSignalIntensities.get(sx * steps
                                              + sy) < averageSignalIntensities.get(sx
                                                                                    * steps
                                                                                    + sy
                                                                                    - 1))
            || (averageSignalIntensities.get(sx * steps
                                              + sy) < averageSignalIntensities.get((sx
                                                                                     + 1)
                                                                                    * steps
                                                                                    + sy))
            || (averageSignalIntensities.get(sx * steps
                                              + sy) < averageSignalIntensities.get((sx
                                                                                     - 1)
                                                                                    * steps
                                                                                    + sy)))
        {
          localMaximum = false;
        }
        if (localMaximum)
        {
          detectedSamplesPositionList.add(sampleCandidates.getTravelPathList()
                                                            .get(sx
                                                                 * steps
                                                                 + sy));
        }
      }
    }

    // the position list doesn't have to be saved back, because we manipulated it by reference.

    return true;
  }

  @Override
  public SampleSearch2DInstruction copy()
  {
    return new SampleSearch2DInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Searches for samples in X/Y and saves the positions in a MoveStageInstruction.";
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[0];
  }
}
