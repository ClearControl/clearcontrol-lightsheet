package clearcontrol.devices.imagej;

import net.imagej.ImageJ;

/**
 * Author: Robert Haase (http://haesleinhuepf.net) at MPI CBG
 * (http://mpi-cbg.de) February 2018
 */
public interface ImageJFeature
{
  default void showImageJ()
  {
    ImageJSingleton.getInstance().showImageJ();
  }
  default ImageJ getImageJ2() { return ImageJSingleton.getInstance().getImageJ2Instance(); }
}
