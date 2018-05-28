package clearcontrol.microscope.lightsheet.warehouse.containers.io;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.opticsprefused.OpticsPrefusedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.sequential.SequentialImageDataContainer;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.microscope.stacks.StackRecyclerManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import clearcontrol.stack.sourcesink.source.RawFileStackSource;
import coremem.recycling.RecyclerInterface;

import java.io.File;
import java.util.Arrays;

/**
 * ReadStackInterfaceContainerFromDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class ReadStackInterfaceContainerFromDiscInstruction extends LightSheetMicroscopeInstructionBase implements LoggingFeature {

    String[] mDatasetNames;


    private Variable<File> mRootFolderVariable =
            new Variable("RootFolder",
                    (Object) null);

    private long mReadTimePoint = 0;

    /**
     * INstanciates a virtual device with a given name
     *
     */
    public ReadStackInterfaceContainerFromDiscInstruction(String[] pDatasetNames, LightSheetMicroscope pLightSheetMicroscope) {
        super("IO: Read stacks from disc " + Arrays.toString(pDatasetNames), pLightSheetMicroscope);
        mDatasetNames = pDatasetNames;
    }

    @Override
    public boolean initialize() {
        mReadTimePoint = 0;
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        File lRootFolder = getRootFolderVariable().get();

        String lDatasetname = lRootFolder.getName();

        lRootFolder = lRootFolder.getParentFile();

        StackInterfaceContainer lContainer;
        String lContainerWarehouseKey;
        if (mDatasetNames.length == 1 && (
                mDatasetNames[0].contains("fused") ||
                        mDatasetNames[0].contains("sequential") ||
                        mDatasetNames[0].contains("interleaved") ||
                        mDatasetNames[0].contains("opticsprefused") ||
                        mDatasetNames[0].contains("default")
        )) {
            lContainer = new FusedImageDataContainer(pTimePoint);
            lContainerWarehouseKey = "fused_" + pTimePoint;
        } else if (mDatasetNames[0].contains("opticsprefused")) {
            lContainer = new OpticsPrefusedImageDataContainer(getLightSheetMicroscope());
            lContainerWarehouseKey = "opticsprefused_raw_" + pTimePoint;
        } else if (mDatasetNames[0].contains("interleaved")) {
            lContainer = new InterleavedImageDataContainer(getLightSheetMicroscope());
            lContainerWarehouseKey = "interleaved_raw_" + pTimePoint;
        } else {
            lContainer = new SequentialImageDataContainer(getLightSheetMicroscope());
            lContainerWarehouseKey = "sequential_raw_" + pTimePoint;
        }


        StackRecyclerManager
                lStackRecyclerManager = getLightSheetMicroscope().getDevice(StackRecyclerManager.class, 0);
        RecyclerInterface<StackInterface, StackRequest>
                lRecycler = lStackRecyclerManager.getRecycler("warehouse",
                1024,
                1024);

/*
        BasicRecycler<StackInterface, StackRequest> lRecycler =
                new BasicRecycler(new ContiguousOffHeapPlanarStackFactory(),
                        10,
                        10,
                        true);
*/
        RawFileStackSource rawFileStackSource =
                new RawFileStackSource(lRecycler);
        rawFileStackSource.setLocation(lRootFolder, lDatasetname);
        for (int i = 0; i < mDatasetNames.length; i++) {
            info("getting" + mDatasetNames[i]);
            StackInterface stack =
                    rawFileStackSource.getStack(mDatasetNames[i],
                            mReadTimePoint);
            if (stack == null) {
                mReadTimePoint = 0;
                stack =
                        rawFileStackSource.getStack(mDatasetNames[i],
                                mReadTimePoint);
            }

            if (stack == null) {
                warning("Error: could not load file " + lRootFolder + " " + lDatasetname + " " + mDatasetNames[i] + "!");
                return false;
            }

            lContainer.put(mDatasetNames[i], stack);
            mReadTimePoint ++;
        }
        getLightSheetMicroscope().getDataWarehouse().put(lContainerWarehouseKey, lContainer);

        return false;
    }


    public Variable<File> getRootFolderVariable()
    {
        return mRootFolderVariable;
    }

    @Override
    public ReadStackInterfaceContainerFromDiscInstruction copy() {
        return new ReadStackInterfaceContainerFromDiscInstruction(mDatasetNames, getLightSheetMicroscope());
    }
}