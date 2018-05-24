package clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.dmp40.demo;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.slms.devices.dmp40.DMP40Device;
import dmp40j.DMP40JDevice;

import static dmp40j.bindings.TLDFMX_64Library.TLDFMX_zernike_flag_t.Z_Ast0_Flag;
import static dmp40j.bindings.TLDFMX_64Library.TLDFMX_zernike_flag_t.Z_Def_Flag;

/**
 * DMP40Demo
 * <p>
 * <p>
 * <p>
 * Author: @haesleinhuepf
 * 05 2018
 */
public class DMP40Demo {
    public static void main(String... args) {
        DMP40Device mirror = new DMP40Device("M00456037");

        if (mirror.start()) {

            System.out.println("The mirror has name " + mirror.getName());
            System.out.println("This mirror supports " + mirror.getZernikeFactors().length + " zernike factors.");
            System.out.println("Zernike factors should range between " + mirror.getMinZernikeAmplitude() + " and " + mirror.getMaxZernikeAmplitude());

            double[] zernikeFactors = mirror.getZernikeFactors();
            zernikeFactors[(int)Z_Def_Flag.value] = mirror.getMinZernikeAmplitude();
            zernikeFactors[(int)Z_Ast0_Flag.value] = mirror.getMaxZernikeAmplitude();

            mirror.setZernikeFactors(zernikeFactors);
            mirror.stop();
        } else {
            System.out.println("Could not connect to mirror " + mirror.getName());
        }
    }

}
