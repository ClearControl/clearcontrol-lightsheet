package clearcontrol.microscope.lightsheet.postprocessing.measurements;

import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.test.TestUtilities;
import clearcontrol.devices.imagej.ImageJFeature;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.postprocessing.containers.DFTContainer;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;
import clearcontrol.stack.OffHeapPlanarStack;
import clearcontrol.stack.StackInterface;
import coremem.ContiguousMemoryInterface;
import coremem.enums.NativeTypeEnum;
import ij.ImageJ;
import ij.ImagePlus;
import org.jtransforms.fft.DoubleFFT_2D;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class DiscreteFourierTransform {

    public void computeDiscreteFourierTransform(long pTimePoint, String key, OffHeapPlanarStack pStack, LightSheetMicroscope pLightSheetMicroscope)
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
        DFTContainer lDftContainer = new DFTContainer(pTimePoint,(int)lDepth,(int)lHeight,(int)lWidth*2,DFTArray);
        pLightSheetMicroscope.getDataWarehouse().put("dft_"+key, lDftContainer);
    }
    public static char[][][]  StackToArray(StackInterface pStack){
        int lDepth = (int)pStack.getDepth();
        int lHeight = (int) pStack.getHeight();
        int lWidth = (int) pStack.getWidth();
        char lImageArray[][][] = new char[lDepth][lHeight][lWidth];

        for (int z = 0; z < lDepth; z++) {
            final ContiguousMemoryInterface lPlaneContiguousMemory =
                    pStack.getContiguousMemory(z);
            int counter = 0;
            for (int y = 0; y < lHeight; y++){
                for (int x = 0; x < lWidth; x++) {
                    char lValue = lPlaneContiguousMemory.getCharAligned(counter);
                    lImageArray[z][y][x] = lValue;
                    counter++;
                }
            }
        }

        return lImageArray;
    }

    public static OffHeapPlanarStack ArrayToStack(char[][][] pArray){
        int lDepth = pArray.length;
        int lHeight = pArray[0].length;
        int lWidth = pArray[0][0].length;
        OffHeapPlanarStack lStack = new
                OffHeapPlanarStack(true,0, NativeTypeEnum.UnsignedShort,1,new long[] {lHeight, lWidth,lDepth});
        final ContiguousMemoryInterface lPlaneContiguousMemory =
                lStack.getContiguousMemory();
        char[] img1Darray = new char[lDepth*lWidth*lHeight];
        for (int z = 0; z < lDepth; z++) {
            for (int y = 0; y < lHeight; y++){
                for (int x = 0; x < lWidth; x++) {
                    img1Darray[getPos(x,lStack.getWidth(),y,lStack.getHeight(),z,lStack.getDepth())] = pArray[z][y][x];
                }
            }
        }
        lPlaneContiguousMemory.copyFrom(img1Darray);
        return lStack;
    }
    public static void main(String ...args){
        OffHeapPlanarStack lStack = new OffHeapPlanarStack(true,0, NativeTypeEnum.UnsignedShort,1,new long[] {10,10,10});
        for (int z = 0; z < lStack.getDepth(); z++) {
            for (int y = 0; y < lStack.getHeight(); y++) {
                for (int x = 0; x < lStack.getWidth(); x++) {
                    lStack.getContiguousMemory().setCharAligned(getPos(x,lStack.getWidth(),y,lStack.getHeight(),z,lStack.getDepth()), (char) x);
                }
            }
        }
        new ImageJ();
        ClearCLIJ.getInstance().show(lStack,"CreatedStack");
        char arr[][][] = StackToArray(lStack);
        StackInterface lStack1 = ArrayToStack(arr);
        char arr1[][][] = StackToArray(lStack1);
        ClearCLIJ clij = ClearCLIJ.getInstance();
        //clij.show(lStack1,"CheckStack");
        ImagePlus img = clij.converter(lStack).getImagePlus();
        ImagePlus img1 = clij.converter(lStack1).getImagePlus();
        assertTrue(TestUtilities.compareImages(img,img1));
        System.exit(0);
    }
    public static int getPos(int x, long pWidth, int y, long pHeight, int z, long pDepth){
        return z*((int)pHeight*(int)pWidth)+y*(int)pWidth+x;
    }


}

