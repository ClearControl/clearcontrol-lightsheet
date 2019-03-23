package clearcontrol.microscope.lightsheet.smart.samplesearch;

import java.util.ArrayList;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.state.spatial.FOVBoundingBox;
import clearcontrol.microscope.lightsheet.state.spatial.Position;

/**
 * The SampleSearch1DInstruction searches samples along a line in space.
 * A given bounding box (consisting of tow points in 3D) describes the stard
 * and end position of the line.
 * The microscope takes images along that line, measures mean intensity and
 * saves the positions of the maxima in a list.
 *
 * Author: @haesleinhuepf
 * May 2018
 */
public class SampleSearch1DInstruction extends
                                       SampleSearchInstructionBase
                                       implements LoggingFeature
{

  public SampleSearch1DInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Detect samples along a line in XYZ",
          pLightSheetMicroscope);
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    // determine the bounding box configuration from the microscope
    FOVBoundingBox fov =
                        getLightSheetMicroscope().getDevice(FOVBoundingBox.class,
                                                            0);

    ArrayList<Position> travelPathList = fov.getTravelPathList();

    info("FOV contains " + travelPathList.size() + " items");

    int steps = 0;

    // collect potential positions, where samples could be
    for (int i = 0; i < travelPathList.size() - 1; i++)
    {
      Position startPosition = travelPathList.get(i);
      Position endPosition = travelPathList.get(i + 1);

      double distance = Math.sqrt(
                                  Math.pow(startPosition.mX
                                           - endPosition.mX, 2)
                                  + Math.pow(startPosition.mY
                                             - endPosition.mY, 2)
                                  + Math.pow(startPosition.mZ
                                             - endPosition.mZ, 2));

      steps = (int) (distance / stepSizeInMillimetersVariable.get()
                     + 1);
      info("FOV can be travelled through with " + steps + " steps");

      double stepX = (endPosition.mX - startPosition.mX)
                     / (steps - 1);
      double stepY = (endPosition.mY - startPosition.mY)
                     / (steps - 1);
      double stepZ = (endPosition.mZ - startPosition.mZ)
                     / (steps - 1);

      for (int s = 0; s < steps; s++)
      {
        Position lPosition =
                           new Position(startPosition.mX + s * stepX,
                                        startPosition.mY + s * stepY,
                                        startPosition.mZ + s
                                                            * stepZ);
        sampleCandidates.getTravelPathList().add(lPosition);
        info("Added candidate position " + lPosition);
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
    int count = 0;
    // search for local maxima
    for (int i = 0; i < travelPathList.size() - 1; i++)
    {

      for (int s = 0; s < steps; s++)
      {
        boolean localMaximum = true;
        if (count > 0
            && averageSignalIntensities.get(count
                                             - 1) > averageSignalIntensities.get(count))
        {
          localMaximum = false;
        }
        if (count < averageSignalIntensities.size() - 1
            && averageSignalIntensities.get(count
                                             + 1) > averageSignalIntensities.get(count))
        {
          localMaximum = false;
        }
        if (localMaximum)
        {
          detectedSamplesPositionList.add(sampleCandidates.getTravelPathList()
                                                            .get(count));
        }
        count++;
      }

    }

    // the position list doesn't have to be saved back, because we manipulated it by reference.

    return true;
  }

  @Override
  public SampleSearch1DInstruction copy()
  {
    return new SampleSearch1DInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Searches for samples in Y direction and saves the positions in a MoveStageInstruction.";
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
