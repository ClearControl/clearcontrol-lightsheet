package clearcontrol.deep;

import static utils.Readers.readFileAsResourceStream;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcontrol.stack.StackInterface;
import cleardl.processors.InputPreprocessor;
import coremem.enums.NativeTypeEnum;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class StackInterfaceShortInputProcessor extends
                                               InputPreprocessor
{

  private int[] mInputBufferDims;
  private ClearCLContext mContext;

  // stacks and buffers
  private StackInterface mInputStackInterface;
  private DoubleBuffer mDoubleBuffer;

  // bufferCL
  private ClearCLBuffer mOriginalShortBufferCL;
  private ClearCLBuffer mFloatBufferCL;
  private ClearCLBuffer mDoubleBufferCL;

  // kernels
  private ClearCLKernel mClearCLShortToDoubleKernel;
  private ClearCLKernel mClearCLDoubleToFloatKernel;

  // limits
  private int mPercentileLo;
  private int mPercentileHi;

  // logging
  private boolean mLoggingOn;

  // percentile
  private Percentile mPercentile;
  private boolean mUpdatePercentiles;

  public StackInterfaceShortInputProcessor(StackInterface pInputShortStackInterface,
                                           FloatBuffer pTensorflowInputBuffer,
                                           ClearCLContext pContext)
  {
    super(pTensorflowInputBuffer);

    if (pInputShortStackInterface.getSizeInBytes()
        / 2 != pTensorflowInputBuffer.capacity())
      throw new IllegalArgumentException(String.format("Buffer sizes don't match. InputStackInterface has %d entries, TensorflowInputBuffer "
                                                       + "has %d.",
                                                       pInputShortStackInterface.getSizeInBytes()
                                                                    / 2,
                                                       pTensorflowInputBuffer.capacity()));

    // logging
    mLoggingOn = true;

    // percentile
    mUpdatePercentiles = true;
    mPercentile = new Percentile();

    // context
    mContext = pContext;

    // buffers and stacks
    mInputStackInterface = pInputShortStackInterface;
    mDoubleBuffer =
                  DoubleBuffer.allocate((int) mInputStackInterface.getSizeInBytes()
                                        / 2);

    mInputBufferDims =
                     new int[mInputStackInterface.getNumberOfDimensions()];
    for (int i = 0; i < mInputBufferDims.length; i++)
    {
      mInputBufferDims[i] =
                          (int) mInputStackInterface.getDimension(i);
    }

    // buffers CL
    mOriginalShortBufferCL =
                           mContext.createBuffer(NativeTypeEnum.Short,
                                                 mInputStackInterface.getSizeInBytes()
                                                                       / 2);
    mFloatBufferCL =
                   mContext.createBuffer(NativeTypeEnum.Float,
                                         getTensorflowInputBuffer().capacity());
    mDoubleBufferCL =
                    mContext.createBuffer(NativeTypeEnum.Double,
                                          getTensorflowInputBuffer().capacity());

    // kernels
    String lKernelSource =
                         readFileAsResourceStream(getClass().getClassLoader()
                                                            .getResourceAsStream("kernels/TFConvert.cl"));
    ClearCLProgram lProgram = mContext.createProgram(lKernelSource);
    try
    {
      lProgram.buildAndLog();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    // configure "short-to-double" kernel
    mClearCLShortToDoubleKernel =
                                lProgram.createKernel("ConvertUshortToDouble");
    mClearCLShortToDoubleKernel.setArguments(mOriginalShortBufferCL,
                                             mDoubleBufferCL,
                                             mInputBufferDims[0],
                                             mInputBufferDims[1],
                                             mInputBufferDims[2]);
    mClearCLShortToDoubleKernel.setGlobalSizes(mInputBufferDims[0],
                                               mInputBufferDims[1],
                                               mInputBufferDims[2]);

    // configure "double-to-float" kernel
    mClearCLDoubleToFloatKernel =
                                lProgram.createKernel("ConvertDoubleToFloatAndNormalize");
    mClearCLDoubleToFloatKernel.setArguments(mDoubleBufferCL,
                                             mFloatBufferCL,
                                             mInputBufferDims[0],
                                             mInputBufferDims[1],
                                             mInputBufferDims[2],
                                             0,
                                             300);

    mClearCLDoubleToFloatKernel.setGlobalSizes(mInputBufferDims[0],
                                               mInputBufferDims[1],
                                               mInputBufferDims[2]);
  }

  public void recalculatePercentiles()
  {
    if (mLoggingOn)
      System.out.println("[TensorflowModelPlus]: updating percentile.");

    mOriginalShortBufferCL.readFrom(mInputStackInterface.getContiguousMemory()
                                                        .getByteBuffer(),
                                    true);
    mClearCLShortToDoubleKernel.run();
    mDoubleBufferCL.writeTo(mDoubleBuffer, true);

    double hi = mPercentile.evaluate(mDoubleBuffer.array(),
                                     mPercentileHi);
    double lo = mPercentile.evaluate(mDoubleBuffer.array(),
                                     mPercentileLo);

    if (mLoggingOn)
    {
      System.out.println("[TensorflowModelPlus]: new hi is " + hi);
      System.out.println("[TensorflowModelPlus]: new lo is " + lo);
    }

    mClearCLDoubleToFloatKernel.setArgument(5, lo);
    mClearCLDoubleToFloatKernel.setArgument(6, hi);

  }

  @Override
  public void updateInputFloatBuffer()
  {
    if (mUpdatePercentiles)
    {
      recalculatePercentiles();
      mClearCLDoubleToFloatKernel.run();

    }
    else
    {
      mOriginalShortBufferCL.readFrom(mInputStackInterface.getContiguousMemory()
                                                          .getByteBuffer(),
                                      true);
      mClearCLShortToDoubleKernel.run();
      mClearCLDoubleToFloatKernel.run();
    }
    mFloatBufferCL.writeTo(getTensorflowInputBuffer(), true);

  }

  public void setLoggingOn(boolean pLoggingOn)
  {
    this.mLoggingOn = pLoggingOn;
  }

  public void setUpdatePercentiles(boolean pUpdatePercentiles)
  {
    this.mUpdatePercentiles = pUpdatePercentiles;
  }

  public void setPercentileLo(int pPercentileLo)
  {
    this.mPercentileLo = pPercentileLo;
  }

  public void setPercentileHi(int pPercentileHi)
  {
    this.mPercentileHi = pPercentileHi;
  }

  public static void main(String[] args)
  {

  }
}
