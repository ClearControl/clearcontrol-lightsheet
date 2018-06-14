package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DCTS2DContainer;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import org.apache.commons.math.stat.descriptive.moment.Mean;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * DrosophilaSelectSampleJustBeforeInvaginationInstruction
 *
 * This instructions analyses recent DCTS2D measurements retrieved from the DataWarehouse. It checks if the following
 * conditions are met:
 * * A sample has quality above average (compared to all samples)
 * * The quality at time points t-1, t-2, t-3 has a five fold smaller gradient than at time points t-4, t-5 and t-6
 *   (on average)
 *
 * If these conditions are met, all spatial positions are removed from the SpaceTravelInstruction but the one which
 * fulfilled the conditions. Then, imaging the single sample can continue.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DrosophilaSelectSampleJustBeforeInvaginationInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    private BoundedVariable<Double> mDerivativeFactorVariable = new BoundedVariable<Double>("derivative factor", 3.0, 0.0, Double.MAX_VALUE, 0.01);

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public DrosophilaSelectSampleJustBeforeInvaginationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Select sample just before first cell invagination (Drosophila)", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        ArrayList<DCTS2DContainer> lQualityInSpaceList = lDataWarehouse.getContainers(DCTS2DContainer.class, true);

        if (lQualityInSpaceList.size() == 0) {
            getLightSheetMicroscope().getTimelapse().log("No measurements found. Measure DCTS2D before asking me where the best sample is.");
            warning("No measurements found. Measure DCTS2D before asking me where the best sample is.");
            return false;
        }

        SpaceTravelInstruction lSpaceTravelScheduler = getLightSheetMicroscope().getDevice(SpaceTravelInstruction.class, 0);
        ArrayList<Position> lPositionList = lSpaceTravelScheduler.getTravelPathList();
        if (lPositionList.size() < 2) {
            getLightSheetMicroscope().getTimelapse().log("There is no decision to make. Leaving.");
            info("There is no decision to make. Leaving.");
            return true;
        }

        if (lQualityInSpaceList.size() % lPositionList.size() != 0) {
            info("Waiting for having same number of measurements for all samples.");
            return true;
        }

        int [] qualityMeasurementSizes = new int[lPositionList.size()];
        double[][] lQualityGroupedBySample = new double[lPositionList.size()][lQualityInSpaceList.size() / lPositionList.size()];

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

            getLightSheetMicroscope().getTimelapse().log("Q[" + i  +"]: " + Arrays.toString(qualityMeasurementSizes));
        }

        if (maxTimePoints < 7) {
            getLightSheetMicroscope().getTimelapse().log("not enough data yet");
            info("not enough data yet");
            return false;
        }

        Position lChosenPosition = null;
        for (int i = 0; i < qualityMeasurementSizes.length; i++) {
            if (sampleQualityAboveMean(lQualityGroupedBySample, i, maxTimePoints - 1)) {
                if (sampleIsJustBeforeInvagination(lQualityGroupedBySample[i], maxTimePoints)) {
                    //lBestPosition = lPositionList.get(i);
                    info("Sample at position " + lPositionList.get(i) + " is apparently undergoing invagiation soon!");
                    getLightSheetMicroscope().getTimelapse().log("Sample at position " + lPositionList.get(i) + " is apparently undergoing invagiation soon!");
                    lChosenPosition = lPositionList.get(i);
                } else {
                    info("Sample at position " + lPositionList.get(i) + " is NOT undergoing invagiation soon!");
                    getLightSheetMicroscope().getTimelapse().log("Sample at position " + lPositionList.get(i) + " is NOT undergoing invagiation soon!");
                }
            } else {
                info("Sample at position " + lPositionList.get(i) + " has too low quality!");
                getLightSheetMicroscope().getTimelapse().log("Sample at position " + lPositionList.get(i) + " has too low quality!");
            }
        }


        if (lChosenPosition != null) {
            info("Chosen position was " + lChosenPosition.mX + "/"  + lChosenPosition.mY + "/"  + lChosenPosition.mZ + "");

            lPositionList.clear();
            lPositionList.add(lChosenPosition);
        }
        return true;
    }

    private boolean sampleQualityAboveMean(double[][] pQualityGroupedBySample, int pSampleIndex, int pTimepoint) {
        double[] lQualityMeasurements = new double[pQualityGroupedBySample.length];
        for (int i = 0; i < lQualityMeasurements.length; i++) {
            lQualityMeasurements[i] = pQualityGroupedBySample[i][pTimepoint];
        }

        double lMeanQuality = new Mean().evaluate(lQualityMeasurements);
        double lQuality = pQualityGroupedBySample[pSampleIndex][pTimepoint];

        return lQuality > lMeanQuality;
    }

    private double[] derivative(double[] input) {
        double[] output = new double[input.length - 1];

        for (int i = 1; i < input.length; i++) {
            output[i - 1] = input[i - 1] - input[i];
        }

        return output;
    }

    private boolean sampleIsJustBeforeInvagination(double[] pQualityMeasurements, int maxTimePoints) {

        double averageLastThree = 1.0 / 3.0 * (
                Math.abs(pQualityMeasurements[maxTimePoints - 1]) +
                        Math.abs(pQualityMeasurements[maxTimePoints - 2]) +
                        Math.abs(pQualityMeasurements[maxTimePoints - 3]));
        double averageThreeBefore = 1.0 / 3.0 * (
                Math.abs(pQualityMeasurements[maxTimePoints - 4]) +
                        Math.abs(pQualityMeasurements[maxTimePoints - 5]) +
                        Math.abs(pQualityMeasurements[maxTimePoints - 6]));



        double[] qualityDerivative = derivative(pQualityMeasurements);

        double averageLastThreeDerivative = 1.0 / 3.0 * (
                Math.abs(qualityDerivative[maxTimePoints - 2]) +
                        Math.abs(qualityDerivative[maxTimePoints - 3]) +
                                Math.abs(qualityDerivative[maxTimePoints - 4]));
        double averageThreeBeforeDerivative = 1.0 / 3.0 * (
                Math.abs(qualityDerivative[maxTimePoints - 5]) +
                        Math.abs(qualityDerivative[maxTimePoints - 6]) +
                                Math.abs(qualityDerivative[maxTimePoints - 7]));

        // In case of a soon to start invagination we assume quality to be mostly stable. Furthermore, in the past,
        // quality is supposed to have changed a lot. Thus quality deltas should be very different (by a factor):
        double factor = averageThreeBeforeDerivative / averageLastThreeDerivative;
        //info("quality: " + Arrays.toString(pQualityMeasurements));
        //info("derivative: " + Arrays.toString(qualityDerivative));
        info("avg der " + averageThreeBeforeDerivative + " " + averageLastThreeDerivative + " avg " + averageThreeBefore + " " + averageLastThree);
        info("Factor: " + factor);
        return (factor > getDerivativeFactorVariable().get()) && (averageThreeBefore < averageLastThree);
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

    public BoundedVariable<Double> getDerivativeFactorVariable() {
        return mDerivativeFactorVariable;
    }

    @Override
    public DrosophilaSelectSampleJustBeforeInvaginationInstruction copy() {
        DrosophilaSelectSampleJustBeforeInvaginationInstruction copied = new DrosophilaSelectSampleJustBeforeInvaginationInstruction(getLightSheetMicroscope());
        copied.mDerivativeFactorVariable.set(mDerivativeFactorVariable.get());
        return copied;
    }
}
