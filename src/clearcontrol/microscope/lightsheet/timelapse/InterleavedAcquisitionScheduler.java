package clearcontrol.microscope.lightsheet.timelapse;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.LightSheetMicroscopeQueue;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.processor.MetaDataFusion;
import clearcontrol.microscope.lightsheet.stacks.MetaDataView;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.StackMetaData;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.python.core.Py.False;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * February 2018
 */
public class InterleavedAcquisitionScheduler extends SchedulerBase implements
                                                                   SchedulerInterface,
                                                                   LoggingFeature
{
  /**
   * INstanciates a virtual device with a given name
   */
  public InterleavedAcquisitionScheduler()
  {
    super("Interleaved acquisition");
  }

  @Override public boolean doExperiment(long pTimePoint)
  {
    if (!(mMicroscope instanceof LightSheetMicroscope))
    {
      warning(""
              + this
              + " needs a lightsheet microscope to be scheduled properly");
      return false;
    }

    int lImageWidth = 2048;
    int lImageHeight = 2048;
    double lExposureTimeInSeconds = 0.05;

    double lIlluminationZStart = 0;
    double lDetectionZZStart = 0;

    double lLightsheetWidth = 0.45;
    double lLightsheetHeight = 500;
    double lLightsheetX = 0;
    double lLightsheetY = 0;

    int lNumberOfImagesToTake = 10;
    double lDetectionZStep = 2;
    double lIlluminationZStep = 2;

    LightSheetMicroscope
        lLightsheetMicroscope =
        (LightSheetMicroscope) mMicroscope;

    // build a queue
    LightSheetMicroscopeQueue
        lQueue =
        lLightsheetMicroscope.requestQueue();

    // initialize queue
    lQueue.clearQueue();
    lQueue.setCenteredROI(lImageWidth, lImageHeight);

    lQueue.setExp(lExposureTimeInSeconds);

    // initial position
    goToInitialPosition(lLightsheetMicroscope,
                        lQueue,
                        lLightsheetWidth,
                        lLightsheetHeight,
                        lLightsheetX,
                        lLightsheetY,
                        lIlluminationZStart,
                        lDetectionZZStart);

    // --------------------------------------------------------------------
    // build a queue

    for (int lImageCounter = 0; lImageCounter
                                < lNumberOfImagesToTake; lImageCounter++)
    {
      for (int d = 0; d
                      < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
      {
        lQueue.setDZ(d,
                     lDetectionZZStart
                     + lImageCounter * lDetectionZStep);
        lQueue.setC(d, true);
      }

      // acuqire an image per light sheet + one more
      for (int l = 0; l
                      < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
      {
        // configure light sheets accordingly
        for (int k = 0; k
                        < lLightsheetMicroscope.getNumberOfLightSheets(); k++)
        {
          if (l < lLightsheetMicroscope.getNumberOfLightSheets())
          {
            //  turn all but one light sheet off
            lQueue.setI(k, k == l);
          }

          // always set position for all lightsheets
          lQueue.setIZ(k,
                       lIlluminationZStart + l * lIlluminationZStep);
        }
      }
      lQueue.addCurrentStateToQueue();
    }

    // back to initial position
    goToInitialPosition(lLightsheetMicroscope,
                        lQueue,
                        lLightsheetWidth,
                        lLightsheetHeight,
                        lLightsheetX,
                        lLightsheetY,
                        lIlluminationZStart,
                        lDetectionZZStart);

    lQueue.setTransitionTime(0.1);

    for (int c = 0; c < lLightsheetMicroscope.getNumberOfDetectionArms(); c++)
    {
      StackMetaData
          lMetaData =
          lQueue.getCameraDeviceQueue(c).getMetaDataVariable().get();

      lMetaData.addEntry(MetaDataAcquisitionType.AcquisitionType,
                         AcquisitionType.TimeLapse);
      lMetaData.addEntry(MetaDataView.Camera, c);

      lMetaData.addEntry(MetaDataFusion.RequestFullFusion, true);

      lMetaData.addEntry(MetaDataChannel.Channel, "C" + c + "interleaved");
    }

    lQueue.finalizeQueue();

    // acquire!
    boolean lPlayQueueAndWait = false;
    try
    {
      lPlayQueueAndWait = lLightsheetMicroscope.playQueueAndWaitForStacks(lQueue,
                                                      100 + lQueue
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
    /*
    for (int d = 0; d < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      StackInterface
          lStack =
          lLightsheetMicroscope.getCameraStackVariable(
              d).get();
    }
    */

    return true;
  }

  private void goToInitialPosition(LightSheetMicroscope lLightsheetMicroscope,
                                   LightSheetMicroscopeQueue lQueue,
                                   double lLightsheetWidth,
                                   double lLightsheetHeight,
                                   double lLightsheetX,
                                   double lLightsheetY,
                                   double lIlluminationZStart,
                                   double lDetectionZZStart)
  {

    for (int l = 0; l
                    < lLightsheetMicroscope.getNumberOfLightSheets(); l++)
    {
      lQueue.setI(l, false);
      lQueue.setIW(l, lLightsheetWidth);
      lQueue.setIH(l, lLightsheetHeight);
      lQueue.setIX(l, lLightsheetX);
      lQueue.setIY(l, lLightsheetY);

      lQueue.setIZ(lIlluminationZStart);
    }
    for (int d = 0; d
                    < lLightsheetMicroscope.getNumberOfDetectionArms(); d++)
    {
      lQueue.setDZ(d, lDetectionZZStart);
      lQueue.setC(d, false);

    } lQueue.addCurrentStateToQueue();
  }
}
