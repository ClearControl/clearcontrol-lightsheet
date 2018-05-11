package clearcontrol.deep;

import static utils.Convert.bytesToShort;

import java.io.File;
import java.util.Random;

import clearcl.ClearCL;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcontrol.stack.ContiguousOffHeapPlanarStackFactory;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import cleardl.core.TensorflowModel;
import cleardl.core.TensorflowModelExecutor;
import coremem.recycling.BasicRecycler;
import utils.Endianness;

public class StackInterfaceDemo
{
  public static void main(String[] args)
  {

    // instantiate stack interfaces

    // dims
    int sx = 100;
    int sy = 100;
    int sz = 10;

    // stack recycler
    BasicRecycler<StackInterface, StackRequest> lRecycler =
                                                          new BasicRecycler<>(new ContiguousOffHeapPlanarStackFactory(),
                                                                              2,
                                                                              1,
                                                                              true);

    // input
    StackInterface lInputStackInterface =
                                        lRecycler.getOrFail(new StackRequest(sx,
                                                                             sy,
                                                                             sz));

    // fill input stack interface with random
    Random lRandom = new Random();
    byte[] arr =
               new byte[(int) lInputStackInterface.getSizeInBytes()];
    lRandom.nextBytes(arr);
    lInputStackInterface.getContiguousMemory().copyFrom(arr);

    System.out.println(String.format("Some bytes: %d, %d, %d",
                                     lInputStackInterface.getContiguousMemory()
                                                         .getByteBuffer()
                                                         .get(sx),
                                     lInputStackInterface.getContiguousMemory()
                                                         .getByteBuffer()
                                                         .get(sy
                                                              * sx),
                                     lInputStackInterface.getContiguousMemory()
                                                         .getByteBuffer()
                                                         .get(sx * sy
                                                              * sz
                                                              / 2)));

    // output stack interface
    StackInterface lOutputStackInterface =
                                         lRecycler.getOrFail(new StackRequest(sx,
                                                                              sy,
                                                                              sz));

    // loading a model
    String lPathToModel = "resources/tf_models/conv3d_1";

    // ClearCL initialization
    ClearCLBackendJOCL lClearCLBackend = new ClearCLBackendJOCL();
    ClearCL lClearCL = new ClearCL(lClearCLBackend);
    ClearCLDevice lClearCLDevice = lClearCL.getLargestGPUDevice();
    System.out.println(lClearCLDevice.getName());
    ClearCLContext lCLearCLContext = lClearCLDevice.createContext();

    // instantiating a tensorflow object and executor
    TensorflowModel lTensorflowModel =
                                     new TensorflowModel(new File(lPathToModel));
    TensorflowModelExecutor lTensorflowExecutor =
                                                new TensorflowModelExecutor(lTensorflowModel,
                                                                            new int[]
                                                                            { sx, sy, sz });

    // input rocessor
    StackInterfaceShortInputProcessor lInputProcessor =
                                                      new StackInterfaceShortInputProcessor(lInputStackInterface,
                                                                                            lTensorflowExecutor.getInputFloatBuffer(),
                                                                                            lCLearCLContext);

    lInputProcessor.setPercentileHi(99);
    lInputProcessor.setPercentileLo(1);
    lInputProcessor.setUpdatePercentiles(true);

    int lIndToCheck = 10000;
    lTensorflowExecutor.setTensorflowInputPreprocessor(lInputProcessor);

    System.out.println("sth from the input float buffer before normalization: "
                       + lTensorflowExecutor.getInputFloatBuffer()
                                            .get(lIndToCheck));

    // running the network
    lTensorflowExecutor.run();

    System.out.println("sth from the input float buffer: "
                       + lTensorflowExecutor.getInputFloatBuffer()
                                            .get(lIndToCheck));

    System.out.println("exp result (x -0.040297): "
                       + lTensorflowExecutor.getInputFloatBuffer()
                                            .get(lIndToCheck)
                         * (-0.040297));

    System.out.println("sth from the output float buffer: "
                       + lTensorflowExecutor.getOutputFloatBuffer()
                                            .get(lIndToCheck));

    // Creating a ClearCL output buffer
    StackInterfaceShortOutputProcessor lOutputProcessor =
                                                        new StackInterfaceShortOutputProcessor(lOutputStackInterface,
                                                                                               lTensorflowExecutor.getOutputFloatBuffer(),
                                                                                               lCLearCLContext);

    float lMin = -0.1f;
    float lMax = 0.0f;
    float lScalingFactor = 3000;

    lOutputProcessor.setMin(lMin);
    lOutputProcessor.setMax(lMax);
    lOutputProcessor.setScaleFactor(lScalingFactor);
    lOutputProcessor.createOutput();

    System.out.println("sth from the output stack interface: "
                       + bytesToShort(new byte[]
                       { lOutputStackInterface.getContiguousMemory().getByte(2 * lIndToCheck), lOutputStackInterface.getContiguousMemory().getByte(2 * lIndToCheck + 1) }, Endianness.LE));

    // test

    float lConvMult = -0.040297f;
    for (int i = 0; i < sx * sy * sz; i += 2)
    {
      int lInputValue = lInputStackInterface.getContiguousMemory()
                                            .getShort(i);

      if (lInputValue < 0)
        lInputValue = Short.MAX_VALUE - Short.MIN_VALUE + lInputValue;

      // System.out.println("Short from input: " + lInputValue);
      double pLo = lInputProcessor.getLoLim();
      double pHi = lInputProcessor.getHiLim();

      // System.out.println("pLo and pHi: " + pLo + " " + pHi);

      // double lInputNorm = Math.min((Math.max(lInputValue, pLo)), pHi);

      double lInputNorm = (lInputValue - pLo) / (pHi - pLo);

      double lOutputNorm = Math.min((Math.max(lInputNorm * lConvMult,
                                              lMin)),
                                    lMax);

      lOutputNorm = lScalingFactor * (lOutputNorm - lMin)
                    / (lMax - lMin);

      double lOutputValue =
                          lOutputStackInterface.getContiguousMemory()
                                               .getShort(i);

      if (lOutputValue < 0)
        lOutputValue =
                     Short.MAX_VALUE - Short.MIN_VALUE + lOutputValue;

      if (Math.abs(lOutputNorm - lOutputValue) > 2)
        System.out.println(String.format("Snap! Value %d ->Network output: %f, calculated output: %f",
                                         i,
                                         lOutputValue,
                                         lOutputNorm));

    }

  }
}
