package clearcontrol.microscope.lightsheet.smart.dynamicframeinterval;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerInterface;
import clearcontrol.microscope.lightsheet.imaging.AbstractAcquistionScheduler;
import clearcontrol.microscope.lightsheet.imaging.interleaved.AppendConsecutiveInterleavedImagingScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.AppendConsecutiveHyperDriveImagingScheduler;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.AppendConsecutiveOpticsPrefusedImagingScheduler;
import clearcontrol.microscope.lightsheet.postprocessing.containers.SpotCountContainer;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.schedulers.CountsSpotsScheduler;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

import java.util.ArrayList;

/**
 * The ImagingPlanningScheduler looks in the most recent measurements from image stacks and decides if frame interval
 * should be de- or increased.
 *
 *
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ImagingPlanningScheduler extends SchedulerBase implements LoggingFeature {

    LightSheetMicroscope mLightSheetMicroscope;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ImagingPlanningScheduler(LightSheetMicroscope pLightSheetMicroscope) {
        super("Smart: Imaging planner");
        mLightSheetMicroscope = pLightSheetMicroscope;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {

        DataWarehouse lDataWarehouse = mLightSheetMicroscope.getDataWarehouse();

        ArrayList<SpotCountContainer> lSpotcountContainers = lDataWarehouse.getContainers(SpotCountContainer.class);
        double[] lSpotCounts = new double[lSpotcountContainers.size()];
        int i = 0;
        for (SpotCountContainer lContainer : lSpotcountContainers) {
            lSpotCounts[i] = lContainer.getMeasurement();
            i++;
        }

        // Todo: A kalman filter like prediction here would be nice...





        int lNumberOfImages = 10;
        double lFrameIntervalInSeconds = 10;

        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();

        // add myself to the scheduler so that I'll be asked again after next imaging sequence
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();
        schedule.add((int) pTimePoint + 1, this);

        // add another imaging sequence
        appendImagingSequence(pTimePoint, lNumberOfImages, lFrameIntervalInSeconds);
        addSpotDetectionAfterEveryFutureFusion(pTimePoint);


        return true;
    }

    private boolean appendImagingSequence(long pTimePoint, int pNumberOfImages, double pFrameIntervalInSeconds) {
        SchedulerInterface lScheduler = null;
        if (pFrameIntervalInSeconds < 15) {
            lScheduler = new AppendConsecutiveHyperDriveImagingScheduler(pNumberOfImages, pFrameIntervalInSeconds);
        } else if (pFrameIntervalInSeconds < 30) {
            lScheduler = new AppendConsecutiveOpticsPrefusedImagingScheduler(pNumberOfImages, pFrameIntervalInSeconds);
        } else {
            lScheduler = new AppendConsecutiveInterleavedImagingScheduler(pNumberOfImages, pFrameIntervalInSeconds);
        }
        lScheduler.setMicroscope(mLightSheetMicroscope);
        lScheduler.initialize();
        return lScheduler.enqueue(pTimePoint);
    }

    private void addSpotDetectionAfterEveryFutureFusion(long pTimePoint) {
        LightSheetTimelapse lTimelapse = ((LightSheetMicroscope) mMicroscope).getTimelapse();

        // add myself to the scheduler so that I'll be asked again after next imaging sequence
        ArrayList<SchedulerInterface> schedule = lTimelapse.getListOfActivatedSchedulers();
        for (int i = (int)pTimePoint; i < schedule.size() - 1; i++) {
            SchedulerInterface lScheduler = schedule.get(i);
            SchedulerInterface lFollowingScheduler = schedule.get(i + 1);
            if ((lScheduler instanceof AbstractAcquistionScheduler) && (!(lFollowingScheduler instanceof CountsSpotsScheduler))) {
                schedule.add(i + 1, new CountsSpotsScheduler<FusedImageDataContainer>(FusedImageDataContainer.class));
                i++;
            }
        }

    }
}
