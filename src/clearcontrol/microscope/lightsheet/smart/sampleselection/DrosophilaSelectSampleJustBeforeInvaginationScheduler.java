package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DCTS2DContainer;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * SelectBestQualitySampleScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DrosophilaSelectSampleJustBeforeInvaginationScheduler extends SchedulerBase implements LoggingFeature {

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public DrosophilaSelectSampleJustBeforeInvaginationScheduler() {
        super("Smart: Drosophila: Select sample just before Invagination");
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope)mMicroscope;
        DataWarehouse lDataWarehouse = lLightSheetMicroscope.getDataWarehouse();

        ArrayList<DCTS2DContainer> lQualityInSpaceList = lDataWarehouse.getContainers(DCTS2DContainer.class);

        if (lQualityInSpaceList.size() == 0) {
            lLightSheetMicroscope.getTimelapse().log("No measurements found. Measure DCTS2D before asking me where the best sample is.");
            warning("No measurements found. Measure DCTS2D before asking me where the best sample is.");
            return false;
        }

        SpaceTravelScheduler lSpaceTravelScheduler = lLightSheetMicroscope.getDevice(SpaceTravelScheduler.class, 0);
        ArrayList<Position> lPositionList = lSpaceTravelScheduler.getTravelPathList();
        if (lPositionList.size() < 2) {
            lLightSheetMicroscope.getTimelapse().log("There is no decision to make. Leaving.");
            info("There is no decision to make. Leaving.");
            return true;
        }

        int [] qualityMeasurementSizes = new int[lPositionList.size()];
        double[][] lQualityGroupedBySample = new double[lPositionList.size()][lQualityInSpaceList.size() / lPositionList.size() + 1];

        //DCTS2DContainer lMaxmimumQualityContainer = lQualityInSpaceList.get(0);
        for (DCTS2DContainer lContainer : lQualityInSpaceList) {
            //if (lContainer.getMeasurement() > lMaxmimumQualityContainer.getMeasurement()) {
            //    lMaxmimumQualityContainer = lContainer;
            //}

            int positionIndex = getClosestPositionIndex(lPositionList, new Position(lContainer.getX(), lContainer.getY(), lContainer.getZ()));

            lQualityGroupedBySample[positionIndex][qualityMeasurementSizes[positionIndex]] = lContainer.getMeasurement();
            qualityMeasurementSizes[positionIndex]++;
        }

        int maxTimePoints = qualityMeasurementSizes[0];
        for (int i = 0; i < qualityMeasurementSizes.length; i++) {
            maxTimePoints = Math.min(maxTimePoints, qualityMeasurementSizes[i]);

            lLightSheetMicroscope.getTimelapse().log("Q[" + i  +"]: " + Arrays.toString(qualityMeasurementSizes));
        }

        if (maxTimePoints < 9) {
            lLightSheetMicroscope.getTimelapse().log("not enough data yet");
            info("not enough data yet");
            return false;
        }

        Position lBestPosition = null;
        for (int i = 1; i < qualityMeasurementSizes.length; i++) {
            if (sampleIsJustBeforeInvagination(lQualityGroupedBySample[i], maxTimePoints)) {
                //lBestPosition = lPositionList.get(i);
                lLightSheetMicroscope.getTimelapse().log("Sample at position " + lPositionList.get(i) + " is apparently undergoing invagiation soon!");
            } else {
                lLightSheetMicroscope.getTimelapse().log("Sample at position " + lPositionList.get(i) + " is NOT undergoing invagiation soon!");
            }
        }


            //info("Best position was " + lMaxmimumQualityContainer.getX() + "/"  + lMaxmimumQualityContainer.getY() + "/"  + lMaxmimumQualityContainer.getZ() + " (DCTS2D = " + lMaxmimumQualityContainer.getMeasurement() + ")");

        //if (lBestPosition != null) {
        //    lPositionList.clear();
        //    lPositionList.add(lBestPosition);
        //}
        return true;
    }

    private double[] derivative(double[] input) {
        double[] output = new double[input.length - 1];

        for (int i = 1; i < input.length; i++) {
            output[i - 1] = input[i] - input[i - 1];
        }

        return output;
    }

    private boolean sampleIsJustBeforeInvagination(double[] pQualityMeasurements, int maxTimePoints) {

        double[] qualityDerivative = derivative(pQualityMeasurements);

        double averageLastThree = Math.abs(1.0 / 3.0 * (
                qualityDerivative[maxTimePoints - 2] +
                        qualityDerivative[maxTimePoints - 3] +
                        qualityDerivative[maxTimePoints - 4]));
        double averageThreeBefore = Math.abs(1.0 / 3.0 * (
                qualityDerivative[maxTimePoints - 5] +
                        qualityDerivative[maxTimePoints - 6] +
                        qualityDerivative[maxTimePoints - 7]));

        // In case of a soon to start invagination we assume quality to be mostly stable. Furthermore, in the past,
        // quality is supposed to have changed a lot. Thus quality deltas should be very different (by a factor):
        return averageThreeBefore / averageLastThree > 5;
    }

    private int getClosestPositionIndex(ArrayList<Position> lPositionList, Position lPosition) {
        int minimumDistanceIndex = -1;
        double minimumDistance = Double.MAX_VALUE;

        for (int i = 0; i < lPositionList.size(); i++) {
            Position lPositionOutOfList = lPositionList.get(i);

            double distance = Math.sqrt(
                    Math.pow(lPosition.mX - lPositionOutOfList.mX, 2.0) +
                    Math.pow(lPosition.mY - lPositionOutOfList.mY, 2.0) +
                    Math.pow(lPosition.mZ - lPositionOutOfList.mZ, 2.0)
            );

            if (distance < minimumDistance) {
                minimumDistance = distance;
                minimumDistanceIndex = i;
            }
        }
        return minimumDistanceIndex;
    }
}
