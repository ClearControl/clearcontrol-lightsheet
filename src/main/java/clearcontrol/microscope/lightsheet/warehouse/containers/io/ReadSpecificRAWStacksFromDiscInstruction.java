package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;

import java.io.File;
import java.util.ArrayList;

/**
 * ReadSpecificRAWStacksFromDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 04 2019
 */
public class ReadSpecificRAWStacksFromDiscInstruction extends ReadStackInterfaceContainerFromDiscInstruction {

    private Variable<String> stackNames = new Variable<String>("Stack names (comma separated; empty: all)", "");

    public ReadSpecificRAWStacksFromDiscInstruction(LightSheetMicroscope pLightSheetMicroscope) {
        super("IO: Read RAW stacks from disc", new String[0], pLightSheetMicroscope);
    }

    public Variable<String> getStackNames() {
        return stackNames;
    }

    @Override
    public boolean initialize() {
        String[] array = stackNames.get().split(",");
        if (array.length > 0 && array[0].length() > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] = array[i].trim();
            }
        } else {
            File stacksFolder = new File(getRootFolderVariable().get().getAbsolutePath() + "/stacks/");

            ArrayList<String> availableDatasets = new ArrayList<String>();

            for (File subfolder : stacksFolder.listFiles()) {
                if (subfolder.isDirectory()) {
                    availableDatasets.add(subfolder.getName());
                }
            }
            array = new String[availableDatasets.size()];
            availableDatasets.toArray(array);
        }
        mDatasetNames = array;

        return super.initialize();
    }


    @Override
    public ReadSpecificRAWStacksFromDiscInstruction copy()
    {
        ReadSpecificRAWStacksFromDiscInstruction copied = new ReadSpecificRAWStacksFromDiscInstruction(
                getLightSheetMicroscope());
        copied.mRestartFromBeginningWhenReachingEnd.set(mRestartFromBeginningWhenReachingEnd.get());
        copied.mTimepointOffset.set(mTimepointOffset.get());
        copied.mTimepointStepSize.set(mTimepointStepSize.get());
        copied.mRootFolderVariable.set(mRootFolderVariable.get());
        copied.stackNames.set(stackNames.get());
        return copied;
    }


    @Override
    public Variable[] getProperties()
    {
        return new Variable[]
                {
                        getRestartFromBeginningWhenReachingEnd(),
                        getRootFolderVariable(),
                        getTimepointOffset(),
                        getTimepointStepSize(),
                        getStackNames()
                };
    }
}
