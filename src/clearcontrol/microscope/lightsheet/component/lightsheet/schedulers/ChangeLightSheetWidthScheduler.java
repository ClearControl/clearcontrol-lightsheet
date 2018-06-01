package clearcontrol.microscope.lightsheet.component.lightsheet.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.state.AcquisitionStateInterface;
import clearcontrol.microscope.state.AcquisitionStateManager;

/**
 * ChangeLightSheetWidthScheduler allows controlling the irises in the illumination arms
 *
 * XWing specifig:
 * * All irises are controlled together
 * * Value 0 corresponds to an open iris
 * * Value 0.45 corresponds to an almost closed iris
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ChangeLightSheetWidthScheduler extends SchedulerBase {


    private final LightSheetMicroscope mLightSheetMicroscope;
    private final double mLightSheetWidth;

    public ChangeLightSheetWidthScheduler(LightSheetMicroscope pLightSheetMicroscope, double pLightSheetWidth) {
        super("Adaptation: Change light sheet width to " + pLightSheetWidth);
        mLightSheetMicroscope = pLightSheetMicroscope;
        mLightSheetWidth = pLightSheetWidth;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        InterpolatedAcquisitionState lState = (InterpolatedAcquisitionState) mLightSheetMicroscope.getDevice(AcquisitionStateManager.class, 0).getCurrentState();
        for (int cpi = 0; cpi < lState.getNumberOfControlPlanes(); cpi++) {
            for (int l = 0; l < lState.getNumberOfLightSheets(); l++) {
                lState.getInterpolationTables().set(LightSheetDOF.IW, cpi, l, mLightSheetWidth);
            }
        }
        return true;
    }
}