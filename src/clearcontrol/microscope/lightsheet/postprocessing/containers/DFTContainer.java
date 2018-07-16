package clearcontrol.microscope.lightsheet.postprocessing.containers;

import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerBase;


public class DFTContainer extends MeasurementArrayContainer {

    double[][][] mDFT = null;

    public DFTContainer(long pTimePoint, int pDepth, int pHeight, int pWidth, double[][][] pDFT) {
        super(pTimePoint, pDepth, pHeight, pWidth, pDFT);
        mDFT = pDFT;
    }

    @Override
    public boolean isDataComplete() {
        return false;
    }

    @Override
    public void dispose() {

    }

    public double[][][] getRealPart(){
        int lDepth = getDepth();
        int lHeight = getHeight();
        int lWidth = getWidth();
        double real[][][] = new double[lDepth][lHeight][(int)lWidth/2];
        for(int z = 0; z< lDepth ;z++){
            for( int x = 0; x<lHeight; x ++){
                for (int y = 0; y<lWidth; y+=2){
                    real[z][x][y/2] = mDFT[z][x][y];
                }
            }
        }
        return real;
    }
    public double[][][] getImaginaryPart(){
        int lDepth = getDepth();
        int lHeight = getHeight();
        int lWidth = getWidth();
        double imag[][][] = new double[lDepth][lHeight][(int)lWidth/2];
        for(int z = 0; z< lDepth ;z++){
            for( int x = 0; x<lHeight; x ++){
                for (int y = 0; y<lWidth; y+=2){
                    imag[z][x][y/2] = mDFT[z][x][y+1];
                }
            }
        }
        return imag;
    }
}
