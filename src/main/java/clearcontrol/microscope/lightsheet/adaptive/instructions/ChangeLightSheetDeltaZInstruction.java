package clearcontrol.microscope.lightsheet.adaptive.instructions;

import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.PropertyIOableInstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateManager;

/**
 * ChangeLightSheetDeltaZInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class ChangeLightSheetDeltaZInstruction extends
        LightSheetMicroscopeInstructionBase
        implements
        PropertyIOableInstructionInterface {

    private final BoundedVariable<Integer> mLightSheetIndex;
    private final BoundedVariable<Double> mLightSheetDeltaZ =
            new BoundedVariable<Double>("Light sheet delta Z",
                    0.0,
                    -Double.MAX_VALUE,
                    Double.MAX_VALUE,
                    0.01);

    public ChangeLightSheetDeltaZInstruction(LightSheetMicroscope pLightSheetMicroscope,
                                             int pLightSheetIndex,
                                             double pLightSheetDeltaZ)
    {
        super("Adaptation: Change light sheet delta Z",
                pLightSheetMicroscope);
        mLightSheetDeltaZ.set(pLightSheetDeltaZ);
        mLightSheetIndex =
                new BoundedVariable<Integer>("Light sheet index",
                        pLightSheetIndex,
                        0,
                        pLightSheetMicroscope.getNumberOfLightSheets());
    }

    @Override
    public boolean initialize()
    {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint)
    {
        InterpolatedAcquisitionState lState =
                (InterpolatedAcquisitionState) getLightSheetMicroscope().getDevice(AcquisitionStateManager.class,
                        0)
                        .getCurrentState();
        for (int cpi = 0; cpi < lState.getNumberOfControlPlanes(); cpi++)
        {
            for (int l = 0; l < lState.getNumberOfLightSheets(); l++)
            {
                lState.getInterpolationTables()
                        .set(LightSheetDOF.IZ, cpi, l, mLightSheetDeltaZ.get());
            }
        }
        return true;
    }

    @Override
    public ChangeLightSheetDeltaZInstruction copy()
    {
        return new ChangeLightSheetDeltaZInstruction(getLightSheetMicroscope(),
                mLightSheetIndex.get(),
                mLightSheetDeltaZ.get());
    }

    @Override
    public String getDescription() {
        return "Change delta-Z of a given illumination-arm.";
    }

    public BoundedVariable<Double> getLightSheetDeltaZ()
    {
        return mLightSheetDeltaZ;
    }

    public BoundedVariable<Integer> getLightSheetIndex()
    {
        return mLightSheetIndex;
    }

    @Override
    public Variable[] getProperties()
    {
        return new Variable[]
                { getLightSheetDeltaZ(), getLightSheetIndex() };
    }

    @Override
    public Class[] getProducedContainerClasses() {
        return new Class[0];
    }

    @Override
    public Class[] getConsumedContainerClasses() {
        return new Class[0];
    }
}
