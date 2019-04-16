package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.LightSheetTimelapse;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.timelapse.TimelapseInterface;
import clearcontrol.stack.sourcesink.sink.SqeazyFileStackSink;

import java.io.File;

/**
 * WriteAllStacksAsSQYToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2019
 */
public class WriteAllStacksAsSQYToDiscInstruction extends
        WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
    public WriteAllStacksAsSQYToDiscInstruction(Class pContainerClass,
                                                LightSheetMicroscope pLightSheetMicroscope)
    {
        super("IO: Write all stacks in " + pContainerClass.getSimpleName()
                        + " as SQY to disc",
                pContainerClass,
                null,
                null,
                pLightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        LightSheetTimelapse lTimelapse =
                (LightSheetTimelapse) getLightSheetMicroscope().getDevice(TimelapseInterface.class,
                        0);

        fileStackSinkInterface = new SqeazyFileStackSink();

        File file = lTimelapse.getCurrentFileStackSinkVariable()
                .get().getLocation();

        fileStackSinkInterface.setLocation(file.getParentFile(), file.getName());

        return true;

    }

    @Override
    public boolean enqueue(long pTimePoint)
    {
        DataWarehouse lDataWarehouse =
                ((LightSheetMicroscope) getLightSheetMicroscope()).getDataWarehouse();
        StackInterfaceContainer container =
                lDataWarehouse.getOldestContainer(mContainerClass);

        mImageKeys = new String[container.keySet().size()];
        int count = 0;
        for (String key : container.keySet())
        {
            mImageKeys[count] = key;
            count++;
        }

        return super.enqueue(pTimePoint);
    }

    @Override
    public WriteAllStacksAsSQYToDiscInstruction copy()
    {
        return new WriteAllStacksAsSQYToDiscInstruction(mContainerClass,
                getLightSheetMicroscope());
    }

    @Override
    public String getDescription() {
        return "Write image stacks from a given container to disc.";
    }

}
