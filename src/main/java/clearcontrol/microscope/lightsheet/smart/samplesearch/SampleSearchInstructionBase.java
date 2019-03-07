package clearcontrol.microscope.lightsheet.smart.samplesearch;

import java.util.ArrayList;

import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.SingleViewPlaneImager;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.stack.StackInterface;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLImage;

/**
 * The SampleSearchInstructionBase serves as base for all instructions
 * which search for samples in space. It contains utility methods such
 * as deriving mean signal intensities from images from a list of positions
 * where images should be taken
 *
 * Author: @haesleinhuepf
 * May 2018
 */
public abstract class SampleSearchInstructionBase extends
                                                  LightSheetMicroscopeInstructionBase
{

  protected BoundedVariable<Double> stepSizeInMillimetersVariable =
                                                                   new BoundedVariable<Double>("Step size in mm",
                                                                                               0.25,
                                                                                               0.01,
                                                                                               Double.MAX_VALUE,
                                                                                               0.001);
  protected SpaceTravelInstruction sampleCandidates;

  public SampleSearchInstructionBase(String pDeviceName,
                                     LightSheetMicroscope pLightSheetMicroscope)
  {
    super(pDeviceName, pLightSheetMicroscope);
  }

  @Override
  public boolean initialize()
  {
    sampleCandidates =
                      new SpaceTravelInstruction(getLightSheetMicroscope());
    sampleCandidates.initialize();
    return true;
  }

  public BoundedVariable<Double> getStepSizeInMillimetersVariable()
  {
    return stepSizeInMillimetersVariable;
  }

  protected ArrayList<Double> measureAverageSignalIntensityAtCandiatePositions()
  {
    ArrayList<Position> lSampleCandidatePositionList =
                                                     sampleCandidates.getTravelPathList();

    ArrayList<Double> averageSignalMeasurements = new ArrayList<Double>();

    InterpolatedAcquisitionState acquisitionState =
                                        (InterpolatedAcquisitionState) getLightSheetMicroscope().getAcquisitionStateManager()
                                                                                                .getCurrentState();

    for (int i = 0; i < lSampleCandidatePositionList.size(); i++)
    {
      sampleCandidates.goToPosition(i);
      SingleViewPlaneImager lImager =
                                    new SingleViewPlaneImager(getLightSheetMicroscope(),
                                                              (acquisitionState.getStackZLowVariable()
                                                                     .getMin()
                                                                     .doubleValue()
                                                               + acquisitionState.getStackZHighVariable()
                                                                       .getMax()
                                                                       .doubleValue()) / 2.0);
      lImager.setImageHeight(acquisitionState.getImageWidthVariable()
                                   .get()
                                   .intValue());
      lImager.setImageHeight(acquisitionState.getImageHeightVariable()
                                   .get()
                                   .intValue());
      lImager.setExposureTimeInSeconds(acquisitionState.getExposureInSecondsVariable()
                                             .get()
                                             .doubleValue());
      StackInterface lStack = lImager.acquireStack();

      CLIJ clij = CLIJ.getInstance();
      ClearCLImage clImage =
                            clij.convert(lStack, ClearCLImage.class);
      double sum = clij.op().sumPixels(clImage);
      clImage.close();
      double average = sum / (acquisitionState.getImageWidthVariable()
                                    .get()
                                    .intValue()
                              * acquisitionState.getImageHeightVariable()
                                      .get()
                                      .intValue());
      averageSignalMeasurements.add(average);
    }

    return averageSignalMeasurements;
  }
}
