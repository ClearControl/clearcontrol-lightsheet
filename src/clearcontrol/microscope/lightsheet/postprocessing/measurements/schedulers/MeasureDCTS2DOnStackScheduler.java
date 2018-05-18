package clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers;

import clearcl.util.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.SliceBySliceDCTS2DContainer;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteConsinusTransformEntropyPerSliceEstimator;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DCTS2DContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

import org.apache.commons.lang.math.IntRange;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.jruby.RubyProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * The MeasureDCTS2DOnStackScheduler measures average image quality of a stack, puts the result in the data warehouse
 * together with spatial (stage) position, timepoint. Furthermore, the measurement is saved in a TSV log file.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class MeasureDCTS2DOnStackScheduler<T extends StackInterfaceContainer> extends SchedulerBase implements LoggingFeature {
    private final Class<T> mClass;

    public MeasureDCTS2DOnStackScheduler(Class<T> pClass) {
        super("Post-processing: DCTS2D measurement for " + pClass.getSimpleName());
        mClass = pClass;
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

        // Read oldest image from the warehouse
        final LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
        DataWarehouse lDataWarehouse = lLightSheetMicroscope.getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        String key = lContainer.keySet().iterator().next();
        StackInterface lStack = lContainer.get(key);

        String targetFolder = lLightSheetMicroscope.getDevice(LightSheetTimelapse.class, 0).getWorkingDirectory().toString();
        long lTimePoint = lContainer.getTimepoint();
        int lDigits = 6;

        ElapsedTime.measure("dcts2d determination", () -> {

            DiscreteConsinusTransformEntropyPerSliceEstimator lDCTS2DEstimator = new DiscreteConsinusTransformEntropyPerSliceEstimator(lStack);
            double[] lQualityPerslice = lDCTS2DEstimator.getQualityArray();

            double lMeanDCTS2DQuality = new Mean().evaluate(lQualityPerslice);

            double lX = 0;
            double lY = 0;
            double lZ = 0;

            for (BasicStageInterface lStage : lLightSheetMicroscope.getDevices(BasicStageInterface.class)) {
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

            DCTS2DContainer lDCTS2DContainer = new DCTS2DContainer(pTimePoint, lX, lY, lZ, lMeanDCTS2DQuality);
            SliceBySliceDCTS2DContainer lSliceBySliceDCTS2DContainer = new SliceBySliceDCTS2DContainer(pTimePoint, lX, lY, lZ, lQualityPerslice);

            lLightSheetMicroscope.getDataWarehouse().put("DCTS2D_" + pTimePoint, lDCTS2DContainer);
            lLightSheetMicroscope.getDataWarehouse().put("SliceBySliceDCTS2D_" + pTimePoint, lSliceBySliceDCTS2DContainer);

            String headline = "t\tX\tY\tZ\tavgDCTS2D\n";
            String resultTableLine = pTimePoint + "\t" + lX + "\t" + lY + "\t" + lZ + "\t" + lMeanDCTS2DQuality + "\n" ;

            int[] lSliceNumber = new IntRange(0, lQualityPerslice.length).toArray();
            String sliceBySliceHeadline = "t\tX\tY\tZ" + (Arrays.toString(lSliceNumber).replace(", ","\t").replace("[","").replace("]",""));
            String sliceBySliceResultTableLine = pTimePoint + "\t" + lX + "\t" + lY + "\t" + lZ + "\t" +
                    Arrays.toString(lQualityPerslice).replace(", ","\t").replace("[","").replace("]","") + "\n" ;



            File lOutputFile = new File(targetFolder + "/dcts2d.tsv");
            File lSliceBySliceOutputFile = new File(targetFolder + "/SliceBySliceDCTS2d.tsv");
            try {
                boolean existedBefore = (lOutputFile.exists());

                BufferedWriter writer = new BufferedWriter(new FileWriter(lOutputFile, true));
                if (!existedBefore) {
                    writer.write(headline);
                }
                writer.write (resultTableLine);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                boolean existedBefore = (lSliceBySliceOutputFile.exists());

                BufferedWriter writer = new BufferedWriter(new FileWriter(lSliceBySliceOutputFile, true));
                if (!existedBefore) {
                    writer.write(sliceBySliceHeadline);
                }
                writer.write (sliceBySliceResultTableLine);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(resultTableLine);
            System.out.println(sliceBySliceResultTableLine);
        });

        //IJ.saveAs(lImpMaximumProjection, "JPEG", targetFolder + "/stacks/thumbnails/" +  String.format("%0" + lDigits + "d", lTimePoint) + ".jpg");
        return true;


    }
}