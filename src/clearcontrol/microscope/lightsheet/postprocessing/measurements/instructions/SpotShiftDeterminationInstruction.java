package clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions;

import clearcl.ClearCLImage;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.SorensonDiceIndexContainer;
import clearcontrol.microscope.lightsheet.postprocessing.containers.SpotsImageContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.StackInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SpotShiftDeterminationInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SpotShiftDeterminationInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {


    private BoundedVariable<Integer> mNumberOfDilations = new BoundedVariable<Integer>("Number of dilations", 8, 2, Integer.MAX_VALUE, 2);


    public SpotShiftDeterminationInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: Determine spot shift", pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse dataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        ArrayList<SpotsImageContainer> spotsImageContainers = dataWarehouse.getContainers(SpotsImageContainer.class);

        if (spotsImageContainers.size() < 2) {

            getLightSheetMicroscope().getTimelapse().log("Not enough spots images found!");
            warning("Not enough spots images found!");
            return false;
        }


        StackInterface lStackA = spotsImageContainers.get(spotsImageContainers.size() - 2).get("spots");
        StackInterface lStackB = spotsImageContainers.get(spotsImageContainers.size() - 1).get("spots");

        if (lStackA == null || lStackB == null) {

            getLightSheetMicroscope().getTimelapse().log("At least one spots image was empty!");
            warning("At least one spots image was empty!");
            return false;
        }

        String targetFolder = getLightSheetMicroscope().getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();

        ElapsedTime.measure("Sorenson-Dice determination", () -> {

            ClearCLIJ clij = ClearCLIJ.getInstance();
            ClearCLImage lCLImageA = clij.converter(lStackA).getClearCLImage();
            ClearCLImage lCLImageB = clij.converter(lStackB).getClearCLImage();

            ClearCLImage flip = clij.createCLImage(lCLImageA.getDimensions(), lCLImageA.getChannelDataType());

            Kernels.dilate(clij, lCLImageA, flip);
            for (int d = 2; d < mNumberOfDilations.get(); d += 2) {
                Kernels.dilate(clij, flip, lCLImageA);
                Kernels.dilate(clij, lCLImageA, flip);
            }
            Kernels.dilate(clij, flip, lCLImageA);


            Kernels.dilate(clij, lCLImageB, flip);
            for (int d = 2; d < mNumberOfDilations.get(); d += 2) {
                Kernels.dilate(clij, flip, lCLImageB);
                Kernels.dilate(clij, lCLImageB, flip);
            }
            Kernels.dilate(clij, flip, lCLImageB);

            Kernels.mask(clij, lCLImageA, lCLImageB, flip);

            double pixelCountOverlap = Kernels.sumPixels(clij, flip);
            double pixelCountA = Kernels.sumPixels(clij, lCLImageA);
            double pixelCountB = Kernels.sumPixels(clij, lCLImageB);

            getLightSheetMicroscope().getTimelapse().log("pixel count overlap " + pixelCountOverlap);
            getLightSheetMicroscope().getTimelapse().log("pixel count a " + pixelCountA);
            getLightSheetMicroscope().getTimelapse().log("pixel count b " + pixelCountB);


            double diceIndex = 2.0 * pixelCountOverlap / (pixelCountA + pixelCountB);
            getLightSheetMicroscope().getTimelapse().log("pixel count dice index " + diceIndex);

            SorensonDiceIndexContainer lSorensonDiceIndexcontainer = new SorensonDiceIndexContainer(pTimePoint, diceIndex);
            dataWarehouse.put("dice_" + pTimePoint, lSorensonDiceIndexcontainer);

            double lX = 0;
            double lY = 0;
            double lZ = 0;

            for (BasicStageInterface lStage : getLightSheetMicroscope().getDevices(BasicStageInterface.class)) {
                if (lStage.toString().contains("X")) {
                    lX = lStage.getPositionVariable().get();
                }
                if (lStage.toString().contains("Y")) {
                    lY = lStage.getPositionVariable().get();
                }
                if (lStage.toString().contains("Z")) {
                    lZ = lStage.getPositionVariable().get();
                }
            }


            String headline = "t\tX\tY\tZ\tDiceIndex\n";
            String resultTableLine = pTimePoint + "\t" + lX + "\t" + lY + "\t" + lZ + "\t" + diceIndex + "\n";

            File lOutputFile = new File(targetFolder + "/dice.tsv");

            try {
                boolean existedBefore = (lOutputFile.exists());

                BufferedWriter writer = new BufferedWriter(new FileWriter(lOutputFile, true));
                if (!existedBefore) {
                    writer.write(headline);
                }
                writer.write(resultTableLine);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            flip.close();
            lCLImageA.close();
            lCLImageB.close();
        });

        return true;
    }

    @Override
    public SpotShiftDeterminationInstruction copy() {
        return new SpotShiftDeterminationInstruction(getLightSheetMicroscope());
    }
}
