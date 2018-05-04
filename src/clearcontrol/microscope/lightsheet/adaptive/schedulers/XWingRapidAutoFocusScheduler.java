package clearcontrol.microscope.lightsheet.adaptive.schedulers;

import clearcontrol.microscope.lightsheet.LightSheetDOF;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.state.InterpolatedAcquisitionState;
import clearcontrol.microscope.lightsheet.state.tables.InterpolationTables;

/**
 * The XWingRapidAutoFocusScheduler uses adaptation of the central plane to configure all control planes. This might be
 * good initial starting point for adaptation.
 *
 * Todo: This is XWing specific code and should move to its repository at some point.
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class XWingRapidAutoFocusScheduler extends SchedulerBase {
    private LightSheetMicroscope mLightSheetMicroscope;

    public XWingRapidAutoFocusScheduler() {
        super("Adaptation: XWingScope rapid autofocus Z and alpha");
    }

    @Override
    public boolean initialize() {
        if (mMicroscope instanceof LightSheetMicroscope){
            mLightSheetMicroscope = (LightSheetMicroscope) mMicroscope;
        }
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        InterpolatedAcquisitionState
                lAcquisitionState =
                (InterpolatedAcquisitionState) mLightSheetMicroscope.getAcquisitionStateManager()
                        .getCurrentState();

        //XWing specific
        int lOptimizeCamera0towardsCPI = 5;
        int lOptimizeCamera1towardsCPI = 2;

        ControlPlaneFocusFinderZScheduler lCamera0ZFocusScheduler = new ControlPlaneFocusFinderZScheduler(0, lOptimizeCamera0towardsCPI);
        ControlPlaneFocusFinderZScheduler lCamera1ZFocusScheduler = new ControlPlaneFocusFinderZScheduler(1, lOptimizeCamera1towardsCPI);
        ControlPlaneFocusFinderAlphaByVariationScheduler lCamera0AlphaScheduler = new ControlPlaneFocusFinderAlphaByVariationScheduler(0, lOptimizeCamera0towardsCPI);
        ControlPlaneFocusFinderAlphaByVariationScheduler lCamera1AlphaScheduler = new ControlPlaneFocusFinderAlphaByVariationScheduler(1, lOptimizeCamera1towardsCPI);

        SchedulerInterface[] lSchedulers = new SchedulerInterface[]{
                lCamera0AlphaScheduler,
                lCamera1AlphaScheduler,
                lCamera0ZFocusScheduler,
                lCamera1ZFocusScheduler
        };

        for (SchedulerInterface lScheduler : lSchedulers) {
            lScheduler.setMicroscope(mMicroscope);
            lScheduler.initialize();
            lScheduler.enqueue(pTimePoint);
        }

        // Copy configuration to other control planes
        for (int lLightSheetIndex = 0; lLightSheetIndex < mLightSheetMicroscope.getNumberOfLightSheets(); lLightSheetIndex++) {
            for (int cpi = 0; cpi < lAcquisitionState.getNumberOfControlPlanes(); cpi++) {
                InterpolationTables it = lAcquisitionState.getInterpolationTables();

                int lCopySourceControlPlane = cpi<4?lOptimizeCamera1towardsCPI:lOptimizeCamera0towardsCPI;

                for (LightSheetDOF lDOF : new LightSheetDOF[]{LightSheetDOF.IZ, LightSheetDOF.IA}) {
                    it.set(lDOF, cpi, lLightSheetIndex, it.get(lDOF, lCopySourceControlPlane, lLightSheetIndex));
                }
            }
        }

        return true;
    }
}
