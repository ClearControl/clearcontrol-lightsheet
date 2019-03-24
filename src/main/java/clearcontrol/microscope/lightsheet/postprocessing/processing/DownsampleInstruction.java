package clearcontrol.microscope.lightsheet.postprocessing.processing;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * DownsampleInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class DownsampleInstruction extends
                                   DataWarehouseInstructionBase
                                   implements
                                   PropertyIOableInstructionInterface
{

  private BoundedVariable<Double> mDownSampleFactorX =
                                                     new BoundedVariable<Double>("Factor X",
                                                                                 0.5,
                                                                                 0.0,
                                                                                 1.0,
                                                                                 0.0001);
  private BoundedVariable<Double> mDownSampleFactorY =
                                                     new BoundedVariable<Double>("Factor Y",
                                                                                 0.5,
                                                                                 0.0,
                                                                                 1.0,
                                                                                 0.0001);
  private BoundedVariable<Double> mDownSampleFactorZ =
                                                     new BoundedVariable<Double>("Factor Z",
                                                                                 0.5,
                                                                                 0.0,
                                                                                 1.0,
                                                                                 0.0001);

  protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after downsampling", true);

  public DownsampleInstruction(DataWarehouse pDataWarehouse)
  {
    super("Post-processing: Downsampling", pDataWarehouse);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    StackInterfaceContainer lContainer =
                                       getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);

    StackInterfaceContainer lResultContainer =
                                             new StackInterfaceContainer(pTimePoint)
                                             {
                                               @Override
                                               public boolean isDataComplete()
                                               {
                                                 return true;
                                               }
                                             };

    for (String key : lContainer.keySet())
    {
      StackInterface stack = lContainer.get(key);

      CLIJ clij = CLIJ.getInstance();

      ClearCLImage lCLImage = clij.convert(stack, ClearCLImage.class);
      ClearCLImage lClImageScaled = clij.createCLImage(new long[]
      { (long) (lCLImage.getWidth()
                * mDownSampleFactorX.get().floatValue()),
        (long) (lCLImage.getHeight()
                * mDownSampleFactorY.get().floatValue()),
        (long) (lCLImage.getDepth()
                * mDownSampleFactorZ.get().floatValue()) },
                                                       lCLImage.getChannelDataType());
      clij.op().downsample(
                         lCLImage,
                         lClImageScaled,
                         mDownSampleFactorX.get().floatValue(),
                         mDownSampleFactorY.get().floatValue(),
                         mDownSampleFactorZ.get().floatValue());
      stack = clij.convert(lClImageScaled, StackInterface.class);
      lCLImage.close();
      lClImageScaled.close();

      lResultContainer.put(key, stack);

      // todo: save/convert meta data
    }

    getDataWarehouse().put("downsampled_" + pTimePoint,
                           lResultContainer);

    return true;
  }

  @Override
  public DownsampleInstruction copy()
  {
    DownsampleInstruction copied =
                                 new DownsampleInstruction(getDataWarehouse());
    copied.mDownSampleFactorX.set(mDownSampleFactorX.get());
    copied.mDownSampleFactorY.set(mDownSampleFactorY.get());
    copied.mDownSampleFactorZ.set(mDownSampleFactorZ.get());
    return copied;
  }

  @Override
  public String getDescription() {
    return "Downsamples all stacks in a given container from the warehouse and puts the results back to the warehouse.";
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { mDownSampleFactorX, mDownSampleFactorY, mDownSampleFactorZ, recycleSavedContainers };
  }

  public BoundedVariable<Double> getDownSampleFactorX()
  {
    return mDownSampleFactorX;
  }

  public BoundedVariable<Double> getDownSampleFactorY()
  {
    return mDownSampleFactorY;
  }

  public BoundedVariable<Double> getDownSampleFactorZ()
  {
    return mDownSampleFactorZ;
  }

  public Variable<Boolean> getRecycleSavedContainers() {
    return recycleSavedContainers;
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[]{StackInterfaceContainer.class};
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    if (!recycleSavedContainers.get()) {
      return new Class[0];
    }
    return new Class[]{StackInterfaceContainer.class};
  }
}
