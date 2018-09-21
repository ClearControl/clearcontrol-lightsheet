package clearcontrol.microscope.lightsheet.imaging.gafaso;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.ip.iqm.DCTS2D;
import clearcontrol.stack.OffHeapPlanarStack;
import de.mpicbg.spimcat.spotdetection.GPUSpotDetectionSliceBySlice;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
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
import clearcontrol.microscope.lightsheet.postprocessing.visualisation.containers.ProjectionCommentContainer;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.Population;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DropAllContainersOfTypeInstruction;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;
import ij.IJ;

/**
 * Genetic Algorithm For Acquisition State Optimization (GAFASO)
 *
 * GAFASOAcquisitionInstruction implements a genetic algorithm to opitimize
 * image quality over time. It acquires image stacks with a given list of
 * acquisition states (a Population of type AcquisitionStateSolution).
 * Afterwards, it removes the states from the population which resulted in worse
 * image quality. The remaining states are recombined and mutated to make a new
 * population with initial number of members.
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class GAFASOAcquisitionInstruction extends
                                          AbstractAcquistionInstruction
                                          implements
                                          InstructionInterface,
                                          LoggingFeature,
                                          PropertyIOableInstructionInterface
{
  // debugging: todo: remove or set false per default
  private final Variable<Boolean> debug =
                                        new Variable<Boolean>("Debug",
                                                              true);

  private final BoundedVariable<Integer> lightSheetIndex =
                                                         new BoundedVariable<Integer>("Light sheet index",
                                                                                      0,
                                                                                      0,
                                                                                      Integer.MAX_VALUE);
  private final BoundedVariable<Integer> detectionArmIndex =
                                                           new BoundedVariable<Integer>("Detection arm index",
                                                                                        0,
                                                                                        0,
                                                                                        Integer.MAX_VALUE);

  private final BoundedVariable<Integer> populationSize =
                                                        new BoundedVariable<Integer>("Population size",
                                                                                     9,
                                                                                     2,
                                                                                     12);
  private int numberOfPositions = populationSize.get();

  // step sizes
  private final BoundedVariable<Double> stepSizeZ =
                                                  new BoundedVariable<Double>("Step size Z (in micron)",
                                                                              1.0,
                                                                              0.001,
                                                                              Double.MAX_VALUE,
                                                                              0.001);
  private final BoundedVariable<Double> stepSizeX =
                                                  new BoundedVariable<Double>("Step size X (in micron)",
                                                                              25.0,
                                                                              0.001,
                                                                              Double.MAX_VALUE,
                                                                              0.001);
  private final BoundedVariable<Double> stepSizeAlpha =
                                                      new BoundedVariable<Double>("Step size alpha (in degrees)",
                                                                                  1.0,
                                                                                  0.001,
                                                                                  Double.MAX_VALUE,
                                                                                  0.001);


  // Checkboxes to control what will be optimized
  private final Variable<Boolean> optimizeZ =
                                            new Variable<Boolean>("Optimize Z",
                                                                  true);
  private final Variable<Boolean> optimizeAlpha =
                                                new Variable<Boolean>("Optimize alpha",
                                                                      false);
  private final Variable<Boolean> optimizeX =
                                            new Variable<Boolean>("Optimize X",
                                                                  true);
  private final Variable<Boolean> optimizeIndex =
                                                new Variable<Boolean>("Optimize light sheet index",
                                                                      true);

  Population<AcquisitionStateSolution> population;

  public GAFASOAcquisitionInstruction(int detectionArmIndex,
                                      int lightSheetIndex,
                                      LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: GAFASO C" + detectionArmIndex
          + "L"
          + lightSheetIndex,
          pLightSheetMicroscope);

    this.lightSheetIndex.set(lightSheetIndex);
    this.detectionArmIndex.set(detectionArmIndex);

    mChannelName.set("interleaved_gao");
  }

  @Override
  public boolean initialize()
  {
    super.initialize();

    // set an initial AcquisitionState and configure step sizes
    HashMap<LightSheetDOF, Double> initialStateMap =
                                                   new HashMap<LightSheetDOF, Double>();
    HashMap<LightSheetDOF, Double> stepStateMap =
                                                new HashMap<LightSheetDOF, Double>();

    if (optimizeZ.get())
    {
      stepStateMap.put(LightSheetDOF.IZ, stepSizeZ.get());
      initialStateMap.put(LightSheetDOF.IZ, 0.0);
    }
    if (optimizeX.get())
    {
      stepStateMap.put(LightSheetDOF.IX, stepSizeX.get());
      initialStateMap.put(LightSheetDOF.IX, 0.0);
    }
    if (optimizeAlpha.get())
    {
      stepStateMap.put(LightSheetDOF.IA, stepSizeAlpha.get());
      initialStateMap.put(LightSheetDOF.IA, 0.0);
    }
    if (optimizeIndex.get())
    {
      stepStateMap.put(LightSheetDOF.II, 1.0);
      initialStateMap.put(LightSheetDOF.II, 0.0);
    }
    AcquisitionStateSolution startSolution =
                                           new AcquisitionStateSolution(initialStateMap,
                                                                        stepStateMap);

    numberOfPositions = populationSize.get();

    population =
               new Population<AcquisitionStateSolution>(new AcquisitionStateSolutionFactory(startSolution),
                                                        numberOfPositions,
                                                        1);

    // mutate all but first to get a variety of states to test
    for (int i = 1; i < numberOfPositions; i++)
    {
        AcquisitionStateSolution solution = population.getSolution(i);
        if (optimizeIndex.get()) {
            solution.mutate();
            if (solution.state.get(LightSheetDOF.II) < 0) {
                solution.state.remove(LightSheetDOF.II);
                solution.state.put(LightSheetDOF.II, (double)(getLightSheetMicroscope().getNumberOfLightSheets() - 1));
            }
            if (solution.state.get(LightSheetDOF.II) >= getLightSheetMicroscope().getNumberOfLightSheets()) {
                solution.state.remove(LightSheetDOF.II);
                solution.state.put(LightSheetDOF.II, 0.0);
            }
        }
    }

    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint) {
    mCurrentState =
            (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                    .getCurrentState();

    int imageWidth = mCurrentState.getImageWidthVariable()
            .get()
            .intValue();
    int imageHeight = mCurrentState.getImageHeightVariable()
            .get()
            .intValue();
    double exposureTimeInSeconds =
            mCurrentState.getExposureInSecondsVariable()
                    .get()
                    .doubleValue();

    int numberOfImagesToTake =
            mCurrentState.getNumberOfZPlanesVariable()
                    .get()
                    .intValue();

    // build a queue
    LightSheetMicroscopeQueue queue =
            getLightSheetMicroscope().requestQueue();

    // initialize queue
    queue.clearQueue();
    queue.setCenteredROI(imageWidth, imageHeight);

    queue.setExp(exposureTimeInSeconds);

    // initial position
    goToInitialPosition(getLightSheetMicroscope(),
            queue,
            mCurrentState.getStackZLowVariable()
                    .get()
                    .doubleValue(),
            mCurrentState.getStackZLowVariable()
                    .get()
                    .doubleValue());

    // --------------------------------------------------------------------
    // go along Z
    for (int lImageCounter =
         0; lImageCounter < numberOfImagesToTake; lImageCounter++) {
      // set all light sheets off
      for (int l =
           0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++) {
        queue.setI(l, false);
      }
      // configure all states; at each Z-plane, all states are imaged
      // subsequently
      for (int l = 0; l < numberOfPositions; l++) {
        AcquisitionStateSolution solution = population.getSolution(l);
        int chosenLightSheetIndex = getLightSheetIndex().get();
        if (optimizeIndex.get()) {
          chosenLightSheetIndex = solution.state.get(LightSheetDOF.II)
                  .intValue();
        }

        mCurrentState.applyAcquisitionStateAtStackPlane(queue,
                lImageCounter);

        // configure all optimized DOFs
        queue.setI(chosenLightSheetIndex, true);
        for (LightSheetDOF key : solution.state.keySet()) {
          if (key == LightSheetDOF.IZ) {
            queue.setIZ(chosenLightSheetIndex,
                    queue.getIZ(lightSheetIndex.get())
                            + solution.state.get(key));
          } else if (key == LightSheetDOF.IX) {
            queue.setIX(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IY) {
            queue.setIY(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IA) {
            queue.setIA(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IB) {
            queue.setIB(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IH) {
            queue.setIH(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IP) {
            queue.setIP(chosenLightSheetIndex,
                    solution.state.get(key));
          } else if (key == LightSheetDOF.IW) {
            queue.setIW(chosenLightSheetIndex,
                    solution.state.get(key));
          }
        }

        // Workaround: turn light sheets of again (one method above might have
        // side effects) because sometimes light sheets are all on
        // Todo: Fix bug upstream
        for (int k =
             0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++) {
          queue.setI(k, k == chosenLightSheetIndex);
        }
        queue.addCurrentStateToQueue();
      }
    }

    // back to initial position
    goToInitialPosition(getLightSheetMicroscope(),
            queue,
            mCurrentState.getStackZLowVariable()
                    .get()
                    .doubleValue(),
            mCurrentState.getStackZLowVariable()
                    .get()
                    .doubleValue());

    queue.setTransitionTime(0.5);
    queue.setFinalisationTime(0.005);

    // configure meta data
    StackMetaData lMetaData =
            queue.getCameraDeviceQueue(detectionArmIndex.get())
                    .getMetaDataVariable()
                    .get();

    lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
            AcquisitionType.TimeLapseInterleaved);
    lMetaData.addEntry(MetaDataView.Camera, detectionArmIndex.get());

    lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

    lMetaData.addEntry(MetaDataChannel.Channel, "interleaved");

    queue.addVoxelDimMetaData(getLightSheetMicroscope(),
            mCurrentState.getStackZStepVariable()
                    .get()
                    .doubleValue());
    queue.addMetaDataEntry(MetaDataOrdinals.TimePoint, pTimePoint);

    queue.finalizeQueue();

    // acquire!
    boolean lPlayQueueAndWait = false;
    try {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait =
              getLightSheetMicroscope().playQueueAndWait(queue,
                      100 + queue.getQueueLength(),
                      TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

    if (!lPlayQueueAndWait) {
      System.out.print("Error while imaging");
      return false;
    }

    // Store results in the DataWarehouse
    GAFASOStackInterfaceContainer lContainer =
            new GAFASOStackInterfaceContainer(getLightSheetMicroscope(), numberOfPositions);

    int d = detectionArmIndex.get();
    StackInterface lStack =
            getLightSheetMicroscope().getCameraStackVariable(d)
                    .get();

    putStackInContainer("C" + d
                    + "interleaved_waist",
            lStack,
            lContainer);

    getLightSheetMicroscope().getDataWarehouse()
            .put("interleaved_waist_raw_"
                    + pTimePoint, lContainer);


    return true;
  }


  @Override
  public GAFASOAcquisitionInstruction copy()
  {
      return this; // this instruction must be a singleton in order to allow other instructions accessing it via
                   // the microscope.getDevice(GAFASOAcquisitionInstruction.class, 0) interface

    //return new GAFASOAcquisitionInstruction(detectionArmIndex.get(),
    //                                        lightSheetIndex.get(),
    //                                        getLightSheetMicroscope());
  }

  public Variable<Boolean> getDebug()
  {
    return debug;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return lightSheetIndex;
  }

  public BoundedVariable<Integer> getDetectionArmIndex()
  {
    return detectionArmIndex;
  }

  public BoundedVariable<Integer> getPopulationSize()
  {
    return populationSize;
  }

  public BoundedVariable<Double> getStepSizeAlpha()
  {
    return stepSizeAlpha;
  }

  public BoundedVariable<Double> getStepSizeX()
  {
    return stepSizeX;
  }

  public BoundedVariable<Double> getStepSizeZ()
  {
    return stepSizeZ;
  }

  public Variable<Boolean> getOptimizeAlpha()
  {
    return optimizeAlpha;
  }

  public Variable<Boolean> getOptimizeX()
  {
    return optimizeX;
  }

  public Variable<Boolean> getOptimizeZ()
  {
    return optimizeZ;
  }

  public Variable<Boolean> getOptimizeIndex()
  {
    return optimizeIndex;
  }

  @Override
  public Variable[] getProperties()
  {

    return new Variable[]
    { detectionArmIndex,
      lightSheetIndex,
      stepSizeAlpha,
      stepSizeX,
      stepSizeZ,
      optimizeAlpha,
      optimizeX,
      optimizeZ,
      optimizeIndex,
      debug,
      populationSize
    };
  }

    public Population<AcquisitionStateSolution> getPopulation() {
        return population;
    }

  public void setPopulation(Population<AcquisitionStateSolution> population) {
    this.population = population;
  }
}
