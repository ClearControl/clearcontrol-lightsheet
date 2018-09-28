package clearcontrol.microscope.lightsheet.timelapse.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import clearcl.imagej.ClearCLIJ;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleReader;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;

import org.junit.Test;

/**
 * The ScheduleIOTest tests if instruction lists can be written to and read from
 * disc.
 *
 * Author: @haesleinhuepf August 2018
 */
public class ScheduleIOTest
{
  private static double tolerance = 0.0001;

  @Test
  public void testReadWrite() throws IOException
  {
    ClearCLIJ clij = ClearCLIJ.getInstance();

    SimulatedLightSheetMicroscope microscope =
                                             new SimulatedLightSheetMicroscope("microscope",
                                                                               clij.getClearCLContext(),
                                                                               32,
                                                                               32);
    microscope.addDevice(0, new TestInstruction());

    ArrayList<InstructionInterface> list =
                                         new ArrayList<InstructionInterface>();

    TestInstruction instruction =
                                microscope.getDevice(TestInstruction.class,
                                                     0);
    instruction.mFile.set(new File("bla.txt"));
    instruction.mBoolean.set(false);
    instruction.mInteger.set(3);
    instruction.mDouble.set(2.5);
    instruction.mString.set("Hello world");

    list.add(instruction);

    File file = new File("temp.txt");

    ScheduleWriter writer = new ScheduleWriter(list, file);
    writer.write();

    ArrayList<InstructionInterface> readList =
                                             new ArrayList<InstructionInterface>();
    ScheduleReader reader = new ScheduleReader(readList,
                                               microscope,
                                               file);
    reader.read();

    TestInstruction readInstruction =
                                    (TestInstruction) (readList.get(0));

    assertEquals(instruction.mDouble.get(),
                 readInstruction.mDouble.get());
    assertEquals(instruction.mInteger.get(),
                 readInstruction.mInteger.get());
    assertEquals(instruction.mBoolean.get(),
                 readInstruction.mBoolean.get());
    assertEquals(0,
                 instruction.mFile.get()
                                  .getCanonicalPath()
                                  .compareTo(readInstruction.mFile.get()
                                                                  .getCanonicalPath()));
    assertEquals(0,
                 instruction.mString.get()
                                    .compareTo(readInstruction.mString.get()));

    InstructionInterface instructionInterface = readList.get(1);

  }
}
