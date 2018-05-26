package clearcontrol.devices.filterwheel.schedulers;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.instructions.InstructionBase;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.devices.optomech.filterwheels.FilterWheelDeviceInterface;

public class FilterWheelInstruction extends InstructionBase implements
        InstructionInterface,
                                                        LoggingFeature
{
    FilterWheelDeviceInterface mFilterWheelDevice;
    int mPosition;

    public FilterWheelInstruction(FilterWheelDeviceInterface pFilterWheelDevice, int pPosition)
    {
        super("Filter wheel: Set filter of " + pFilterWheelDevice.getName() + " to " + pFilterWheelDevice.getPositionName(pPosition));
        mFilterWheelDevice = pFilterWheelDevice;
        mPosition = pPosition;
    }

    @Override public boolean initialize()
    {
        return true;
    }

    @Override public boolean enqueue(long pTimePoint)
    {
        mFilterWheelDevice.setPosition(mPosition);
        return true;
    }
}
