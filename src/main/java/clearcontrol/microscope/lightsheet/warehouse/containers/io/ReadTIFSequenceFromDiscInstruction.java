package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.DefaultStackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.StackRecyclerManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.metadata.StackMetaData;
import clearcontrol.stack.sourcesink.source.RawFileStackSource;
import coremem.recycling.RecyclerInterface;
import ij.IJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;

/**
 * The ReadTifSequenceFromDiscInstruction allows reading RAW images
 * from disc. This allows simulation of workflows with data originally acquired
 * by a microscope.
 *
 * Author: @haesleinhuepf
 * March 2019
 */
public class ReadTIFSequenceFromDiscInstruction extends
                                                            LightSheetMicroscopeInstructionBase
                                                            implements
                                                            LoggingFeature,
                                                            PropertyIOableInstructionInterface
{
  Variable<String> mDatasetName = new Variable<String>("Data set name", "default");
  BoundedVariable<Integer> mTimepointStepSize =
                                              new BoundedVariable<Integer>("Read every nth time point",
                                                                           1,
                                                                           1,
                                                                           Integer.MAX_VALUE);
  BoundedVariable<Integer> mTimepointOffset =
                                            new BoundedVariable<Integer>("Start at nth time point",
                                                                         0,
                                                                         0,
                                                                         Integer.MAX_VALUE);

  private Variable<File> mRootFolderVariable;

  private Variable<Boolean> mRestartFromBeginningWhenReachingEnd =
                                                                 new Variable<Boolean>("Restart when reached final file",
                                                                                       false);

  private int mReadTimePoint = 0;

  public ReadTIFSequenceFromDiscInstruction(            LightSheetMicroscope pLightSheetMicroscope)
  {
    super("IO: Read a TIF sequence from disc", pLightSheetMicroscope);

    mRootFolderVariable = new Variable("RootFolder",
                                       new File(System.getProperty("user.home")
                                                + "/Desktop"));
  }

  @Override
  public boolean initialize()
  {
    mReadTimePoint = mTimepointOffset.get();
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    File lRootFolder = getRootFolderVariable().get();

    //String lDatasetname = lRootFolder.getName();

    //lRootFolder = lRootFolder.getParentFile();

    StackInterfaceContainer lContainer;
    String lContainerWarehouseKey;

    File[] files = lRootFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".tif");
      }
    });


    lContainer = new DefaultStackInterfaceContainer(pTimePoint);
    lContainerWarehouseKey = mDatasetName.get() + "_" + pTimePoint;


    StackRecyclerManager lStackRecyclerManager =
                                               getLightSheetMicroscope().getDevice(StackRecyclerManager.class,
                                                                                   0);
    RecyclerInterface<StackInterface, StackRequest> lRecycler =
                                                              lStackRecyclerManager.getRecycler("warehouse",
                                                                                                1024,
                                                                                                1024);

    info("getting " + mDatasetName.get() + " tp " + mReadTimePoint);
    try {
      if (mReadTimePoint < files.length) {
        String filename = files[mReadTimePoint].toString();
        info("Reading " + filename);


        ImagePlus tifImp = IJ.openImage(filename);
        CLIJ clij = CLIJ.getInstance();

        // todo generate stacks with the help of the recycler above

        StackInterface stack = clij.convert(tifImp, StackInterface.class);
        StackMetaData metaData = new StackMetaData();
        metaData.setVoxelDimX(tifImp.getCalibration().pixelWidth);
        metaData.setVoxelDimY(tifImp.getCalibration().pixelHeight);
        metaData.setVoxelDimZ(tifImp.getCalibration().pixelDepth);
        metaData.setTimeStampInNanoseconds(System.nanoTime());
        stack.setMetaData(metaData);

        lContainer.put(mDatasetName.get(), stack);
      }
      else
      {
        warning("Error: could not load file " + lRootFolder
                + " "
                + mDatasetName
                + "!");
      }
    }
    catch (NullPointerException e)
    {
      e.printStackTrace();
    }


    mReadTimePoint += mTimepointStepSize.get();
    if(mReadTimePoint > files.length && mRestartFromBeginningWhenReachingEnd.get()) {
      mReadTimePoint = mTimepointOffset.get();
    }
    getLightSheetMicroscope().getDataWarehouse()
                             .put(lContainerWarehouseKey, lContainer);

    return false;
  }

  public Variable<File> getRootFolderVariable()
  {
    return mRootFolderVariable;
  }

  @Override
  public ReadTIFSequenceFromDiscInstruction copy()
  {
    return new ReadTIFSequenceFromDiscInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Read Tif images from disc and store it image by image in the DataWarehouse.";
  }

  public Variable<String> getDatasetName() {
    return mDatasetName;
  }

  public BoundedVariable<Integer> getTimepointOffset()
  {
    return mTimepointOffset;
  }

  public BoundedVariable<Integer> getTimepointStepSize()
  {
    return mTimepointStepSize;
  }

  public Variable<Boolean> getRestartFromBeginningWhenReachingEnd()
  {
    return mRestartFromBeginningWhenReachingEnd;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    {
      getRestartFromBeginningWhenReachingEnd(),
      getRootFolderVariable(),
      getTimepointOffset(),
      getTimepointStepSize(),
      getDatasetName()
    };
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[]{StackInterfaceContainer.class};
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[0];
  }
}
