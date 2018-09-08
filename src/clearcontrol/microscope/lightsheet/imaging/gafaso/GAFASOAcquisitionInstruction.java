package clearcontrol.microscope.lightsheet.imaging.gafaso;

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
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

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
        AcquisitionStateSolution startSolution = new AcquisitionStateSolution(initialStateMap, stepStateMap);

        numberOfPositions = populationSize.get();

        population = new Population<AcquisitionStateSolution>(new AcquisitionStateSolutionFactory(startSolution), numberOfPositions, 1);

        // mutate all but first
        for (int i = 1; i < numberOfPositions; i++) {
            population.getSolution(i).mutate();
        }

        return true;
    }

    @Override public boolean enqueue(long pTimePoint)
    {
        // debug
        for (int i = 0; i < numberOfPositions; i++) {
            info(population.getSolution(i).toString());
        }


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
                mCurrentState.applyAcquisitionStateAtStackPlane(queue,
                        lImageCounter);

                // configure light sheets accordingly
                queue.setI(lightSheetIndex.get(), true);
                AcquisitionStateSolution solution = population.getSolution(l);
                for (LightSheetDOF key : solution.state.keySet()) {
                    if (key == LightSheetDOF.IZ) {
                        queue.setIZ(lightSheetIndex.get(), queue.getIZ(lightSheetIndex.get()) + solution.state.get(key));
                    } else if (key == LightSheetDOF.IX){
                        queue.setIX(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IY){
                        queue.setIY(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IA){
                        queue.setIA(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IB){
                        queue.setIB(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IH){
                        queue.setIH(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IP){
                        queue.setIP(lightSheetIndex.get(), solution.state.get(key));
                    } else if (key == LightSheetDOF.IW){
                        queue.setIW(lightSheetIndex.get(), solution.state.get(key));
                    }
                }
                //queue.setIX(lightSheetIndex.get(), lightSheetXPositions[l].get());
                //queue.setIY(lightSheetIndex.get(), lightSheetYPositions[l].get());
                //queue.setIZ(lightSheetIndex.get(), queue.getIZ(lightSheetIndex.get()) + lightSheetDeltaZPositions[l].get());
                for (int k = 0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++) {
                    //System.out.println("on[" + k + "]: " + queue.getI(k));
                    queue.setI(k, k == lightSheetIndex.get());
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

        // Split stack
        SplitStackInstruction splitter = new SplitStackInstruction(getLightSheetMicroscope());
        splitter.initialize();
        splitter.enqueue(pTimePoint);

        // Measure quality, update population
        StackInterfaceContainer splitContainer = (StackInterfaceContainer) getLightSheetMicroscope().getDataWarehouse().get("split_" + pTimePoint);
        for (int i = 0; i < numberOfPositions; i ++) {
            population.getSolution(i).setStack(splitContainer.get("C" + detectionArmIndex.get() + "_" + i));
        }
        population.runEpoch();
        population.removeDuplicates();

        return true;
    }

    @Override
    public GAFASOAcquisitionInstruction copy() {
        return new GAFASOAcquisitionInstruction(detectionArmIndex.get(), lightSheetIndex.get(), getLightSheetMicroscope());
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
