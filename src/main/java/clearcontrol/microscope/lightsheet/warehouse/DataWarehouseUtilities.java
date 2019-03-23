package clearcontrol.microscope.lightsheet.warehouse;

import clearcontrol.microscope.lightsheet.postprocessing.containers.FocusMeasuresContainer;
import clearcontrol.microscope.lightsheet.postprocessing.measurements.instructions.MeasureImageQualityInstruction;
import clearcontrol.microscope.lightsheet.processor.fusion.FusedImageDataContainer;
import clearcontrol.microscope.lightsheet.warehouse.containers.DataContainerInterface;
import clearcontrol.microscope.lightsheet.warehouse.containers.StackInterfaceContainer;

public class DataWarehouseUtilities {
    public static boolean isImageContainer(Class containerClass) {
        return StackInterfaceContainer.class.isAssignableFrom(containerClass);
    }
    public static boolean containsImageContainer(Class[] containerClasses) {
        for (Class clazz : containerClasses) {
            if (isImageContainer(clazz)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String... args) {
        System.out.println("Fused: " + isImageContainer(FusedImageDataContainer.class));
        System.out.println("Focus: " + isImageContainer(FocusMeasuresContainer.class));
        System.out.println("Stack: " + isImageContainer(StackInterfaceContainer.class));
        System.out.println("Data: " + isImageContainer(DataContainerInterface.class));
    }
}
