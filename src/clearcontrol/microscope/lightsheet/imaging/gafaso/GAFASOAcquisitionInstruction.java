package clearcontrol.microscope.lightsheet.imaging.gafaso;

import autopilot.image.DoubleArrayImage;
import autopilot.measures.FocusMeasures;
import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.interleavedwaist.SplitStackInstruction;
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.containers.ProjectionCommentContainer;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropAllContainersOfTypeInstruction;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;
import ij.IJ;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * GAFASOAcquisitionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 09 2018
 */
public class GAFASOAcquisitionInstruction extends
        AbstractAcquistionInstruction implements
        InstructionInterface,
        LoggingFeature,
        PropertyIOableInstructionInterface
{
    Variable<Boolean> debug = new Variable<Boolean>("Debug", true);

    BoundedVariable<Integer> lightSheetIndex = new BoundedVariable<Integer>("Light sheet index", 0, 0, Integer.MAX_VALUE);
    BoundedVariable<Integer> detectionArmIndex = new BoundedVariable<Integer>("Detection arm index", 0, 0, Integer.MAX_VALUE);

    BoundedVariable<Integer> populationSize = new BoundedVariable<Integer>("Population size", 9, 2, 12);
    private int numberOfPositions = populationSize.get();

    BoundedVariable<Double> stepSizeZ = new BoundedVariable<Double>("Step size Z (in micron)", 1.0, 0.001, Double.MAX_VALUE, 0.001);
    BoundedVariable<Double> stepSizeX = new BoundedVariable<Double>("Step size X (in micron)", 25.0, 0.001, Double.MAX_VALUE, 0.001);
    BoundedVariable<Double> stepSizeAlpha = new BoundedVariable<Double>("Step size alpha (in degrees)", 1.0, 0.001, Double.MAX_VALUE, 0.001);

    Variable<Boolean> optimizeZ = new Variable<Boolean>("Optimize Z", true);
    Variable<Boolean> optimizeAlpha = new Variable<Boolean>("Optimize alpha", false);
    Variable<Boolean> optimizeX = new Variable<Boolean>("Optimize X", true);
    Variable<Boolean> optimizeIndex = new Variable<Boolean>("Optimize light sheet index", true);


    Population<AcquisitionStateSolution> population;

    /**
     * INstanciates a virtual device with a given name
     */
    public GAFASOAcquisitionInstruction(int detectionArmIndex, int lightSheetIndex, LightSheetMicroscope pLightSheetMicroscope)
    {
        super("Acquisition: GAFASO C" + detectionArmIndex +  "L" + lightSheetIndex, pLightSheetMicroscope);

        this.lightSheetIndex.set(lightSheetIndex);
        this.detectionArmIndex.set(detectionArmIndex);

        mChannelName.set("interleaved_gao");
    }

    @Override
    public boolean initialize() {
        super.initialize();

        HashMap<LightSheetDOF, Double> initialStateMap = new HashMap<LightSheetDOF, Double>();
        HashMap<LightSheetDOF, Double> stepStateMap = new HashMap<LightSheetDOF, Double>();

        if (optimizeZ.get()) {
            stepStateMap.put(LightSheetDOF.IZ, stepSizeZ.get());
            initialStateMap.put(LightSheetDOF.IZ, 0.0);
        }
        if (optimizeX.get()) {
            stepStateMap.put(LightSheetDOF.IX, stepSizeX.get());
            initialStateMap.put(LightSheetDOF.IX, 0.0);
        }
        if (optimizeAlpha.get()) {
            stepStateMap.put(LightSheetDOF.IA, stepSizeAlpha.get());
            initialStateMap.put(LightSheetDOF.IA, 0.0);
        }
        if (optimizeIndex.get()) {
            stepStateMap.put(LightSheetDOF.II, 1.0);
            initialStateMap.put(LightSheetDOF.II, 0.0);
        }
        AcquisitionStateSolution startSolution = new AcquisitionStateSolution(initialStateMap, stepStateMap);

        numberOfPositions = populationSize.get();

        population = new Population<AcquisitionStateSolution>(new AcquisitionStateSolutionFactory(startSolution), numberOfPositions, 1);

        // mutate all but first
        for (int i = 1; i < numberOfPositions; i++) {
            population.getSolution(i).mutate();
        }
        population.removeDuplicates();
        fixLightSheetIndexOverflow();


        return true;
    }

    @Override public boolean enqueue(long pTimePoint)
    {


        mCurrentState = (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager().getCurrentState();

        int imageWidth = mCurrentState.getImageWidthVariable().get().intValue();
        int imageHeight = mCurrentState.getImageHeightVariable().get().intValue();
        double exposureTimeInSeconds = mCurrentState.getExposureInSecondsVariable().get().doubleValue();

        int numberOfImagesToTake = mCurrentState.getNumberOfZPlanesVariable().get().intValue();

        // build a queue
        LightSheetMicroscopeQueue
                queue =
                getLightSheetMicroscope().requestQueue();

        // initialize queue
        queue.clearQueue();
        queue.setCenteredROI(imageWidth, imageHeight);

        queue.setExp(exposureTimeInSeconds);

        // initial position
        goToInitialPosition(getLightSheetMicroscope(),
                queue,
                mCurrentState.getStackZLowVariable().get().doubleValue(),
                mCurrentState.getStackZLowVariable().get().doubleValue());

        // --------------------------------------------------------------------
        // build a queue

        for (int lImageCounter = 0; lImageCounter
                < numberOfImagesToTake; lImageCounter++)
        {
            for (int l = 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++) {
                queue.setI(l, false);
            }
            for (int l = 0; l
                    < numberOfPositions; l++)
            {
                AcquisitionStateSolution solution = population.getSolution(l);
                int chosenLightSheetIndex = getLightSheetIndex().get();
                if (optimizeIndex.get()) {
                    chosenLightSheetIndex = solution.state.get(LightSheetDOF.II).intValue();
                }

                mCurrentState.applyAcquisitionStateAtStackPlane(queue,
                        lImageCounter);

                // configure light sheets accordingly
                queue.setI(chosenLightSheetIndex, true);
                for (LightSheetDOF key : solution.state.keySet()) {
                    if (key == LightSheetDOF.IZ) {
                        queue.setIZ(chosenLightSheetIndex, queue.getIZ(lightSheetIndex.get()) + solution.state.get(key));
                    } else if (key == LightSheetDOF.IX){
                        queue.setIX(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IY){
                        queue.setIY(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IA){
                        queue.setIA(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IB){
                        queue.setIB(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IH){
                        queue.setIH(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IP){
                        queue.setIP(chosenLightSheetIndex, solution.state.get(key));
                    } else if (key == LightSheetDOF.IW){
                        queue.setIW(chosenLightSheetIndex, solution.state.get(key));
                    }
                }
                //queue.setIX(lightSheetIndex.get(), lightSheetXPositions[l].get());
                //queue.setIY(lightSheetIndex.get(), lightSheetYPositions[l].get());
                //queue.setIZ(lightSheetIndex.get(), queue.getIZ(lightSheetIndex.get()) + lightSheetDeltaZPositions[l].get());
                for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++) {
                    //System.out.println("on[" + k + "]: " + queue.getI(k));
                    queue.setI(k, k == chosenLightSheetIndex);
                    //System.out.println("on[" + k + "]: " + queue.getI(k));
                }
                queue.addCurrentStateToQueue();
            }
        }

        // back to initial position
        goToInitialPosition(getLightSheetMicroscope(),
                queue,
                mCurrentState.getStackZLowVariable().get().doubleValue(),
                mCurrentState.getStackZLowVariable().get().doubleValue());

        queue.setTransitionTime(0.5);
        queue.setFinalisationTime(0.005);

        StackMetaData
                lMetaData =
                queue.getCameraDeviceQueue(detectionArmIndex.get()).getMetaDataVariable().get();

        lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                AcquisitionType.TimeLapseInterleaved);
        lMetaData.addEntry(MetaDataView.Camera, detectionArmIndex.get());

        lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

        lMetaData.addEntry(MetaDataChannel.Channel,  "interleaved");

        queue.addVoxelDimMetaData(getLightSheetMicroscope(), mCurrentState.getStackZStepVariable().get().doubleValue());
        queue.addMetaDataEntry(MetaDataOrdinals.TimePoint,
                pTimePoint);

        queue.finalizeQueue();

        // acquire!
        boolean lPlayQueueAndWait = false;
        try
        {
            mTimeStampBeforeImaging = System.nanoTime();
            lPlayQueueAndWait = getLightSheetMicroscope().playQueueAndWait(queue,
                    100 + queue
                            .getQueueLength(),
                    TimeUnit.SECONDS);

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
        }

        if (!lPlayQueueAndWait)
        {
            System.out.print("Error while imaging");
            return false;
        }

        // Store results in the DataWarehouse
        InterleavedImageDataContainer lContainer = new InterleavedImageDataContainer(getLightSheetMicroscope());

        int d = detectionArmIndex.get();
        StackInterface lStack = getLightSheetMicroscope().getCameraStackVariable(
                d).get();

        putStackInContainer("C" + d + "interleaved_waist", lStack, lContainer);

        getLightSheetMicroscope().getDataWarehouse().put("interleaved_waist_raw_" + pTimePoint, lContainer);

        ClearCLIJ clij = ClearCLIJ.getInstance();
        ClearCLImage input = clij.converter(lStack).getClearCLImage();
        ClearCLImage tenengradWeights = clij.createCLImage(input.getDimensions(), ImageChannelDataType.Float);
        Kernels.tenengradWeightsSliceWise(clij, tenengradWeights, input);

        ClearCLImage cropped = clij.createCLImage(new long[]{input.getWidth(), input.getHeight(), numberOfPositions}, tenengradWeights.getChannelDataType());

        ClearCLImage maxProjection = clij.createCLImage(new long[]{input.getWidth(), input.getHeight()}, ImageChannelDataType.UnsignedInt8);
        ClearCLImage argMaxProjection = clij.createCLImage(new long[]{input.getWidth(), input.getHeight()}, ImageChannelDataType.UnsignedInt16);

        long[] argMaxHistogram = new long[numberOfPositions];
        for (int i = 0; i < input.getDepth() / numberOfPositions; i ++) {
            Kernels.crop(clij, tenengradWeights, cropped, 0, 0, (int)(input.getDepth() / numberOfPositions) * i);

            Kernels.argMaxProjection(clij, tenengradWeights, maxProjection, argMaxProjection);

            RandomAccessibleInterval<UnsignedShortType> argMaxImg = (RandomAccessibleInterval<UnsignedShortType>) clij.converter(argMaxProjection).getRandomAccessibleInterval();
            Cursor<UnsignedShortType> cursor = Views.iterable(argMaxImg).cursor();

            while (cursor.hasNext()) {
                argMaxHistogram[cursor.next().get() % numberOfPositions]++;
            }

            IJ.saveAsTiff(clij.converter(argMaxProjection).getImagePlus(), getLightSheetMicroscope().getTimelapse().getWorkingDirectory() + "/argmax.tif");
        }

        for (int j = 0; j < argMaxHistogram.length; j++) {
            population.getSolution(j).setFitness(argMaxHistogram[j]);
        }
        IJ.saveAsTiff(clij.converter(input).getImagePlus(), getLightSheetMicroscope().getTimelapse().getWorkingDirectory() + "/input.tif");

        input.close();
        tenengradWeights.close();
        cropped.close();

        maxProjection.close();
        argMaxProjection.close();


        // debug
        if (debug.get()) {
            new DropAllContainersOfTypeInstruction(ProjectionCommentContainer.class, getLightSheetMicroscope().getDataWarehouse()).enqueue(pTimePoint);
            String comment = "";
            for (int i = 0; i < numberOfPositions; i++) {
                comment = comment + population.getSolution(i).toString() + "\n";
                info(population.getSolution(i).toString());
            }
            getLightSheetMicroscope().getDataWarehouse().put("comment_" + pTimePoint, new ProjectionCommentContainer(pTimePoint, comment));
        }

        population = population.runEpoch();
        population.removeDuplicates();

        fixLightSheetIndexOverflow();
        return true;
    }

    private void fixLightSheetIndexOverflow() {
        // fix illumination arm index overflow
        if (optimizeIndex.get()) {
            for (int i = 0; i < numberOfPositions; i++) {
                AcquisitionStateSolution solution = population.getSolution(i);
                if (solution.state.get(LightSheetDOF.II) < 0) {
                    solution.state.remove(LightSheetDOF.II);
                    solution.state.put(LightSheetDOF.II, (double)getLightSheetMicroscope().getNumberOfLightSheets() - 1);
                }
                if (solution.state.get(LightSheetDOF.II) >= getLightSheetMicroscope().getNumberOfLightSheets()) {
                    solution.state.remove(LightSheetDOF.II);
                    solution.state.put(LightSheetDOF.II, 0.0);
                }
            }
        }
    }

    @Override
    public GAFASOAcquisitionInstruction copy() {
        return new GAFASOAcquisitionInstruction(detectionArmIndex.get(), lightSheetIndex.get(), getLightSheetMicroscope());
    }

    public Variable<Boolean> getDebug() {
        return debug;
    }

    public BoundedVariable<Integer> getLightSheetIndex() {
        return lightSheetIndex;
    }

    public BoundedVariable<Integer> getDetectionArmIndex() {
        return detectionArmIndex;
    }

    public BoundedVariable<Integer> getPopulationSize() {
        return populationSize;
    }

    public BoundedVariable<Double> getStepSizeAlpha() {
        return stepSizeAlpha;
    }

    public BoundedVariable<Double> getStepSizeX() {
        return stepSizeX;
    }

    public BoundedVariable<Double> getStepSizeZ() {
        return stepSizeZ;
    }

    public Variable<Boolean> getOptimizeAlpha() {
        return optimizeAlpha;
    }

    public Variable<Boolean> getOptimizeX() {
        return optimizeX;
    }

    public Variable<Boolean> getOptimizeZ() {
        return optimizeZ;
    }

    public Variable<Boolean> getOptimizeIndex() {
        return optimizeIndex;
    }

    @Override
    public Variable[] getProperties() {

        return new Variable[]{
                detectionArmIndex,
                lightSheetIndex,
                stepSizeAlpha,
                stepSizeX,
                stepSizeZ,
                optimizeAlpha,
                optimizeX,
                optimizeZ
        };
    }
}
