package clearcontrol.microscope.lightsheet.timelapse.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import clearcl.imagej.ClearCLIJ;
import clearcontrol.devices.stages.kcube.instructions.SpaceTravelInstruction;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.microscope.lightsheet.simulation.SimulatedLightSheetMicroscope;
import clearcontrol.microscope.lightsheet.state.spatial.Position;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleReader;
import clearcontrol.microscope.lightsheet.timelapse.io.ScheduleWriter;

import org.junit.Test;

/**
 * ScheduleIOTest
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf 08 2018
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
    microscope.addDevice(0, new SpaceTravelInstruction(microscope));

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

    SpaceTravelInstruction spaceTravelInstruction =
                                                  new SpaceTravelInstruction(microscope);
    spaceTravelInstruction.getTravelPathList()
                          .add(new Position(1, 2, 3));
    spaceTravelInstruction.getTravelPathList()
                          .add(new Position(4, 5, 6));

    list.add(spaceTravelInstruction);

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
    SpaceTravelInstruction readSpaceTravelInstruction =
                                                      (SpaceTravelInstruction) instructionInterface;
    assertEquals(1,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(0).mX,
                 tolerance);
    assertEquals(2,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(0).mY,
                 tolerance);
    assertEquals(3,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(0).mZ,
                 tolerance);
    assertEquals(4,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(1).mX,
                 tolerance);
    assertEquals(5,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(1).mY,
                 tolerance);
    assertEquals(6,
                 readSpaceTravelInstruction.getTravelPathList()
                                           .get(1).mZ,
                 tolerance);

  }
}
