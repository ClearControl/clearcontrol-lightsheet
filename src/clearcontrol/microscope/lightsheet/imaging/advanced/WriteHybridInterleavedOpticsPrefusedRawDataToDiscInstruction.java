package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;
import clearcontrol.microscope.lightsheet.imaging.interleaved.WriteInterleavedRawDataToDiscInstruction;
import clearcontrol.microscope.lightsheet.warehouse.containers.io.WriteStackInterfaceContainerAsRawToDiscInstructionBase;

/**
 * WriteHybridInterleavedOpticsPrefusedRawDataToDiscInstruction
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 08 2018
 */
public class WriteHybridInterleavedOpticsPrefusedRawDataToDiscInstruction  extends
        WriteStackInterfaceContainerAsRawToDiscInstructionBase
{
    public WriteHybridInterleavedOpticsPrefusedRawDataToDiscInstruction(LightSheetMicroscope pLightSheetMicroscope)
    {
        super("IO: Write hybrid interleaved/optics prefused raw data to disc", HybridInterleavedOpticsPrefusedImageDataContainer.class, listKeys(pLightSheetMicroscope.getNumberOfDetectionArms()), null, pLightSheetMicroscope);
    }

    private static String[] listKeys(int pNumberOfDetectionArms) {
        String[] result = new String[pNumberOfDetectionArms];
        for (int d = 0; d < pNumberOfDetectionArms; d++) {
            result[d] = "C" + d + "hybrid_interleaved_opticsprefused";
        }
        return result;
    }

    @Override
    public WriteHybridInterleavedOpticsPrefusedRawDataToDiscInstruction copy() {
        return new WriteHybridInterleavedOpticsPrefusedRawDataToDiscInstruction(getLightSheetMicroscope());
    }
}

