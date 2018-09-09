package clearcontrol.microscope.lightsheet.imaging.interleavedwaist;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionInstruction;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.MetaDataOrdinals;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * <<<<<<< HEAD InterleavedWaistAcquisitionInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018 ======= The
 * InterleavedWaistAcquisitionInstruction allows imaging with several waist
 * positions (X/Y) and illumination arm defocus (Z) in order to optimize image
 * quality. The resulting stacks might be very long. Consider splitting them
 * using the SplitStackInstruction.
 *
 * Author: @haesleinhuepf August 2018 >>>>>>>
 * 83a0cb0ef1c0d22e5e67707f68df10fdedd832d6
 */
public class InterleavedWaistAcquisitionInstruction extends
                                                    AbstractAcquistionInstruction
                                                    implements
                                                    InstructionInterface,
                                                    LoggingFeature,
                                                    PropertyIOableInstructionInterface
{

  <<<<<<<HEAD
  private final static int numberOfPositions = 9;

  BoundedVariable<Integer> lightSheetIndex =
                                           new BoundedVariable<Integer>("Light sheet index",
                                                                        0,
                                                                        0,
                                                                        Integer.MAX_VALUE);

  BoundedVariable<Double>[] lightSheetXPositions;
  BoundedVariable<Double>[] lightSheetYPositions;
  BoundedVariable<Double>[] lightSheetDeltaZPositions;

  /**
   * INstanciates a virtual device with a given name
   */
  public InterleavedWaistAcquisitionInstruction(int lightSheetIndex,
                                                LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Acquisition: Interleaved waist CxL" + lightSheetIndex,
          pLightSheetMicroscope);

    lightSheetXPositions = new BoundedVariable[numberOfPositions];
    lightSheetYPositions = new BoundedVariable[numberOfPositions];
    lightSheetDeltaZPositions =
                              new BoundedVariable[numberOfPositions];
    for (int i = 0; i < lightSheetXPositions.length; i++)
=======

  private final static int numberOfPositions = 9;

  BoundedVariable<Integer> lightSheetIndex =
                                           new BoundedVariable<Integer>("Light sheet index",
                                                                        0,
                                                                        0,
                                                                        Integer.MAX_VALUE);

  BoundedVariable<Double>[] lightSheetXPositions;
  BoundedVariable<Double>[] lightSheetYPositions;
  BoundedVariable<Double>[] lightSheetDeltaZPositions;

  public InterleavedWaistAcquisitionInstruction(int lightSheetIndex, LightSheetMicroscope pLightSheetMicroscope)
>>>>>>> 83a0cb0ef1c0d22e5e67707f68df10fdedd832d6
    {
      lightSheetXPositions[i] = new BoundedVariable<Double>("X" + i,
                                                            0.0,
                                                            -Double.MAX_VALUE,
                                                            Double.MAX_VALUE,
                                                            0.001);
      lightSheetYPositions[i] = new BoundedVariable<Double>("Y" + i,
                                                            0.0,
                                                            -Double.MAX_VALUE,
                                                            Double.MAX_VALUE,
                                                            0.001);
      lightSheetDeltaZPositions[i] =
                                   new BoundedVariable<Double>("dZ"
                                                               + i,
                                                               0.0,
                                                               -Double.MAX_VALUE,
                                                               Double.MAX_VALUE,
                                                               0.001);
    }
    this.lightSheetIndex.set(lightSheetIndex);

    mChannelName.set("interleaved_waist");
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
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
    // build a queue

    for (int lImageCounter =
                           0; lImageCounter < numberOfImagesToTake; lImageCounter++)
    {
      for (int l =
                 0; l < getLightSheetMicroscope().getNumberOfLightSheets(); l++)
      {
        queue.setI(l, false);
      }
      for (int l = 0; l < lightSheetXPositions.length; l++)
      {
        mCurrentState.applyAcquisitionStateAtStackPlane(queue,
                                                        lImageCounter);

        // configure light sheets accordingly
        queue.setI(lightSheetIndex.get(), true);
        queue.setIX(lightSheetIndex.get(),
                    lightSheetXPositions[l].get());
        queue.setIY(lightSheetIndex.get(),
                    lightSheetYPositions[l].get());
        queue.setIZ(lightSheetIndex.get(),
                    queue.getIZ(lightSheetIndex.get())
                                           + lightSheetDeltaZPositions[l].get());
        for (int k =
                   0; k < getLightSheetMicroscope().getNumberOfLightSheets(); k++)
        {
          System.out.println("on[" + k + "]: " + queue.getI(k));
          queue.setI(k, k == lightSheetIndex.get());
          System.out.println("on[" + k + "]: " + queue.getI(k));
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

    for (int c =
               0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
    {
      StackMetaData lMetaData = queue.getCameraDeviceQueue(c)
                                     .getMetaDataVariable()
                                     .get();

      lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                         AcquisitionType.TimeLapseInterleaved);
      lMetaData.addEntry(MetaDataView.Camera, c);

      lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

      lMetaData.addEntry(MetaDataChannel.Channel, "interleaved");
    }
    queue.addVoxelDimMetaData(getLightSheetMicroscope(),
                              mCurrentState.getStackZStepVariable()
                                           .get()
                                           .doubleValue());
    queue.addMetaDataEntry(MetaDataOrdinals.TimePoint, pTimePoint);

    queue.finalizeQueue();

    // acquire!
    boolean lPlayQueueAndWait = false;
    try
    {
      mTimeStampBeforeImaging = System.nanoTime();
      lPlayQueueAndWait =
                        getLightSheetMicroscope().playQueueAndWait(queue,
                                                                   100 + queue.getQueueLength(),
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
    InterleavedImageDataContainer lContainer =
                                             new InterleavedImageDataContainer(getLightSheetMicroscope());
    for (int d =
               0; d < getLightSheetMicroscope().getNumberOfDetectionArms(); d++)
    {
      StackInterface lStack =
                            getLightSheetMicroscope().getCameraStackVariable(d)
                                                     .get();

      putStackInContainer("C" + d
                          + "interleaved_waist",
                          lStack,
                          lContainer);
    }
    getLightSheetMicroscope().getDataWarehouse()
                             .put("interleaved_waist_raw_"
                                  + pTimePoint, lContainer);

    return true;
  }

  @Override
  public InterleavedWaistAcquisitionInstruction copy()
  {
    return new InterleavedWaistAcquisitionInstruction(lightSheetIndex.get(),
                                                      getLightSheetMicroscope());
  }

  public BoundedVariable<Double>[] getLightSheetXPositions()
  {
    return lightSheetXPositions;
  }

  public BoundedVariable<Double>[] getLightSheetYPositions()
  {
    return lightSheetYPositions;
  }

  public BoundedVariable<Double>[] getLightSheetDeltaZPositions()
  {
    return lightSheetDeltaZPositions;
  }

  public BoundedVariable<Integer> getLightSheetIndex()
  {
    return lightSheetIndex;
  }

  @Override
  public Variable[] getProperties()
  {
    Variable[] result = new Variable[lightSheetXPositions.length
                                     + lightSheetYPositions.length
                                     + lightSheetDeltaZPositions.length
                                     + 1 // lightsheetIndex
    ];

    int count = 0;
    for (int i = 0; i < lightSheetXPositions.length; i++)
    {
      result[count] = lightSheetXPositions[i];
      count++;
    }
    for (int i = 0; i < lightSheetYPositions.length; i++)
    {
      result[count] = lightSheetYPositions[i];
      count++;
    }
    for (int i = 0; i < lightSheetDeltaZPositions.length; i++)
    {
      result[count] = lightSheetDeltaZPositions[i];
      count++;
    }
    result[count] = lightSheetIndex;
    count++;

    return result;
  }
}
