package clearcontrol.microscope.lightsheet.imaging.advanced;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import clearcl.imagej.ClearCLIJ;
import clearcl.imagej.kernels.Kernels;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.warehouse.DataWarehouse;
import ij.IJ;
import ij.ImagePlus;

public class HybridInterleavedOpticsPrefusedSingleCameraFusion extends LightSheetMicroscopeInstructionBase {
    private final static int digits = 6;

    BoundedVariable<Integer> mCameraIndexVariable;


    private HybridInterleavedOpticsPrefusedSingleCameraFusion(LightSheetMicroscope lightSheetMicroscope) {
        super("Post-processing: Hybrid interleaved/opticsprefused fusion + saving", lightSheetMicroscope);
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        /*DataWarehouse lDataWarehouse = getLightSheetMicroscope().getDataWarehouse();
        final HybridInterleavedOpticsPrefusedImageDataContainer
                lContainer = lDataWarehouse.getOldestContainer(HybridInterleavedOpticsPrefusedImageDataContainer.class);

        ClearCLIJ clij = ClearCLIJ.getInstance();

        String folder =

        ClearCLImage src = clij.converter(lContainer.get("C" + mCameraIndexVariable.get() + "hybrid_interleaved_opticsprefused")).getClearCLImage();

        long[] targetSize = new long[]{src.getWidth(), src.getHeight(), src.getDepth() / 5};

        ClearCLImage dst0 = clij.createCLImage(targetSize, ImageChannelDataType.Float);
        ClearCLImage dst1 = clij.createCLImage(targetSize, ImageChannelDataType.Float);
        ClearCLImage dst2 = clij.createCLImage(targetSize, ImageChannelDataType.Float);
        ClearCLImage dst3 = clij.createCLImage(targetSize, ImageChannelDataType.Float);
        ClearCLImage dst4 = clij.createCLImage(targetSize, ImageChannelDataType.Float);

        Kernels.splitStack(clij, src, dst0, dst1, dst2, dst3, dst4);

        // save input data individually
        clij.show(dst0, "light sheet 0");
        IJ.saveAsTiff(IJ.getImage(), folder + "_light_sheets_0.tif");
        clij.show(dst1, "light sheet 1");
        IJ.saveAsTiff(IJ.getImage(), folder + "_light_sheets_1.tif");
        clij.show(dst2, "light sheet 2");
        IJ.saveAsTiff(IJ.getImage(), folder + "_light_sheets_2.tif");
        clij.show(dst3, "light sheet 3");
        IJ.saveAsTiff(IJ.getImage(), folder + "_light_sheets_3.tif");

        // save hardware fused stack
        clij.show(dst4, "all light sheets on");
        IJ.saveAsTiff(IJ.getImage(), folder + String.format("%0" + digits + "d", pTimePoint) + "_all_light_sheets_on.tif");

        // tenengrad fusion
        ClearCLImage result = clij.createCLImage(targetSize, src.getChannelDataType()); // re-using memory; result should not be closed later on

        float[] sigmas = new float[]
                { 15, 15, 5 };

        Kernels.tenengradFusion(clij, result, sigmas, dst0, dst1, dst2);
        clij.show(result, "result");
        IJ.saveAsTiff(IJ.getImage(), folder + "tenengrad_fusion.tif");

        // cleanup memory
        src.close();
        dst0.close();
        dst1.close();
        dst2.close();
        dst3.close();
        dst4.close();
        result.close();

*/
        return true;
    }

    @Override
    public InstructionInterface copy() {
        return null;
    }


    public BoundedVariable<Integer> getCameraIndexVariable() {
        return mCameraIndexVariable;
    }
}
