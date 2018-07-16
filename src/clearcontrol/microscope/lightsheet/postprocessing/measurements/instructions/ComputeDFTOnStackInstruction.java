package clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.DiscreteFourierTransform;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;

public class ComputeDFTOnStackInstruction <T extends StackInterfaceContainer> extends LightSheetMicroscopeInstructionBase implements LoggingFeature {
    private final Class<T> mClass;

    public ComputeDFTOnStackInstruction(Class<T> pClass, LightSheetMicroscope pLightSheetMicroscope) {
        super("Post-processing: DFT on stack " + pClass.getSimpleName(), pLightSheetMicroscope);
        mClass = pClass;
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();

        T lContainer = lDataWarehouse.getOldestContainer(mClass);

        String key = lContainer.keySet().iterator().next();
        StackInterface lStack = lContainer.get(key);

        DiscreteFourierTransform lDFT = new DiscreteFourierTransform();
        lDFT.computeDiscreteFourierTransform(pTimePoint, key, (OffHeapPlanarStack) lStack, getLightSheetMicroscope());

        return true;
    }

    @Override
    public ComputeDFTOnStackInstruction copy() {
        return new ComputeDFTOnStackInstruction(mClass, getLightSheetMicroscope());
    }

}
