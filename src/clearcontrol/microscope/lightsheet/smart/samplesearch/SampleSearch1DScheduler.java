package clearcontrol.microscope.lightsheet.smart.samplesearch;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.state.spatial.FOVBoundingBox;
import clearcontrol.microscope.lightsheet.state.spatial.Position;

import java.util.ArrayList;

/**
 * SampleSearch1DScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SampleSearch1DScheduler extends SampleSearchScheduler implements LoggingFeature {


    public SampleSearch1DScheduler() {
        super("Smart: Detect samples along a line in XYZ");
    }


    @Override
    public boolean enqueue(long pTimePoint) {
        FOVBoundingBox lFOV = mLightSheetMicroscope.getDevice(FOVBoundingBox.class, 0);

        ArrayList<Position> lTravelPathList = lFOV.getTravelPathList();

        info("FOV contains " + lTravelPathList.size() + " items");


        int steps = 0;

        for (int i = 0; i < lTravelPathList.size() - 1; i++) {
            Position lStartPosition = lTravelPathList.get(i);
            Position lEndPosition = lTravelPathList.get(i+1);

            double distance = Math.sqrt(
                    Math.pow(lStartPosition.mX - lEndPosition.mX, 2) +
                            Math.pow(lStartPosition.mY - lEndPosition.mY, 2) +
                            Math.pow(lStartPosition.mZ - lEndPosition.mZ, 2));

            steps = (int)(distance / mStepSizeInMillimetersVariable.get() + 1);
            info("FOV can be travelled through with " + steps + " steps");

            double stepX = (lEndPosition.mX - lStartPosition.mX) / (steps - 1);
            double stepY = (lEndPosition.mY - lStartPosition.mY) / (steps - 1);
            double stepZ = (lEndPosition.mZ - lStartPosition.mZ) / (steps - 1);

            for (int s = 0; s < steps; s++) {
                Position lPosition = new Position(
                        lStartPosition.mX + s * stepX,
                        lStartPosition.mY + s * stepY,
                        lStartPosition.mZ + s * stepZ
                );
                mSampleCandidates.getTravelPathList().add(lPosition);
                info("Added candidate position " + lPosition);
            }

        }


        SpaceTravelScheduler lSpaceTravelScheduler = mLightSheetMicroscope.getDevice(SpaceTravelScheduler.class, 0);
        ArrayList<Position> lDetectedSamplesPositionList = lSpaceTravelScheduler.getTravelPathList();
        lDetectedSamplesPositionList.clear();


        ArrayList<Double> lAverageSignalIntensities = measureAverageSignalIntensityAtCandiatePositions();
        int count = 0;
        for (int i = 0; i < lTravelPathList.size() - 1; i++) {

            for (int s = 0; s < steps; s++) {
                boolean localMaximum = true;
                if (count > 0 && lAverageSignalIntensities.get(count - 1) > lAverageSignalIntensities.get(count)) {
                    localMaximum = false;
                }
                if (count < lAverageSignalIntensities.size() - 1 && lAverageSignalIntensities.get(count + 1) > lAverageSignalIntensities.get(count)) {
                    localMaximum = false;
                }
                if (localMaximum) {
                    lDetectedSamplesPositionList.add(mSampleCandidates.getTravelPathList().get(count));
                }
                count++;
            }

        }



        return true;
    }
}
