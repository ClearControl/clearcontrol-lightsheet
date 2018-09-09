package clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.test;

import static org.junit.Assert.assertEquals;

import clearcontrol.microscope.lightsheet.spatialphasemodulation.zernike.ZernikePolynomials;

import org.junit.Test;

public class ZernikePolynomialsTests
{

  private static final double cEpsilon = 1e-10;

  /**
   * Zernicke radial polynomial R^m_n(x,y) tested against using values returned
   * Wolfram Alpha's function ZernikeR[0,0,0] (http://www.wolframalpha.com/)
   * 
   */
  @Test
  public void testZmnxy()
  {
    assertEquals(1,
                 ZernikePolynomials.computeZnmrt(0, 0, 0, 0),
                 cEpsilon);

    assertEquals(-0.625,
                 ZernikePolynomials.computeZnmrt(3, 1, 0.5, 0),
                 cEpsilon);

    assertEquals(-0.5,
                 ZernikePolynomials.computeZnmrt(2,
                                                 0,
                                                 0.5,
                                                 Math.PI / 2),
                 cEpsilon);

  }

  /**
   * Zernicke radial polynomial R^m_n(r) tested against values returned Wolfram
   * Alpha's function ZernikeR[0,0,0] (http://www.wolframalpha.com/)
   * 
   */
  @Test
  public void testRmnr()
  {
    assertEquals(1,
                 ZernikePolynomials.computeRnmr(0, 0, 0),
                 cEpsilon);

    assertEquals(-0.5,
                 ZernikePolynomials.computeRnmr(2, 0, 0.5),
                 cEpsilon);

    assertEquals(1.0,
                 ZernikePolynomials.computeRnmr(0, 0, 0),
                 cEpsilon);

    assertEquals(0,
                 ZernikePolynomials.computeRnmr(0, 2, 0.5),
                 cEpsilon);

    assertEquals(0.0,
                 ZernikePolynomials.computeRnmr(1, 1, 0),
                 cEpsilon);

    assertEquals(0.5,
                 ZernikePolynomials.computeRnmr(1, 1, 0.5),
                 cEpsilon);

    assertEquals(1.0,
                 ZernikePolynomials.computeRnmr(1, 1, 1),
                 cEpsilon);

    assertEquals(0.0,
                 ZernikePolynomials.computeRnmr(2, 1, 0.33),
                 cEpsilon);

    assertEquals(0.0,
                 ZernikePolynomials.computeRnmr(1, 2, 0.33),
                 cEpsilon);

    assertEquals(0.1089,
                 ZernikePolynomials.computeRnmr(2, 2, 0.33),
                 cEpsilon);

    assertEquals(9.00313068e-19,
                 ZernikePolynomials.computeRnmr(100, 100, 0.66),
                 cEpsilon);

  }

  @Test
  public void testJNoll()
  {

    assertEquals(1, ZernikePolynomials.jNoll(0, 0));
    assertEquals(2, ZernikePolynomials.jNoll(1, 1));
    assertEquals(3, ZernikePolynomials.jNoll(1, -1));
    assertEquals(4, ZernikePolynomials.jNoll(2, 0));
    assertEquals(5, ZernikePolynomials.jNoll(2, -2));
    assertEquals(6, ZernikePolynomials.jNoll(2, 2));
    assertEquals(7, ZernikePolynomials.jNoll(3, -1));
    assertEquals(8, ZernikePolynomials.jNoll(3, 1));
    assertEquals(9, ZernikePolynomials.jNoll(3, -3));
    assertEquals(10, ZernikePolynomials.jNoll(3, 3));

    assertEquals(11, ZernikePolynomials.jNoll(4, 0));
    assertEquals(12, ZernikePolynomials.jNoll(4, 2));
    assertEquals(13, ZernikePolynomials.jNoll(4, -2));
    assertEquals(14, ZernikePolynomials.jNoll(4, 4));
    assertEquals(15, ZernikePolynomials.jNoll(4, -4));
    assertEquals(16, ZernikePolynomials.jNoll(5, 1));
    assertEquals(17, ZernikePolynomials.jNoll(5, -1));
    assertEquals(18, ZernikePolynomials.jNoll(5, 3));
    assertEquals(19, ZernikePolynomials.jNoll(5, -3));
    assertEquals(20, ZernikePolynomials.jNoll(5, 5));

  }

  @Test
  public void testZernikeModeNames()
  {
    assertEquals("Defocus", ZernikePolynomials.getZernikeModeName(4));
    assertEquals("Defocus",
                 ZernikePolynomials.getZernikeModeNameFromNollIndex(4));
    assertEquals("Vertical astigmatism",
                 ZernikePolynomials.getZernikeModeName(5));
    assertEquals("Vertical astigmatism",
                 ZernikePolynomials.getZernikeModeNameFromNollIndex(6));
  }

}
