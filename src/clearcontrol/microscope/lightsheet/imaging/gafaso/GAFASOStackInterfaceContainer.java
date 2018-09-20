package clearcontrol.microscope.lightsheet.imaging.gafaso;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.imaging.interleaved.InterleavedImageDataContainer;

/**
 * The GAFASOStackInterfaceContainer is meant to hold a single stack acquired
 * by the GAFASOAcquisitionInstruction
 *
 * Author: @haesleinhuepf
 * September 2018
 */
public class GAFASOStackInterfaceContainer extends InterleavedImageDataContainer {
    private int numberOfPositions;

    public GAFASOStackInterfaceContainer(LightSheetMicroscope pLightSheetMicroscope, int numberOfPositions) {
        super(pLightSheetMicroscope);

        this.numberOfPositions = numberOfPositions;
    }

    public int getNumberOfPositions() {
        return numberOfPositions;
    }
}
