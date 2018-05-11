package clearcontrol.deep;

import static utils.Convert.bytesToShort;

import java.io.File;
import java.util.Random;

import clearcl.ClearCL;
import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcontrol.stack.ContiguousOffHeapPlanarStackFactory;
import clearcontrol.stack.StackInterface;
import clearcontrol.stack.StackRequest;
import cleardl.core.TensorflowModel;
import cleardl.core.TensorflowModelExecutor;
import coremem.enums.NativeTypeEnum;
import coremem.recycling.BasicRecycler;
import utils.Endianness;

public class StackInterfaceDemo
{
  public static void main(String[] args)
  {

    int sx = 100;
    int sy = 100;
    int sz = 10;

    // StackInterface lInputStackInterface = new
    // ContiguousOffHeapPlanarStackFactory().create(new StackRequest(sx, sy,
    // sz));

    BasicRecycler<StackInterface, StackRequest> lRecycler =
                                                          new BasicRecycler<>(new ContiguousOffHeapPlanarStackFactory(),
                                                                              2,
                                                                              1,
                                                                              true);
    StackInterface lInputStackInterface =
                                        lRecycler.getOrFail(new StackRequest(sx,
                                                                             sy,
                                                                             sz));
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

    // StackInterface lOutputStackInterface = new
    // ContiguousOffHeapPlanarStackFactory().create(new StackRequest(sx, sy,
    // sz));
    StackInterface lOutputStackInterface =
                                         lRecycler.getOrFail(new StackRequest(sx,
                                                                              sy,
                                                                              sz));

    int[] dims = new int[]
    { (int) lInputStackInterface.getDimension(0),
      (int) lInputStackInterface.getDimension(1),
      (int) lInputStackInterface.getDimension(2) };

    // loading a model
    String lPathToModel = "resources/tf_models/conv3d_1";

    // ClearCL initialization
    ClearCLBackendJOCL lClearCLBackend = new ClearCLBackendJOCL();
    ClearCL lClearCL = new ClearCL(lClearCLBackend);
    ClearCLDevice lClearCLDevice = lClearCL.getLargestGPUDevice();
    System.out.println(lClearCLDevice.getName());
    ClearCLContext lCLearCLContext = lClearCLDevice.createContext();

    // instantiating a tensorflow object

    TensorflowModel lTensorflowModel =
                                     new TensorflowModel(new File(lPathToModel));
    TensorflowModelExecutor lTensorflowExecutor =
                                                new TensorflowModelExecutor(lTensorflowModel,
                                                                            dims);
    StackInterfaceShortInputProcessor lPreprocessor =
                                                    new StackInterfaceShortInputProcessor(lInputStackInterface,
                                                                                          lTensorflowExecutor.getInputFloatBuffer(),
                                                                                          lCLearCLContext);

    lPreprocessor.setPercentileHi(99);
    lPreprocessor.setPercentileLo(1);
    lPreprocessor.setUpdatePercentiles(true);

    int lIndToCheck = 10000;
    lTensorflowExecutor.setTensorflowInputPreprocessor(lPreprocessor);

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

    // Creating a ClearCL output buffer, mind the size - half of the original
    // due to 16bit->8bit downscaling
    ClearCLBuffer lClearCLOutputBuffer =
                                       lCLearCLContext.createBuffer(NativeTypeEnum.Float,
                                                                    lInputStackInterface.getContiguousMemory()
                                                                                        .getByteBuffer()
                                                                                        .capacity()
                                                                                          / 2);
    lClearCLOutputBuffer.readFrom(lTensorflowExecutor.getOutputFloatBuffer(),
                                  true);

    StackInterfaceShortOutputProcessor lOutputProcessor =
                                                        new StackInterfaceShortOutputProcessor(lOutputStackInterface,
                                                                                               lTensorflowExecutor.getOutputFloatBuffer(),
                                                                                               lCLearCLContext);

    lOutputProcessor.setMin(-0.1f);
    lOutputProcessor.setMax(0);
    lOutputProcessor.setScaleFactor(300);
    lOutputProcessor.createOutput();

    System.out.println("sth from the output stack interface: "
                       + bytesToShort(new byte[]
                       { lOutputStackInterface.getContiguousMemory().getByte(2 * lIndToCheck), lOutputStackInterface.getContiguousMemory().getByte(2 * lIndToCheck + 1) }, Endianness.LE));

    // lInputStackInterface.release();
    // lOutputStackInterface.release();

    // System.out.println("sth from the output buffer: " +
    // lOutputStackInterface.getContiguousMemory().getByteBuffer().get(10000));

    // uncomment if want the input and output to be saved
    lOutputStackInterface.getContiguousMemory()
                         .getByteBuffer()
                         .rewind();

  }
}
