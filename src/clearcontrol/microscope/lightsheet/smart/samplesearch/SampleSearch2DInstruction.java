package clearcontrol.microscope.lightsheet.smart.samplesearch;

import clearcontrol.devices.stages.kcube.scheduler.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.state.spatial.FOVBoundingBox;
import clearcontrol.microscope.lightsheet.state.spatial.Position;

import java.util.ArrayList;

/**
 * SampleSearch2DInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SampleSearch2DInstruction extends SampleSearchInstruction {

    public SampleSearch2DInstruction() {
        super("Smart: Search samples in a bounding box in XY plane");
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        FOVBoundingBox lFOV = mLightSheetMicroscope.getDevice(FOVBoundingBox.class, 0);

        ArrayList<Position> lTravelPathList = lFOV.getTravelPathList();

        Position lStartPosition = lTravelPathList.get(0);
        Position lEndPosition = lTravelPathList.get(lTravelPathList.size() - 1);

        double distance = Math.sqrt(
                Math.pow(lStartPosition.mX - lEndPosition.mX, 2) +
                        Math.pow(lStartPosition.mY - lEndPosition.mY, 2) +
                        Math.pow(lStartPosition.mZ - lEndPosition.mZ, 2));

        int steps = (int)(distance / mStepSizeInMillimetersVariable.get() + 1);

        double stepX = (lEndPosition.mX - lStartPosition.mX) / (steps - 1);
        double stepY = (lEndPosition.mY - lStartPosition.mY) / (steps - 1);
        double stepZ = (lEndPosition.mZ - lStartPosition.mZ) / (steps - 1);

        for (int sx = 0; sx < steps; sx++) {
            for (int sy = 0; sy < steps; sy++) {
                Position lPosition = new Position(
                        lStartPosition.mX + sx * stepX,
                        lStartPosition.mY + sy * stepY,
                        lStartPosition.mZ + (sx + sy)/2.0 * stepZ
                );
                mSampleCandidates.getTravelPathList().add(lPosition);
            }
        }


        SpaceTravelInstruction lSpaceTravelScheduler = mLightSheetMicroscope.getDevice(SpaceTravelInstruction.class, 0);
        ArrayList<Position> lDetectedSamplesPositionList = lSpaceTravelScheduler.getTravelPathList();
        lDetectedSamplesPositionList.clear();


        ArrayList<Double> lAverageSignalIntensities = measureAverageSignalIntensityAtCandiatePositions();

        for (int sx = 1; sx < steps - 1; sx++) {
            for (int sy = 1; sy < steps - 1; sy++) {

                boolean localMaximum = true;
                if ((lAverageSignalIntensities.get(sx * steps + sy) < lAverageSignalIntensities.get(sx * steps + sy + 1))  ||
                        (lAverageSignalIntensities.get(sx * steps + sy) < lAverageSignalIntensities.get(sx * steps + sy - 1))  ||
                        (lAverageSignalIntensities.get(sx * steps + sy) < lAverageSignalIntensities.get((sx + 1) * steps + sy))  ||
                        (lAverageSignalIntensities.get(sx * steps + sy) < lAverageSignalIntensities.get((sx - 1) * steps + sy))
                        ){
                    localMaximum = false;
                }
                if (localMaximum) {
                    lDetectedSamplesPositionList.add(mSampleCandidates.getTravelPathList().get(sx * steps + sy));
                }
            }
        }


        return true;
    }
}
