package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcl.imagej.ClearCLIJ;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.BasicStageInterface;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.stack.StackInterface;
import ij.ImagePlus;
import ij.process.ImageStatistics;

public class CenterSampleInZInstruction extends
                                        LightSheetMicroscopeInstructionBase
                                        implements
                                        InstructionInterface,
                                        LoggingFeature,
                                        PropertyIOableInstructionInterface
{

  private InterpolatedAcquisitionState mInterpolatedAcquisitionState;

  // in wich range might the sample be located?
  BoundedVariable<Double> mSearchRangeMinZVariable =
                                                   new BoundedVariable<Double>("search range start",
                                                                               10.0,
                                                                               -Double.MAX_VALUE,
                                                                               Double.MAX_VALUE,
                                                                               0.001);
  BoundedVariable<Double> mSearchRangeMaxZVariable =
                                                   new BoundedVariable<Double>("search range end",
                                                                               12.0,
                                                                               -Double.MAX_VALUE,
                                                                               Double.MAX_VALUE,
                                                                               0.001);

  BoundedVariable<Double> mSearchRangeZVariable =
                                                new BoundedVariable<Double>("search range",
                                                                            2.0,
                                                                            -Double.MAX_VALUE,
                                                                            Double.MAX_VALUE,
                                                                            0.001);

  // how many images in this range should I take?
  BoundedVariable<Integer> mNumberOfStepsVariable =
                                                  new BoundedVariable<Integer>("number of steps",
                                                                               30,
                                                                               -Integer.MAX_VALUE,
                                                                               Integer.MAX_VALUE,
                                                                               1);

  public CenterSampleInZInstruction(LightSheetMicroscope pLightSheetMicroscope)
  {
    super("Smart: Center sample in Z", pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    mInterpolatedAcquisitionState =
                                  (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                                                                                          .getCurrentState();

    double pCenterFOV =
                      (mInterpolatedAcquisitionState.getStackZHighVariable()
                                                    .get()
                                                    .doubleValue()
                       + mInterpolatedAcquisitionState.getStackZLowVariable()
                                                      .get()
                                                      .doubleValue())
                        / 2.0;

    mSearchRangeMinZVariable.set(pCenterFOV
                                 - mSearchRangeZVariable.get() / 2.0);
    mSearchRangeMinZVariable.set(pCenterFOV
                                 + mSearchRangeZVariable.get() / 2.0);

    return true;
  }

  @Override
  public boolean enqueue(long l)
  {

    ClearCLIJ clij = ClearCLIJ.getInstance();

    BasicStageInterface lStageZ = null;

    for (BasicStageInterface lStage : getLightSheetMicroscope().getDevices(BasicStageInterface.class))
    {
      if (lStage.toString().contains("Z"))
      {
        lStageZ = lStage;
      }
    }
    if (lStageZ == null)
    {
      warning("did not find stage interface!");
      return false;
    }

    double lZ = (mInterpolatedAcquisitionState.getStackZLowVariable()
                                              .get()
                                              .doubleValue()
                 + mInterpolatedAcquisitionState.getStackZHighVariable()
                                                .get()
                                                .doubleValue())
                / 2;

    SingleViewPlaneImager imager =
                                 new SingleViewPlaneImager(getLightSheetMicroscope(),
                                                           (int) lZ);
    imager.setImageHeight(2048);
    imager.setImageWidth(2048);
    imager.setExposureTimeInSeconds(0.02);
    imager.setLightSheetIndex(0);
    imager.setDetectionArmIndex(0);

    // acquire an image
    StackInterface acquiredImageStack = imager.acquire();
    // clij.show(acquiredImageStack, "before test shift");

    // search for the plane with maximum signal
    double stepSize = (mSearchRangeMaxZVariable.get()
                       - mSearchRangeMinZVariable.get())
                      / mNumberOfStepsVariable.get();
    double maxValue = 0;
    double bestPositionZ = lStageZ.getPositionVariable().get();

    for (int i = 0; i < mNumberOfStepsVariable.get(); i++)
    {

      // move stage in Z
      double z = mSearchRangeMinZVariable.get() + i * stepSize;
      lStageZ.moveBy(z - lStageZ.getPositionVariable().get(), true);

      // take an image
      acquiredImageStack = imager.acquire();
      // clij.show(acquiredImageStack, "image on position " + z);
      ImagePlus imp =
                    clij.converter(acquiredImageStack).getImagePlus();

      // analyse it
      ImageStatistics stats = imp.getStatistics();

      info("Z: " + z);
      info("mean: " + (stats.mean));
      info("max: " + (stats.max));
      info("min: " + (stats.min));

      if (stats.mean > maxValue)
      {
        maxValue = stats.mean;
        bestPositionZ = z;
      }
    }

    // move to the determined position
    lStageZ.moveBy(bestPositionZ
                   - lStageZ.getPositionVariable().get(), true);

    // acquiredImageStack = imager.acquire();
    // clij.show(acquiredImageStack, "image on best position " + bestPositionZ);

    return true;
  }

  public BoundedVariable<Double> getSearchRangeMinZVariable()
  {
    return mSearchRangeMinZVariable;
  }

  public BoundedVariable<Double> getSearchRangeMaxZVariable()
  {
    return mSearchRangeMaxZVariable;
  }

  public BoundedVariable<Double> getSearchRangeZVariable()
  {
    return mSearchRangeZVariable;
  }

  public BoundedVariable<Integer> getNumberOfStepsVariable()
  {
    return mNumberOfStepsVariable;
  }

  @Override
  public CenterSampleInZInstruction copy()
  {
    CenterSampleInZInstruction copied =
                                      new CenterSampleInZInstruction(getLightSheetMicroscope());
    copied.mNumberOfStepsVariable.set(mNumberOfStepsVariable.get());
    copied.mSearchRangeMaxZVariable.set(mSearchRangeMaxZVariable.get());
    copied.mSearchRangeMinZVariable.set(mSearchRangeMinZVariable.get());
    copied.mSearchRangeZVariable.set(mSearchRangeZVariable.get());
    return copied;
  }

  @Override
  public Variable[] getProperties()
  {
    return new Variable[]
    { getNumberOfStepsVariable(),
      getSearchRangeMaxZVariable(),
      getSearchRangeMinZVariable(),
      getSearchRangeZVariable() };
  }
}
