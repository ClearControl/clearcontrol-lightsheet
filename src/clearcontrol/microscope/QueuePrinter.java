package clearcontrol.microscope;

import clearcontrol.core.device.queue.QueueInterface;
import clearcontrol.devices.cameras.StackCameraQueue;
import clearcontrol.devices.signalgen.SignalGeneratorQueue;
import clearcontrol.microscope.MicroscopeQueueBase;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArm;
import clearcontrol.microscope.lightsheet.component.detection.DetectionArmQueue;
import clearcontrol.microscope.lightsheet.component.lightsheet.LightSheetQueue;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitch;
import clearcontrol.microscope.lightsheet.component.opticalswitch.LightSheetOpticalSwitchQueue;
import clearcontrol.microscope.lightsheet.signalgen.LightSheetSignalGeneratorQueue;
import clearcontrol.microscope.state.AcquisitionStateBase;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class QueuePrinter
{
  MicroscopeQueueBase mQueue;
  String mLevelString = "";

  public QueuePrinter(MicroscopeQueueBase pQueue) {
    mQueue = pQueue;
  }

  public void printAll()
  {
    printQueue(mQueue);
  }

  private void printQueue(QueueInterface pQueueInterface) {
    System.out.println(mLevelString + pQueueInterface);
    if (pQueueInterface instanceof MicroscopeQueueBase) {
      printMicroscopeQueue((MicroscopeQueueBase)pQueueInterface);
    } else if (pQueueInterface instanceof AcquisitionStateBase) {
      printAcqusitionStateBase((AcquisitionStateBase) pQueueInterface);
    } else if (pQueueInterface instanceof LightSheetSignalGeneratorQueue) {
      printLightSheetSignalGeneratorQueue((LightSheetSignalGeneratorQueue) pQueueInterface);
    } else if (pQueueInterface instanceof DetectionArmQueue) {
      printDetectionArmQueue((DetectionArmQueue) pQueueInterface);
    } else if (pQueueInterface instanceof LightSheetQueue) {
      printLightSheetQueue((LightSheetQueue) pQueueInterface);
    } else if (pQueueInterface instanceof LightSheetOpticalSwitchQueue) {
      printLightSheetOpticalSwitchQueue((LightSheetOpticalSwitchQueue) pQueueInterface);
    } else if (pQueueInterface instanceof SignalGeneratorQueue) {
      printSignalGenerator((SignalGeneratorQueue) pQueueInterface);
    } else if (pQueueInterface instanceof StackCameraQueue){
      printStackCameraQueue((StackCameraQueue) pQueueInterface);
    } else {
      System.out.println(mLevelString + " ...?");
    }
  }

  private void printLightSheetOpticalSwitchQueue(LightSheetOpticalSwitchQueue pQueue) {
    System.out.println(mLevelString + " numberOfSwitches: " + pQueue.getNumberOfSwitches());
  }

  private void printLightSheetQueue(LightSheetQueue pQueue) {
    System.out.println(mLevelString + " Z: " + pQueue.getZVariable());
  }

  private void printDetectionArmQueue(DetectionArmQueue pQueue) {
    System.out.println(mLevelString + " Z: " + pQueue.getZVariable().get());
  }

  private void printLightSheetSignalGeneratorQueue(LightSheetSignalGeneratorQueue pQueue) {
    printQueue(pQueue.getDelegatedQueue());
  }

  private void printStackCameraQueue(StackCameraQueue pQueue) {
    System.out.println(mLevelString + " items:" + pQueue.getQueueLength());
  }

  private void printSignalGenerator(SignalGeneratorQueue pQueue) {
    System.out.println(mLevelString + " Estd time: " + pQueue.estimatePlayTime(TimeUnit.NANOSECONDS) + " ns");
  }

  private void printAcqusitionStateBase( AcquisitionStateBase pQueue) {
    System.out.println(mLevelString + " " + pQueue.getMicroscope().requestQueue());
  }

  private void printMicroscopeQueue(MicroscopeQueueBase pQueue)  {
    String lFormerLevelString = mLevelString;
    mLevelString = mLevelString + " ";
    ArrayList<QueueInterface> lQueueList = pQueue.mQueueList;

    for (QueueInterface lQueue : lQueueList) {
      printQueue(lQueue);
    }

    mLevelString = lFormerLevelString;
  }
}
