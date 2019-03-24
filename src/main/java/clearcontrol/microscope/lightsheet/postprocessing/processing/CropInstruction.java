package clearcontrol.microscope.lightsheet.postprocessing.processing;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.DefaultStackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.lightsheet.warehouse.instructions.AutoRecyclerInstructionInterface;
import clearcontrol.microscope.lightsheet.warehouse.instructions.DataWarehouseInstructionBase;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;

import java.util.Stack;

/**
 * The CropInstruction takes a StackInterfaceContainer from the DataWarehouse,
 * crops all images in it according to its configuration and put the result back
 * to the DataWarehouse in a new container.
 *
 * Author: @haesleinhuepf 06 2018
 */
public class CropInstruction extends DataWarehouseInstructionBase
                             implements
                             PropertyIOableInstructionInterface,
                             AutoRecyclerInstructionInterface
{

  private BoundedVariable<Integer> mCropXVariable =
                                                  new BoundedVariable<Integer>("Crop X",
                                                                               0,
                                                                               0,
                                                                               Integer.MAX_VALUE);
  private BoundedVariable<Integer> mCropYVariable =
                                                  new BoundedVariable<Integer>("Crop Y",
                                                                               0,
                                                                               0,
                                                                               Integer.MAX_VALUE);
  private BoundedVariable<Integer> mCropZVariable =
                                                  new BoundedVariable<Integer>("Crop Z",
                                                                               0,
                                                                               0,
                                                                               Integer.MAX_VALUE);
  private BoundedVariable<Integer> mCropWidthVariable =
                                                      new BoundedVariable<Integer>("Crop width",
                                                                                   256,
                                                                                   0,
                                                                                   Integer.MAX_VALUE);
  private BoundedVariable<Integer> mCropHeightVariable =
                                                       new BoundedVariable<Integer>("Crop height",
                                                                                    256,
                                                                                    0,
                                                                                    Integer.MAX_VALUE);
  private BoundedVariable<Integer> mCropDepthVariable =
                                                      new BoundedVariable<Integer>("Crop depth",
                                                                                   1,
                                                                                   0,
                                                                                   Integer.MAX_VALUE);

  protected Variable<Boolean> recycleSavedContainers = new Variable<Boolean> ("Recycle containers after cropping", true);


  public CropInstruction(DataWarehouse pDataWarehouse,
                         int pCropX,
                         int pCropY,
                         int pCropWidth,
                         int pCropHeight)
  {
    super("Post-processing: Crop", pDataWarehouse);
    mCropXVariable.set(pCropX);
    mCropYVariable.set(pCropY);
    mCropWidthVariable.set(pCropWidth);
    mCropHeightVariable.set(pCropHeight);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    StackInterfaceContainer lSourceContainer =
                                             getDataWarehouse().getOldestContainer(StackInterfaceContainer.class);
    StackInterfaceContainer lTargetContainer = new DefaultStackInterfaceContainer(pTimePoint);

    for (String key : lSourceContainer.keySet())
    {
      StackInterface lStack = lSourceContainer.get(key);

      CLIJ clij = CLIJ.getInstance();
      ClearCLBuffer src = clij.convert(lStack, ClearCLBuffer.class);
      ClearCLBuffer dst = clij.createCLBuffer(new long[]
      { (long) mCropWidthVariable.get(),
        (long) mCropHeightVariable.get(),
        (long) mCropDepthVariable.get() }, src.getNativeType());

      clij.op().crop(src,
                   dst,
                   mCropXVariable.get(),
                   mCropYVariable.get(),
                   mCropZVariable.get());
      // clij.show(dst, "Processing Quality On");

      StackInterface lCroppedStack = clij.convert(dst, StackInterface.class);
      lCroppedStack.copyMetaDataFrom(lStack);
      dst.close();
      src.close();

      lTargetContainer.put(key, lCroppedStack);
    }
    getDataWarehouse().put("crop_" + pTimePoint, lTargetContainer);

    return true;
  }

  @Override
  public InstructionInterface copy()
  {
    return new CropInstruction(getDataWarehouse(),
                               mCropXVariable.get(),
                               mCropXVariable.get(),
                               mCropWidthVariable.get(),
                               mCropHeightVariable.get());
  }

  @Override
  public String getDescription() {
    return " The CropInstruction takes a StackInterfaceContainer from the DataWarehouse, " +
            "crops all images in it according to its configuration and put the result back " +
            "to the DataWarehouse in a new container.";
  }

  public BoundedVariable<Integer> getCropXVariable()
  {
    return mCropXVariable;
  }

  public BoundedVariable<Integer> getCropYVariable()
  {
    return mCropYVariable;
  }

  public BoundedVariable<Integer> getCropZVariable()
  {
    return mCropZVariable;
  }

  public BoundedVariable<Integer> getCropWidthVariable()
  {
    return mCropWidthVariable;
  }

  public BoundedVariable<Integer> getCropHeightVariable()
  {
    return mCropHeightVariable;
  }

  public BoundedVariable<Integer> getCropDepthVariable()
  {
    return mCropDepthVariable;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getCropXVariable(),
      getCropYVariable(),
      getCropZVariable(),
      getCropWidthVariable(),
      getCropHeightVariable(),
      getCropDepthVariable(),
      getRecycleSavedContainers()};
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
