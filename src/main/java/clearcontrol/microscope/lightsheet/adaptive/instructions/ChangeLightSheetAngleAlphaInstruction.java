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
 * ChangeLightSheetAngleAlphaInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 11 2018
 */
public class ChangeLightSheetAngleAlphaInstruction extends
        LightSheetMicroscopeInstructionBase
        implements
        PropertyIOableInstructionInterface {

    private final BoundedVariable<Integer> mLightSheetIndex;
    private final BoundedVariable<Double> mLightSheetAngle =
            new BoundedVariable<Double>("Light sheet angle",
                    0.0,
                    -Double.MAX_VALUE,
                    Double.MAX_VALUE,
                    0.01);

    public ChangeLightSheetAngleAlphaInstruction(LightSheetMicroscope pLightSheetMicroscope,
                                                 int pLightSheetIndex,
                                                 double pLightSheetAngle)
    {
        super("Adaptation: Change light sheet angle",
                pLightSheetMicroscope);
        mLightSheetAngle.set(pLightSheetAngle);
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
                        .set(LightSheetDOF.IA, cpi, l, mLightSheetAngle.get());
            }
        }
        return true;
    }

    @Override
    public ChangeLightSheetAngleAlphaInstruction copy()
    {
        return new ChangeLightSheetAngleAlphaInstruction(getLightSheetMicroscope(),
                mLightSheetIndex.get(),
                mLightSheetAngle.get());
    }

    @Override
    public String getDescription() {
        return "Change alpha of a given illumination-arm to a given value.";
    }

    public BoundedVariable<Double> getLightSheetAngleAlpha()
    {
        return mLightSheetAngle;
    }

    public BoundedVariable<Integer> getLightSheetIndex()
    {
        return mLightSheetIndex;
    }

    @Override
    public Variable[] getProperties()
    {
        return new Variable[]
                { getLightSheetAngleAlpha(), getLightSheetIndex() };
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
