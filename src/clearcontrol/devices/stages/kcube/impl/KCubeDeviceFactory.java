package clearcontrol.devices.stages.kcube.impl;

import aptj.APTJDeviceFactory;
import aptj.APTJDeviceType;
import aptj.APTJExeption;
import aptj.bindings.APTLibrary;
import clearcontrol.core.device.VirtualDevice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This factory allows to produce K-Cube devices representing Thorlabs
 * Kinesis KST101 step motor control units.
 *
 * IMPORTANT: The factory device must be closed properly! Otherwise
 * your application may crash at next startup. It is recommended to
 * add the device to the microscope device list. Then it will be
 * closed properly as soon as the app closes.
 *
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG (http://mpi-cbg.de)
 * January 2018
 */
public class KCubeDeviceFactory extends VirtualDevice
{
  private static KCubeDeviceFactory sKCubeDeviceFactory = null;

  protected APTJDeviceFactory mAPTJLibrary;
  protected ArrayList<KCubeDevice> mDeviceList = new ArrayList<KCubeDevice>();

  public static boolean sDebugToLogFile = true;
  public static boolean sDebugToStdOut = true;

  private KCubeDeviceFactory() {
    super("KCubeDeviceFactory");
    log("Trying to cleanup Thorlabs Kinesis");
    APTLibrary.APTCleanUp();
    log("Trying to connect to Thorlabs Kinesis");
    APTLibrary.APTInit();
    log("Configure Kinesis");
    APTLibrary.EnableEventDlg(0);
    log("Trying to connect to Thorlabs Kinesis KCube factory ");


    try
    {
      mAPTJLibrary = new APTJDeviceFactory(APTJDeviceType.TST001);
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }
    log("Thorlabs Kinesis initialisation finished.");
  }

  public static KCubeDeviceFactory getInstance() {
    if (sKCubeDeviceFactory == null) {
      sKCubeDeviceFactory = new KCubeDeviceFactory();
    }
    return sKCubeDeviceFactory;
  }

  public KCubeDevice createKCubeDevice(long pSerialID, String pName) {
    log("Trying to connect to KCube " + pName + " " + pSerialID);
    try
    {
      KCubeDevice lDevice = new KCubeDevice(mAPTJLibrary.createDeviceFromSerialNumber(pSerialID), pName);
      mDeviceList.add(lDevice);
      return lDevice;
    }
    catch (APTJExeption aptjExeption)
    {
      aptjExeption.printStackTrace();
    }
    log("Connecting to " + pSerialID + " failed");
    return null;
  }


  private void log(String pLog) {
    if (sDebugToStdOut)
    {
      System.out.println(pLog);
    }
    if (sDebugToLogFile)
    {
      File lLogFile = new File("log.txt");
      try
      {
        FileOutputStream lStream = new FileOutputStream(lLogFile, true);
        lStream.write(pLog.getBytes());
        lStream.close();
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }


  @Override
  public boolean open()
  {
    return true;
  }

  @Override
  public boolean close()
  {
    log("Closing KCube factory");
    sKCubeDeviceFactory = null;

    log("Closing APTJ factory");
    try
    {
      mAPTJLibrary.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    mAPTJLibrary = null;

    log("Closing APT lib connection");
    APTLibrary.APTCleanUp();



    //mAPTJLibrary.createDeviceFromIndex()
    /*for (KCubeDevice lDevice : mDeviceList) {

    }*/
    return true;
  }
}
