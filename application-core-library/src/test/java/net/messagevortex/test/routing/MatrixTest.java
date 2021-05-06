package net.messagevortex.test.routing;

import static org.junit.Assert.assertTrue;


import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.router.operation.GaloisFieldMathMode;
import net.messagevortex.router.operation.MathMode;
import net.messagevortex.router.operation.Matrix;
import net.messagevortex.router.operation.RealMathMode;
import net.messagevortex.router.operation.RedundancyMatrix;
import net.messagevortex.router.operation.VandermondeMatrix;
import org.junit.jupiter.api.Test;


public class MatrixTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  @Test
  public void basicMatrixTest() {
    LOGGER.log(Level.INFO, "basic multiplication test (unit matrices)");
    for (int i = 1; i <= 10; i++) {
      LOGGER.log(Level.INFO, "  Testing unit multiplication with size " + i);
      Matrix unit = Matrix.unitMatrix(i, RealMathMode.getRealMathMode());
      assertTrue("error multiplying unit matrices (" + i + ")\n" + unit, unit.mul(unit).equals(unit));
      for (int j = 0; j < 11; j++) {
        int size = (int) (Math.random() * 10) + 1;
        Matrix m = Matrix.randomMatrix(i, size, RealMathMode.getRealMathMode());
        LOGGER.log(Level.INFO, "  Testing unit multiplication with random matrix size (" + i + "/" + size + "; run is " + j + ")");
        unit = Matrix.unitMatrix(i, RealMathMode.getRealMathMode());
        assertTrue("error multiplying random matrices (" + i + "/" + size + ")\n" + m + "\n=\n" + m.mul(unit), m.mul(unit).equals(m));
      }
    }
  }

  @Test
  public void vandermondeMatrixTest() {
    LOGGER.log(Level.INFO, "VandermondeMatrix blackbox tests");
    Matrix m = new VandermondeMatrix(3, 6, GaloisFieldMathMode.getGaloisFieldMathMode(4));
    LOGGER.log(Level.INFO, "Matrix is \n" + m);
    assertTrue("Illegal row 0 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(0)) + "]", Arrays.equals(new int[] {1, 0, 0}, m.getRow(0)));
    assertTrue("Illegal row 1 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(1)) + "]", Arrays.equals(new int[] {1, 1, 1}, m.getRow(1)));
    assertTrue("Illegal row 2 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(2)) + "]", Arrays.equals(new int[] {1, 2, 4}, m.getRow(2)));
    assertTrue("Illegal row 0 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(3)) + "]", Arrays.equals(new int[] {1, 3, 5}, m.getRow(3)));
    assertTrue("Illegal row 1 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(4)) + "]", Arrays.equals(new int[] {1, 4, 3}, m.getRow(4)));
    assertTrue("Illegal row 2 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(5)) + "]", Arrays.equals(new int[] {1, 5, 2}, m.getRow(5)));
  }

  @Test
  public void redundancyMatrixTest() {
    final int[] MAX_GALOIS = new int[] {4, 8, 16};
    SecureRandom sr = new SecureRandom();
    for (int galois : MAX_GALOIS) {
      MathMode mm = GaloisFieldMathMode.getGaloisFieldMathMode(galois);

      for (int k = 0; k < 100; k++) {
        LOGGER.log(Level.INFO, "testing redundancy matrix for GF(2^" + galois + ") run " + k);
        // generate random data vector
        Matrix data = new Matrix(1, sr.nextInt(7) + 1, mm);
        for (int i = 0; i < data.getY(); i++) {
          data.setField(0, i, (int) (Math.random() * Math.pow(2, galois)));
        }
        // determine total blocks (including redundancy)
        int tot = sr.nextInt(7) + 1 + data.getY();

        // determine bad/missing block indexes
        int[] missing = new int[sr.nextInt(tot - data.getY()) + 1];
        for (int i = 0; i < missing.length; i++) {
          boolean duplicated = true;
          while (duplicated) {
            missing[i] = sr.nextInt(tot + 1);
            duplicated = false;
            if (i > 0) {
              for (int j = 0; j < i && !duplicated; j++) {
                if (missing[i] == missing[j]) {
                  duplicated = true;
                }
              }
            }
          }
        }
        Arrays.sort(missing);

        LOGGER.log(Level.INFO, "  test parameter: dataRows=" + data.getY() + "; totalRows=" + tot + "; missingRows=" + missing.length);

        LOGGER.log(Level.INFO, "  Got data vector\r\n" + data);

        RedundancyMatrix m1 = new RedundancyMatrix(data.getY(), tot, mm);
        LOGGER.log(Level.INFO, "  Got redundancy matrix for GF(2^" + galois + ")\r\n" + m1);
        RedundancyMatrix dataRowsOfRedundancyMatrix = new RedundancyMatrix(m1);
        while (dataRowsOfRedundancyMatrix.getX() != dataRowsOfRedundancyMatrix.getY()) {
          dataRowsOfRedundancyMatrix.removeRow(dataRowsOfRedundancyMatrix.getX());
        }
        assertTrue("data rows in redundancy matrix are not unit rows \n" + Matrix.unitMatrix(dataRowsOfRedundancyMatrix.getX(), mm) + "\n" + dataRowsOfRedundancyMatrix, Matrix.unitMatrix(dataRowsOfRedundancyMatrix.getX(), mm).equals(dataRowsOfRedundancyMatrix));

        Matrix red = m1.mul(data);
        LOGGER.log(Level.INFO, "  Got data with redundancy vector\r\n" + red.toString());

        Matrix damaged = new Matrix(red);
        for (int i = missing.length - 1; i >= 0; i--) {
          red.removeRow(missing[i]);
          damaged.removeRow(missing[i]);
        }
        while (damaged.getY() != data.getY()) {
          damaged.removeRow(data.getY());
        }
        LOGGER.log(Level.INFO, "  Got damaged vector\r\n" + damaged);

        Matrix m2 = m1.getRecoveryMatrix(missing);
        LOGGER.log(Level.INFO, "  Got recovery matrix\r\n" + m2.toString());

        Matrix recovered = m2.mul(damaged);
        LOGGER.log(Level.INFO, "  Got recovered data\r\n" + recovered.toString());
        assertTrue("data and recovered data is not equal\r\n" + data + "\r\n" + recovered, recovered.equals(data));
      }
    }
  }

  public static void main(String[] args) {
    SecureRandom sr = new SecureRandom();
    int galois = 8;
    MathMode mm = GaloisFieldMathMode.getGaloisFieldMathMode(8);
    for (int k = 0; k < 100; k++) {
      LOGGER.log(Level.INFO, "testing redundancy matrix for GF(2^" + galois + ") run " + k);
      // generate random data vector
      Matrix data = new Matrix(1, sr.nextInt(7) + 1, mm);
      for (int i = 0; i < data.getY(); i++) {
        data.setField(0, i, (int) (Math.random() * Math.pow(2, galois)));
      }
      // determine total blocks (including redundancy)
      int tot = sr.nextInt(7) + 1 + data.getY();
      // determine bad/missing block indexes
      int[] missing = new int[sr.nextInt(tot - data.getY()) + 1];
      for (int i = 0; i < missing.length; i++) {
        boolean duplicated = true;
        while (duplicated) {
          missing[i] = sr.nextInt(tot + 1);
          duplicated = false;
          if (i > 0) {
            for (int j = 0; j < i && !duplicated; j++) {
              if (missing[i] == missing[j]) {
                duplicated = true;
              }
            }
          }
        }
      }
      Arrays.sort(missing);

      LOGGER.log(Level.INFO, "  test parameter: dataRows=" + data.getY() + "; totalRows=" + tot + "; missingRows=" + missing.length);

      LOGGER.log(Level.INFO, "  Got data vector\r\n" + data);

      RedundancyMatrix m1 = new RedundancyMatrix(data.getY(), tot, mm,true,true);
      LOGGER.log(Level.INFO, "  Got redundancy matrix for GF(2^" + galois + ")\r\n" + m1);
      RedundancyMatrix dataRowsOfRedundancyMatrix = new RedundancyMatrix(m1);
      while (dataRowsOfRedundancyMatrix.getX() != dataRowsOfRedundancyMatrix.getY()) {
        dataRowsOfRedundancyMatrix.removeRow(dataRowsOfRedundancyMatrix.getX());
      }

      Matrix red = m1.mul(data);
      LOGGER.log(Level.INFO, "  Got data with redundancy vector\r\n" + red.toString());

      Matrix damaged = new Matrix(red);
      for (int i = missing.length - 1; i >= 0; i--) {
        red.removeRow(missing[i]);
        damaged.removeRow(missing[i]);
      }
      while (damaged.getY() != data.getY()) {
        damaged.removeRow(data.getY());
      }
      LOGGER.log(Level.INFO, "  Got damaged vector\r\n" + damaged);

      Matrix m2 = m1.getRecoveryMatrix(missing);
      LOGGER.log(Level.INFO, "  Got recovery matrix\r\n" + m2.toString());

      Matrix recovered = m2.mul(damaged);
      LOGGER.log(Level.INFO, "  Got recovered data\r\n" + recovered.toString());
      assertTrue("data and recovered data is not equal\r\n" + data + "\r\n" + recovered, recovered.equals(data));
    }
  }

}
