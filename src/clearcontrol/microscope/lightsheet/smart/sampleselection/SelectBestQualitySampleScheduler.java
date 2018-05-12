package clearcontrol.microscope.lightsheet.smart.sampleselection;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.adaptive.schedulers.SpaceTravelScheduler;
import clearcontrol.microscope.lightsheet.component.scheduler.SchedulerBase;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DCTS2DContainer;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;

import java.util.ArrayList;

/**
 * SelectBestQualitySampleScheduler
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class SelectBestQualitySampleScheduler extends SchedulerBase implements LoggingFeature {

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public SelectBestQualitySampleScheduler() {
        super("Smart: Select best quality sample (spatial position with maximum DCTS2D)");
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        if (!(mMicroscope instanceof LightSheetMicroscope)) {
            warning("I need a LightSheetMicroscope!");
            return false;
        }

        LightSheetMicroscope lLightSheetMicroscope = (LightSheetMicroscope)mMicroscope;
        DataWarehouse lDataWarehouse = lLightSheetMicroscope.getDataWarehouse();

        ArrayList<DCTS2DContainer> lQualityInSpaceList = lDataWarehouse.getContainers(DCTS2DContainer.class);

        if (lQualityInSpaceList.size() == 0) {
            warning("No measurements found. Measure DCTS2D before asking me where the best sample is.");
            return false;
        }

        DCTS2DContainer lMaxmimumQualityContainer = lQualityInSpaceList.get(0);
        for (DCTS2DContainer lContainer : lQualityInSpaceList) {
            if (lContainer.getMeasurement() > lMaxmimumQualityContainer.getMeasurement()) {
                lMaxmimumQualityContainer = lContainer;
            }
        }

        info("Best position was " + lMaxmimumQualityContainer.getX() + "/"  + lMaxmimumQualityContainer.getY() + "/"  + lMaxmimumQualityContainer.getZ() + " (DCTS2D = " + lMaxmimumQualityContainer.getMeasurement() + ")");

        SpaceTravelScheduler lSpaceTravelScheduler = lLightSheetMicroscope.getDevice(SpaceTravelScheduler.class, 0);
        ArrayList<Position> lPositionList = lSpaceTravelScheduler.getTravelPathList();
        lPositionList.clear();
        lPositionList.add(new Position(lMaxmimumQualityContainer.getX(), lMaxmimumQualityContainer.getY(), lMaxmimumQualityContainer.getZ()));

        return true;
    }
}