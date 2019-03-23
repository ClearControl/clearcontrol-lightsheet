package clearcontrol.microscope.lightsheet.postprocessing.clij;

import coremem.enums.NativeTypeEnum;
import net.haesleinhuepf.clij.clearcl.enums.ImageChannelDataType;

/**
 * ConverterUtilities
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 03 2019
 */
public class ConverterUtilities {

    public static net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum nativeToNativeHSLH(NativeTypeEnum type) {
        return net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum.valueOf(type.toString());
    }

    public static NativeTypeEnum nativeHSLHToNative(net.haesleinhuepf.clij.coremem.enums.NativeTypeEnum type) {
        return NativeTypeEnum.valueOf(type.toString());
    }

    public static ImageChannelDataType nativeTypeToImageChannelDataType(NativeTypeEnum lType) {
        ImageChannelDataType lImageChannelType = null;

        if (lType == NativeTypeEnum.UnsignedByte) {
            return ImageChannelDataType.UnsignedInt8;
        } else if (lType == NativeTypeEnum.Byte) {
            return ImageChannelDataType.SignedInt8;
        } else if (lType == NativeTypeEnum.UnsignedShort) {
            return ImageChannelDataType.UnsignedInt16;
        } else if (lType == NativeTypeEnum.Short) {
            return ImageChannelDataType.SignedInt16;
        } else if (lType == NativeTypeEnum.Float) {
            return ImageChannelDataType.Float;
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert image of type " + lType);
        }
    }

    public static NativeTypeEnum imageChannelDataTypeToNativeType(ImageChannelDataType lType) {

        if (lType == ImageChannelDataType.UnsignedInt8) {
            return NativeTypeEnum.UnsignedByte;
        } else if (lType == ImageChannelDataType.SignedInt8) {
            return NativeTypeEnum.Byte;
        } else if (lType == ImageChannelDataType.UnsignedInt8) {
            return NativeTypeEnum.UnsignedByte;
        } else if (lType == ImageChannelDataType.UnsignedInt16) {
            return NativeTypeEnum.UnsignedShort;
        } else if (lType == ImageChannelDataType.SignedInt16) {
            return NativeTypeEnum.Short;
        } else if (lType == ImageChannelDataType.Float) {
            return NativeTypeEnum.Float;
        } else  {
            throw new IllegalArgumentException(
                    "Cannot convert image of type " + lType);
        }

    }
}
