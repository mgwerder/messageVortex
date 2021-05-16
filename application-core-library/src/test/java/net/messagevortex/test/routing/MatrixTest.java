package net.messagevortex.test.routing;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.router.operation.GaloisFieldMathMode;
import net.messagevortex.router.operation.MathMode;
import net.messagevortex.router.operation.Matrix;
import net.messagevortex.router.operation.RealMathMode;
import net.messagevortex.router.operation.RedundancyMatrix;
import net.messagevortex.router.operation.VandermondeMatrix;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;


@ExtendWith(GlobalJunitExtension.class)
public class MatrixTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    public void basicMatrixTest() {
        LOGGER.log(Level.INFO, "basic multiplication test (unit matrices)");
        for (int i = 1; i <= 10; i++) {
            LOGGER.log(Level.INFO, "  Testing unit multiplication with size " + i);
            Matrix unit = Matrix.unitMatrix(i, RealMathMode.getRealMathMode());
            Assertions.assertTrue(unit.mul(unit).equals(unit), "error multiplying unit matrices (" + i + ")\n" + unit);
            for (int j = 0; j < 11; j++) {
                int size = (int) (Math.random() * 10) + 1;
                Matrix m = Matrix.randomMatrix(i, size, RealMathMode.getRealMathMode());
                LOGGER.log(Level.INFO, "  Testing unit multiplication with random matrix size (" + i + "/" + size + "; run is " + j + ")");
                unit = Matrix.unitMatrix(i, RealMathMode.getRealMathMode());
                Assertions.assertTrue(m.mul(unit).equals(m), "error multiplying random matrices (" + i + "/" + size + ")\n" + m + "\n=\n" + m.mul(unit));
            }
        }
    }

    @Test
    public void vandermondeMatrixTest() {
        LOGGER.log(Level.INFO, "VandermondeMatrix blackbox tests");
        Matrix m = new VandermondeMatrix(3, 6, GaloisFieldMathMode.getGaloisFieldMathMode(4));
        LOGGER.log(Level.INFO, "Matrix is \n" + m);
        Assertions.assertTrue(Arrays.equals(new int[]{1, 0, 0}, m.getRow(0)), "Illegal row 0 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(0)) + "]");
        Assertions.assertTrue(Arrays.equals(new int[]{1, 1, 1}, m.getRow(1)), "Illegal row 1 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(1)) + "]");
        Assertions.assertTrue(Arrays.equals(new int[]{1, 2, 4}, m.getRow(2)), "Illegal row 2 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(2)) + "]");
        Assertions.assertTrue(Arrays.equals(new int[]{1, 3, 5}, m.getRow(3)), "Illegal row 0 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(3)) + "]");
        Assertions.assertTrue(Arrays.equals(new int[]{1, 4, 3}, m.getRow(4)), "Illegal row 1 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(4)) + "]");
        Assertions.assertTrue(Arrays.equals(new int[]{1, 5, 2}, m.getRow(5)), "Illegal row 2 of GF(2^4) VandermodeMatrix [" + Arrays.toString(m.getRow(5)) + "]");
    }

    @Test
    public void redundancyMatrixTest() {
        final int[] MAX_GALOIS = new int[]{4, 8, 16};
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
                Assertions.assertTrue(Matrix.unitMatrix(dataRowsOfRedundancyMatrix.getX(), mm).equals(dataRowsOfRedundancyMatrix), "data rows in redundancy matrix are not unit rows \n" + Matrix.unitMatrix(dataRowsOfRedundancyMatrix.getX(), mm) + "\n" + dataRowsOfRedundancyMatrix);

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
                Assertions.assertTrue(recovered.equals(data), "data and recovered data is not equal\r\n" + data + "\r\n" + recovered);
            }
        }
    }

    @Test
    public void dataRecoveryTest() {
        SecureRandom sr = new SecureRandom();
        for (int galois : new int[]{8, 16}) {
            MathMode mm = GaloisFieldMathMode.getGaloisFieldMathMode(galois);
            for (int k = 0; k < 10; k++) {
                LOGGER.log(Level.INFO, "testing redundancy matrix for GF(2^" + galois + ") run " + k);
                // generate random data vector
                Matrix data = new Matrix(sr.nextInt(4096), sr.nextInt((int) Math.min((Math.pow(2, galois) - 8), 500)), mm);
                for (int x = 0; x < data.getX(); x++) {
                    for (int y = 0; y < data.getY(); y++) {
                        data.setField(x, y, (int) (Math.random() * Math.pow(2, galois)));
                    }
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
                LOGGER.log(Level.INFO, "  test parameter: dataRows=" + data.getY() + "; totalRows=" + tot
                        + "; missingRows=" + missing.length + "; data size="+(data.getX()* data.getY()/1024)+"kb");
                LOGGER.log(Level.INFO, "  Got data vector");

                RedundancyMatrix m1 = new RedundancyMatrix(data.getY(), tot, mm, true, true);
                LOGGER.log(Level.INFO, "  Got redundancy matrix for GF(2^" + galois + ")");
                RedundancyMatrix dataRowsOfRedundancyMatrix = new RedundancyMatrix(m1);
                while (dataRowsOfRedundancyMatrix.getX() != dataRowsOfRedundancyMatrix.getY()) {
                    dataRowsOfRedundancyMatrix.removeRow(dataRowsOfRedundancyMatrix.getX());
                }

                Matrix red = m1.mul(data);
                LOGGER.log(Level.INFO, "  Got data with redundancy vector");

                Matrix damaged = new Matrix(red);
                for (int i = missing.length - 1; i >= 0; i--) {
                    red.removeRow(missing[i]);
                    damaged.removeRow(missing[i]);
                }
                while (damaged.getY() != data.getY()) {
                    damaged.removeRow(data.getY());
                }
                LOGGER.log(Level.INFO, "  Got damaged vector... calculating recovery matrix");

                Matrix m2 = m1.getRecoveryMatrix(missing);
                LOGGER.log(Level.INFO, "  Got recovery matrix");

                Matrix recovered = m2.mul(damaged);
                LOGGER.log(Level.INFO, "  Got recovered data");
                Assertions.assertTrue(recovered.equals(data), "data and recovered data is not equal");
            }
        }
    }

}
