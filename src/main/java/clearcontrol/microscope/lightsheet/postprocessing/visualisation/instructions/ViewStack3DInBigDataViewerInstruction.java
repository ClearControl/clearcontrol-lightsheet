package clearcontrol.microscope.lightsheet.postprocessing.visualisation.instructions;

import java.time.Duration;

import net.haesleinhuepf.clij.CLIJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.RealType;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import clearcontrol.core.concurrent.timing.ElapsedTime;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.TimeStampContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.StackInterface;

/**
 * ViewStack3DInBigDataViewerInstructionPanel
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
 */
public class ViewStack3DInBigDataViewerInstruction<T extends StackInterfaceContainer, P extends RealType<P>>
                                                  extends
                                                  ViewStackInstructionBase<T>
                                                  implements
                                                  LoggingFeature
{

  private static Bdv bdv = null;
  private RandomAccessibleInterval<P> rai = null;

  public ViewStack3DInBigDataViewerInstruction(Class<T> pTargetContainerClass,
                                               LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Visualisation: View stack '"
          + pTargetContainerClass.getSimpleName()
          + "' in BigDataViewer",
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
      if (bdv != null)
      {
        info("resarting BDV");
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
      ElapsedTime.measure("conversion for BDV", () -> {
        LoopBuilder.setImages(rai, newRai)
                   .forEachPixel((result, back) -> {
                     result.set(back);
                   });
      });
    }

    BdvOptions options = BdvOptions.options();
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

    if (bdv == null)
    {
      bdv = BdvFunctions.show(rai, title, options);
      ConverterSetup converterSetup = bdv.getBdvHandle()
                                         .getSetupAssignments()
                                         .getConverterSetups()
                                         .get(0);
      converterSetup.setDisplayRange(100, 1000);
    }
    else
    {
      bdv.getBdvHandle().getViewerPanel().paint();
      bdv.getBdvHandle().getViewerPanel().setName(title);
    }
    System.out.println(title);

    return true;
  }

  @Override
  public InstructionInterface copy()
  {
    return new ViewStack3DInBigDataViewerInstruction<T, P>(getStackInterfaceContainerClass(),
                                                           getLightSheetMicroscope());
  }

  public void resetBigDataViewer()
  {
    if (bdv != null)
    {
      bdv.close();
    }
    bdv = null;
    rai = null;
  }
}
