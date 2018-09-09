package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator.demo;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.SolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator.ActuatorSolution;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm.implementations.actuator.ActuatorSolutionFactory;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.SpatialPhaseModulatorDeviceInterface;

import org.ejml.data.DenseMatrix64F;

/**
 * The ActuatorDemoSolutionFactory allows to create ZernikeDemoSolutions which
 * don't need a real microscope to calculate fitness.
 *
 * Author: @haesleinhuepf 04 2018
 */
public class ActuatorDemoSolutionFactory implements
                                         SolutionFactory<ActuatorDemoSolution>
{
  private final SpatialPhaseModulatorDeviceInterface mSpatialPhaseModulatorDeviceInterface;
  private final LightSheetMicroscope mLightSheetMicroscope;
  private final double mPositionZ;
  private final DenseMatrix64F mReferenceSolutionMatrix;
  private ActuatorSolutionFactory mFactory;

  public ActuatorDemoSolutionFactory(LightSheetMicroscope pLightSheetMicroscope,
                                     SpatialPhaseModulatorDeviceInterface pSpatialPhaseModulatorDeviceInterface,
                                     double pPositionZ,
                                     DenseMatrix64F pReferenceSolutionMatrix)
  {

    mFactory = new ActuatorSolutionFactory(pLightSheetMicroscope,
                                           pSpatialPhaseModulatorDeviceInterface,
                                           pPositionZ);
    mSpatialPhaseModulatorDeviceInterface =
                                          pSpatialPhaseModulatorDeviceInterface;
    mLightSheetMicroscope = pLightSheetMicroscope;
    mPositionZ = pPositionZ;
    mReferenceSolutionMatrix = pReferenceSolutionMatrix;
  }

  @Override
  public ActuatorDemoSolution random()
  {
    ActuatorSolution lSolution = mFactory.random();

    return new ActuatorDemoSolution(lSolution.getMatrix(),
                                    mLightSheetMicroscope,
                                    mSpatialPhaseModulatorDeviceInterface,
                                    mPositionZ,
                                    mReferenceSolutionMatrix);
  }

  @Override
  public ActuatorDemoSolution crossover(ActuatorDemoSolution pSolution1,
                                        ActuatorDemoSolution pSolution2)
  {
    ActuatorSolution lSolution = mFactory.crossover(pSolution1,
                                                    pSolution2);

    return new ActuatorDemoSolution(lSolution.getMatrix(),
                                    mLightSheetMicroscope,
                                    mSpatialPhaseModulatorDeviceInterface,
                                    mPositionZ,
                                    mReferenceSolutionMatrix);
  }
}
