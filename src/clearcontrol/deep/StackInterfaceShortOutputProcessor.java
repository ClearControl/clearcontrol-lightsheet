package clearcontrol.deep;

import static utils.Readers.readFileAsResourceStream;

import java.io.IOException;
import java.nio.FloatBuffer;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcontrol.stack.StackInterface;
import cleardl.processors.OutputPostprocessor;
import coremem.enums.NativeTypeEnum;

public class StackInterfaceShortOutputProcessor extends
                                                OutputPostprocessor
{

  // context
  private ClearCLContext mContext;

  // stacks and buffers
  private int[] mOutputBufferDims;
  private StackInterface mOuputShortStackInterface;

  // buffersCL
  private ClearCLBuffer mOutputShortBufferCL;
  private ClearCLBuffer mFloatBufferCL;

  // kernel
  private ClearCLKernel mClearCLFloatToOutputKernel;

  // scale factor
  private float mScaleFactor;

  // limits
  private float mMin;
  private float mMax;

  // logging
  private boolean mLoggingOn;

  public StackInterfaceShortOutputProcessor(StackInterface pOutputShortStackInterface,
                                            FloatBuffer pTensorflowOutputBuffer,
                                            ClearCLContext pContext)
  {
    super(pTensorflowOutputBuffer);

    if (pOutputShortStackInterface.getSizeInBytes()
        / 2 != pTensorflowOutputBuffer.capacity())
      throw new IllegalArgumentException(String.format("Buffer sizes don't match. OutputStackInterface has %d entries, TensorflowOutputBuffer"
                                                       + " "
                                                       + "has %d.",
                                                       pOutputShortStackInterface.getSizeInBytes()
                                                                    / 2,
                                                       pTensorflowOutputBuffer.capacity()));

    // logging
    mLoggingOn = true;

    // context
    mContext = pContext;

    // scale factor
    mScaleFactor = 300;

    // buffers and stacks
    mOuputShortStackInterface = pOutputShortStackInterface;

    // dimensions
    mOutputBufferDims =
                      new int[mOuputShortStackInterface.getNumberOfDimensions()];
    for (int i = 0; i < mOutputBufferDims.length; i++)
    {
      mOutputBufferDims[i] =
                           (int) mOuputShortStackInterface.getDimension(i);
    }

    // buffers CL
    mOutputShortBufferCL =
                         mContext.createBuffer(NativeTypeEnum.Short,
                                               mOuputShortStackInterface.getSizeInBytes()
                                                                     / 2);
    mFloatBufferCL =
                   mContext.createBuffer(NativeTypeEnum.Float,
                                         getTensorflowOutputBuffer().capacity());

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

    // configure "float-to-short" kernel
    mClearCLFloatToOutputKernel =
                                lProgram.createKernel("ConvertFloatToUshort");
    mClearCLFloatToOutputKernel.setArguments(mFloatBufferCL,
                                             mOutputShortBufferCL,
                                             mOutputBufferDims[0],
                                             mOutputBufferDims[1],
                                             mOutputBufferDims[2],
                                             0,
                                             1,
                                             mScaleFactor);
    mClearCLFloatToOutputKernel.setGlobalSizes(mOutputBufferDims[0],
                                               mOutputBufferDims[1],
                                               mOutputBufferDims[2]);

  }

  @Override
  public void createOutput()
  {
    mFloatBufferCL.readFrom(getTensorflowOutputBuffer(), true);
    mClearCLFloatToOutputKernel.run();
    mOutputShortBufferCL.writeTo(mOuputShortStackInterface.getContiguousMemory()
                                                          .getByteBuffer(),
                                 true);

  }

  public void setLoggingOn(boolean pLoggingOn)
  {
    this.mLoggingOn = pLoggingOn;
  }

  public void setMin(float pMin)
  {
    this.mMin = pMin;
    mClearCLFloatToOutputKernel.setArgument(5, mMin);
  }

  public void setMax(float pMax)
  {
    this.mMax = pMax;
    mClearCLFloatToOutputKernel.setArgument(6, mMax);
  }

  public float getScaleFactor()
  {
    return mScaleFactor;
  }

  public void setScaleFactor(float pScaleFactor)
  {
    this.mScaleFactor = pScaleFactor;
    mClearCLFloatToOutputKernel.setArgument(7, mScaleFactor);
  }

  public static void main(String[] args)
  {

  }
}
