package clearcontrol.microscope.lightsheet.spatialphasemodulation.instructions;

import clearcl.imagej.utilities.ImageTypeConverter;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.core.variable.bounded.BoundedVariable;
import clearcontrol.microscope.lightsheet.LightSheetMicroscope;
import clearcontrol.microscope.lightsheet.instructions.LightSheetMicroscopeInstructionBase;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.containers.PSFContainer;
import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;
import clearcontrol.microscope.stacks.StackRecyclerManager;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import coremem.recycling.RecyclerInterface;
import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.io.File;

/**
 * The LoadPSFInstruction loads a TIF image from a given folder containing a PSF with a specified Zernike mode.
 *
 * Author: @haesleinhuepf
 * 07 2018
 */
public class LoadPSFInstruction extends LightSheetMicroscopeInstructionBase implements
        LoggingFeature {
    private Variable<File> mRootFolderVariable =
            new Variable("RootFolder",
                    (Object) null);

    private BoundedVariable<Double> mZernikeFactors[];

    private final double PRECISION = 0.1;

    public LoadPSFInstruction(LightSheetMicroscope pLightSheetMicroscope, int numberOfZernikeFactors) {
        super("IO: Load PSF", pLightSheetMicroscope);

        mZernikeFactors = new BoundedVariable[numberOfZernikeFactors];

        for (int i = 0; i < numberOfZernikeFactors; i++) {
            mZernikeFactors[i] = new BoundedVariable<Double>("Z" + ZernikePolynomials.jNoll(i) + ": " + ZernikePolynomials.getZernikeModeName(i), 0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.0001);
        }
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean enqueue(long pTimePoint) {
        String filename = getRootFolderVariable().get() + "\\PSF";

        for (int i = 0; i < mZernikeFactors.length; i++) {
            double zernikeFactor = mZernikeFactors[i].get();

            if (Math.abs(zernikeFactor) >= PRECISION) {
                String presign = "pos";
                if (zernikeFactor < 0) {
                    presign = "neg";
                }

                zernikeFactor = Math.abs(Math.round(zernikeFactor / PRECISION) * PRECISION);

                filename = filename + "_Z" + ZernikePolynomials.jNoll(i) + "_" + presign + "_" + zernikeFactor;
            }
        }

        filename = filename + ".tif";

        StackRecyclerManager
                lStackRecyclerManager = getLightSheetMicroscope().getDevice(StackRecyclerManager.class, 0);
        RecyclerInterface<StackInterface, StackRequest>
                lRecycler = lStackRecyclerManager.getRecycler("warehouse",
                1024,
                1024);

        ImagePlus psfImage = IJ.openImage(filename);
        if (psfImage == null) {
            warning("PSF image file " + filename + " was not found!");
            return false;
        }
        info("Successfully loaded PSF from " + filename);

        Img<FloatType> psfImg = ImageJFunctions.convertFloat(psfImage);

        StackRequest lStackRequest = new StackRequest(psfImage.getWidth(), psfImage.getHeight(), psfImage.getNSlices());

        StackInterface lStack = lRecycler.getOrFail(lStackRequest);

        ImageTypeConverter.copyRandomAccessibleIntervalToOffHeapPlanarStack(psfImg, lStack);

        PSFContainer psfContainer = new PSFContainer(pTimePoint);

        psfContainer.setPSF(lStack);

        getLightSheetMicroscope().getDataWarehouse().put("PSF_" + pTimePoint, psfContainer);

        return true;
    }

    @Override
    public LoadPSFInstruction copy() {
        LoadPSFInstruction copy = new LoadPSFInstruction(getLightSheetMicroscope(), mZernikeFactors.length);
        for (int i = 0; i < mZernikeFactors.length; i++) {
            copy.mZernikeFactors[i].set(mZernikeFactors[i].get());
        }
        copy.mRootFolderVariable.set(mRootFolderVariable.get());
        return copy;
    }


    public Variable<File> getRootFolderVariable()
    {
        return mRootFolderVariable;
    }

    public BoundedVariable<Double>[] getZernikeFactorVariables() {
        return mZernikeFactors;
    }
}