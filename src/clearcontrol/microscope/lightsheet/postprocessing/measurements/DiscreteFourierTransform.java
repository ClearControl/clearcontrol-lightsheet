package clearcontrol.microscope.lightsheet.postprocessing.measurements;

import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DFTContainer;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.stack.OffHeapPlanarStack;
import coremem.ContiguousMemoryInterface;
import org.jtransforms.fft.DoubleFFT_2D;

import java.util.Arrays;

public class DiscreteFourierTransform {

    public void computeDiscreteFourierTransform(OffHeapPlanarStack pStack, LightSheetMicroscope pLightSheetMicroscope)
    {
        long lWidth = pStack.getWidth();
        long lHeight = pStack.getHeight();
        long lDepth = pStack.getDepth();
        DoubleFFT_2D fft = new DoubleFFT_2D((int)lHeight, (int)lWidth);


        double DFTArray[][][] = new double[(int)lDepth][(int)lHeight][(int)lWidth * 2];
        for (int z = 0; z < lDepth; z++)
        {
            double img_2D[][] = new double[(int)lHeight][(int)lWidth];
            double input_2D[][] = new double[(int)lHeight][(int)lWidth*2];
            final ContiguousMemoryInterface lPlaneContiguousMemory =
                    pStack.getContiguousMemory(z);

            int counter = 0;
            for (int i = 0; i<lWidth;i++){
                for (int j = 0; j < lHeight; j++){
                    double lValue = lPlaneContiguousMemory.getCharAligned(counter);
                    img_2D[j][i] = lValue;
                    input_2D[j][i] = lValue;
                    counter++;
                }
            }
            fft.realForwardFull(input_2D);
            //Checked
            //fft.complexInverse(input_2D,true);
            DFTArray[z] = input_2D;
        }
//        DFTContainer lDftContainer = new DFTContainer(0);
//        lDftContainer.
//        pLightSheetMicroscope.getDataWarehouse().put("dft", lDftContainer)
    }



}

