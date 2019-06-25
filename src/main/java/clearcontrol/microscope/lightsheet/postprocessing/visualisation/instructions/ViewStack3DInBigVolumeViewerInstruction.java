package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import bdv.tools.brightness.ConverterSetup;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvOptions;
import bvv.util.BvvStackSource;
import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.TimeStampContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.CLIJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;

import java.time.Duration;

/**
 * ViewStack3DInBigDataViewerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ViewStack3DInBigVolumeViewerInstruction<T extends StackInterfaceContainer, P extends RealType<P>>
                                                  extends
                                                  ViewStackInstructionBase<T>
                                                  implements
                                                  LoggingFeature
{

  private static Bvv bvv = null;
  private RandomAccessibleInterval<P> rai = null;

  public BoundedVariable<Double> min = new BoundedVariable<Double>("min", 100.0, 1.0, 100000.0);
  public BoundedVariable<Double> max = new BoundedVariable<Double>("max", 1000.0, 1.0, 100000.0);

  public ViewStack3DInBigVolumeViewerInstruction(Class<T> pTargetContainerClass,
                                                 LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Visualisation: View stack '"
          + pTargetContainerClass.getSimpleName()
          + "' in BigVolumeViewer",
          pTargetContainerClass,
          pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    resetBigDataViewer();
    return true;
  }

  @Override
  public boolean enqueue(long pTimePoint)
  {

    T lContainer =
                 getLightSheetMicroscope().getDataWarehouse()
                                          .getOldestContainer(getStackInterfaceContainerClass());
    StackInterface stack = getImageFromContainer(lContainer);

    CLIJ clij = CLIJ.getInstance();

    RandomAccessibleInterval<P> newRai = clij.convert(stack, RandomAccessibleInterval.class);

    if (rai == null || rai.dimension(0) != newRai.dimension(0)
        || rai.dimension(1) != newRai.dimension(1)
        || rai.dimension(2) != newRai.dimension(2))
    {
      if (bvv != null)
      {
        info("resarting BVV");
        info("x: " + rai.dimension(0) + " " + newRai.dimension(0));
        info("y: " + rai.dimension(1) + " " + newRai.dimension(1));
        info("z: " + rai.dimension(2) + " " + newRai.dimension(2));
      }
      resetBigDataViewer();
      rai = newRai;
    }
    else
    {
      ElapsedTime.sStandardOutput = true;
      ElapsedTime.measure("conversion for BVV", () -> {
        LoopBuilder.setImages(rai, newRai)
                   .forEachPixel((result, back) -> {
                     result.set(back);
                   });
      });
    }

    BvvOptions options = BvvOptions.options();
    if (stack.getMetaData() != null)
    {
      options.sourceTransform(stack.getMetaData().getVoxelDimX(),
                              stack.getMetaData().getVoxelDimY(),
                              stack.getMetaData().getVoxelDimZ());
    }

    TimeStampContainer lStartTimeInNanoSecondsContainer =
                                                        TimeStampContainer.getGlobalTimeSinceStart(getLightSheetMicroscope().getDataWarehouse(),
                                                                                                   pTimePoint,
                                                                                                   stack);

    Duration duration =
                      Duration.ofNanos(stack.getMetaData()
                                            .getTimeStampInNanoseconds()
                                       - lStartTimeInNanoSecondsContainer.getTimeStampInNanoSeconds());
    long s = duration.getSeconds();
    String title = String.format("%d:%02d:%02d",
                                 s / 3600,
                                 (s % 3600) / 60,
                                 (s % 60));

    if (bvv == null)
    {
      bvv = BvvFunctions.show(rai, title, options);
      ConverterSetup converterSetup = bvv.getBvvHandle()
                                         .getSetupAssignments()
                                         .getConverterSetups()
                                         .get(0);
      converterSetup.setDisplayRange(100, 2000);
    }
    else
    {
      ((BvvStackSource)bvv).invalidate();
      bvv.getBvvHandle().getViewerPanel().paint();
      bvv.getBvvHandle().getViewerPanel().setName(title);
    }
    System.out.println(title);

    return true;
  }

  @Override
  public InstructionInterface copy()
  {
    return new ViewStack3DInBigVolumeViewerInstruction<T, P>(getStackInterfaceContainerClass(),
                                                           getLightSheetMicroscope());
  }

  @Override
  public String getDescription() {
    return "View a stack from a given container in the BigDataViewer.";
  }

  public void resetBigDataViewer()
  {
    if (bvv != null)
    {
      bvv.close();
    }
    bvv = null;
    rai = null;
  }

  @Override
  public Class[] getProducedContainerClasses() {
    return new Class[0];
  }

  @Override
  public Class[] getConsumedContainerClasses() {
    return new Class[]{StackInterfaceContainer.class};
  }
}
