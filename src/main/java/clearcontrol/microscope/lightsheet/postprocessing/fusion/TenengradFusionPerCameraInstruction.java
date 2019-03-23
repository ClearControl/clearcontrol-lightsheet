package clearcontrol.microscope.lightsheet.postprocessing.fusion;

import java.util.ArrayList;

import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.metadata.MetaDataAcquisitionType;
import clearcontrol.microscope.state.AcquisitionType;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.metadata.MetaDataChannel;
import clearcontrol.stack.metadata.StackMetaData;

/**
 * The TenengradFusionPerCameraInstruction fuses all images in a container
 * coming from the same camera. It can fuse images from several cameras if it
 * find them in the container.
 *
 * Assumptions: - The cameras deliver images of type UnsignedShort - All Images
 * in the container have the same size
 *
 * Thus, it can be used to fuse images resulting from sequential imaging.
 *
 * Author: @haesleinhuepf August 2018
 */
public class TenengradFusionPerCameraInstruction extends
                                                 LightSheetMicroscopeInstructionBase
                                                 implements
                                                 LoggingFeature,
                                                 PropertyIOableInstructionInterface
{

  private BoundedVariable<Double> blurWeightSigmaX =
                                                   new BoundedVariable<Double>("Blur weights X sigma in pixels",
                                                                               15.0,
                                                                               0.0,
                                                                               Double.MAX_VALUE,
                                                                               0.01);
  private BoundedVariable<Double> blurWeightSigmaY =
                                                   new BoundedVariable<Double>("Blur weights Y sigma in pixels",
                                                                               15.0,
                                                                               0.0,
                                                                               Double.MAX_VALUE,
                                                                               0.01);
  private BoundedVariable<Double> blurWeightSigmaZ =
                                                   new BoundedVariable<Double>("Blur weights Z sigma in pixels",
                                                                               5.0,
                                                                               0.0,
                                                                               Double.MAX_VALUE,
                                                                               0.01);

  private BoundedVariable<Double> weightExponent =
                                                 new BoundedVariable<Double>("Weight exponent",
                                                                             1.0,
                                                                             -Double.MAX_VALUE,
                                                                             Double.MAX_VALUE,
                                                                             0.001);

  /**
   * INstanciates a virtual device with a given name
   *
   * @param pLightSheetMicroscope
   */
  public TenengradFusionPerCameraInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Post-processing: Tenengrad fusion per camera",
          pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {
    CLIJ clij = CLIJ.getInstance();

    StackInterfaceContainer containerIn =
                                        getLightSheetMicroscope().getDataWarehouse()
                                                                 .getOldestContainer(StackInterfaceContainer.class);

    float[] weightBlurSigmas =
    { blurWeightSigmaX.get().floatValue(),
      blurWeightSigmaY.get().floatValue(),
      blurWeightSigmaZ.get().floatValue() };

    TenengradFusedStackInterfaceContainer containerOut =
                                                       new TenengradFusedStackInterfaceContainer(pTimePoint);

    info("Read container with " + containerIn.keySet());

    for (int c =
               0; c < getLightSheetMicroscope().getNumberOfDetectionArms(); c++)
    {
      ArrayList<ClearCLImage> images = new ArrayList<ClearCLImage>();

      StackMetaData metaData = null;
      for (String key : containerIn.keySet())
      {
        if (key.toLowerCase().startsWith("c" + c))
        {
          // Get UnsignedShort stack from container
          StackInterface stack = containerIn.get(key);
          metaData = stack.getMetaData().clone();
          ClearCLImage imageOfAnyType = clij.convert(stack, ClearCLImage.class);

          // Convert it to a float CLImage
          ClearCLImage clImage =
                               clij.createCLImage(stack.getDimensions(),
                                                  ImageChannelDataType.Float);

          //ImageTypeConverter.copyRandomAccessibleIntervalToClearCLImage(rai,
          //                                                              clImage);

          clij.op().copy(imageOfAnyType, clImage);

          imageOfAnyType.close();

          // store the float image in the list;
          images.add(clImage);
        }
      }

      if (images.size() == 0)
      {
        // no images from a given camera
        warning("No images found for camera c" + c);
        continue;
      }

      ClearCLImage fusionResult =
                                clij.createCLImage(images.get(0)
                                                         .getDimensions(),
                                                   ImageChannelDataType.Float);
      ClearCLImage fusionResultAsUnsignedShort =
                                               clij.createCLImage(fusionResult.getDimensions(),
                                                                  ImageChannelDataType.UnsignedInt16);

      ClearCLImage[] imagesIn = new ClearCLImage[images.size()];
      images.toArray(imagesIn);

      info("Fusing " + images.size() + " images");

      // fusion
      clij.op().tenengradFusion(fusionResult,
                              weightBlurSigmas,
                              weightExponent.get().floatValue(),
                              imagesIn);

      // Result conversion / storage
      clij.op().copy(fusionResult, fusionResultAsUnsignedShort);
      StackInterface result =
                            clij.convert(fusionResultAsUnsignedShort, StackInterface.class);
      result.setMetaData(metaData);
      result.getMetaData().removeEntry(MetaDataChannel.Channel);
      result.getMetaData().addEntry(MetaDataChannel.Channel,
                                    "tenengrad_fused");
      result.getMetaData()
            .removeEntry(MetaDataAcquisitionType.AcquisitionType);
      result.getMetaData()
            .addEntry(MetaDataAcquisitionType.AcquisitionType,
                      AcquisitionType.TimelapseSequential);

      containerOut.put("C" + c + "_tenengrad_fused", result);

      // cleanup
      for (ClearCLImage image : images)
      {
        image.close();
      }
      fusionResult.close();
      fusionResultAsUnsignedShort.close();
    }

    getLightSheetMicroscope().getDataWarehouse()
                             .put("tenengrad_fused_" + pTimePoint,
                                  containerOut);

    return true;
  }

  @Override
  public TenengradFusionPerCameraInstruction copy()
  {
    return new TenengradFusionPerCameraInstruction(getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "Fuses all image stacks in a given container using Tenengrad fusion.";
  }

  public BoundedVariable<Double> getBlurWeightSigmaX()
  {
    return blurWeightSigmaX;
  }

  public BoundedVariable<Double> getBlurWeightSigmaY()
  {
    return blurWeightSigmaY;
  }

  public BoundedVariable<Double> getBlurWeightSigmaZ()
  {
    return blurWeightSigmaZ;
  }

  public BoundedVariable<Double> getWeightExponent()
  {
    return weightExponent;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getBlurWeightSigmaX(),
      getBlurWeightSigmaY(),
      getBlurWeightSigmaZ(),
      getWeightExponent() };
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[]{StackInterfaceContainer.class};
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{TenengradFusedStackInterfaceContainer.class};
  }
}
