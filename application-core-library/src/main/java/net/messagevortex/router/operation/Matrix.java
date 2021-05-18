package net.messagevortex.router.operation;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * <p>Offers matrixContent calculations in different fields.</p>
 */
public class Matrix {

    /* may be set to disable cache */
    static boolean matrixCacheDisabled = false;

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    static final int X = 0;
    static final int Y = 1;

    public static final int MAX_CACHE = 30;
    private static final Map<String, Matrix> matrixCache = new LinkedHashMap<>();


    int[] matrixContent;
    int[] dimension = new int[2];
    int modulo = Integer.MAX_VALUE;
    MathMode mode;

    /**
     * <p>Creates a copy of the current matrix object.</p>
     *
     * @param originalMatrix the matrix to be copied
     */
    public Matrix(Matrix originalMatrix) {
        this(originalMatrix.getX(), originalMatrix.getY(), originalMatrix.mode,
                originalMatrix.matrixContent);
        this.modulo = originalMatrix.modulo;
        // make sure that any dimension is copied
        this.dimension = Arrays.copyOf(dimension, dimension.length);
        // copy matrix content
        this.matrixContent = Arrays.copyOf(matrixContent, matrixContent.length);
    }

    /***
     * <p>Creates a matrixContent (x,y) with the specified MathMode.</p>
     *
     * @param x      the number of columns of the matrixContent
     * @param y      the number of rows of the matrixContent
     * @param mode   the math mode to be applied
     */
    public Matrix(int x, int y, MathMode mode) {
        if (x < 1 || y < 1) {
            throw new IllegalArgumentException("null or negative matrix size exception (" + x + "/" + y + ")");
        }
        dimension[X] = x;
        dimension[Y] = y;
        matrixContent = new int[x * y];
        if (mode != null) {
            this.mode = mode;
        }
    }

    /***
     * <p>Creates a two dimensional matrixContent (x,y) with the specified MathMode.</p>
     *
     * @param x       the number of columns of the matrixContent
     * @param y       the number of rows of the matrixContent
     * @param mode    the math mode to be applied
     * @param content the content as one dimensional array (sequence of rows)
     */
    public Matrix(int x, int y, MathMode mode, int[] content) {
        this(x, y, mode);
        matrixContent = Arrays.copyOf(content, content.length);
    }

    /***
     * <p>Creates a two dimensional matrixContent (x,y) with the specified MathMode.</p>
     *
     * @param x       the number of columns of the matrixContent
     * @param y       the number of rows of the matrixContent
     * @param mode    the math mode to be applied
     * @param content the content as one dimensional array (sequence of rows)
     */
    public Matrix(int x, int y, MathMode mode, byte[] content) {
        this(x, y, mode);
        matrixContent = new int[content.length];
        for (int i = 0; i < content.length; i++) {
            matrixContent[i] = content[i] & 0xFF;
        }
    }

    /***
     * <p>Creates a two dimensional matrixContent (x,y) with the specified MathMode.</p>
     *
     * @param x       the number of columns of the matrixContent
     * @param y       the number of rows of the matrixContent
     * @param mode    the math mode to be applied
     * @param content the value to be set in all fields
     */
    public Matrix(int x, int y, MathMode mode, int content) {
        this(x, y, mode);
        for (int i = 0; i < x * y; i++) {
            matrixContent[i] = content;
        }
    }

    /***
     * <p>Creates a two dimensional unit matrixContent (size,size) with the specified MathMode.</p>
     *
     * @param size    the number of columns of the matrixContent
     * @param mode    the math mode to be applied
     * @return the requested matrix
     */
    public static Matrix unitMatrix(int size, MathMode mode) {
        if (!matrixCacheDisabled) {
            Matrix m = getCache("um" + size + "/" + mode);
            if (m != null) {
                return new Matrix(m);
            }
        }
        Matrix ret = new Matrix(size, size, mode);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x != y) {
                    ret.matrixContent[y * size + x] = 0;
                } else {
                    ret.matrixContent[y * size + x] = 1;
                }
            }
        }
        addCache("um" + size + "/" + mode, new Matrix(ret));
        return ret;
    }

    static void addCache(String key, Matrix m) {
        synchronized (matrixCache) {
            if (matrixCacheDisabled) {
                return;
            }
            if (matrixCache.containsKey(key)) {
                return;
            }
            while (matrixCache.size() > MAX_CACHE) {
                matrixCache.remove(matrixCache.entrySet().iterator().next().getKey());
            }
            matrixCache.put(key, m);
        }
    }

    static Matrix getCache(String key) {
        synchronized (matrixCache) {
            return matrixCache.get(key);
        }
    }

    /***
     * <p>Get the number of columns.</p>
     *
     * @return the number of columns as int value
     */
    public int getX() {
        return dimension[X];
    }

    /***
     * <p>Set the number of rows.</p>
     *
     * @return the number of rows as int value
     */
    public int getY() {
        return dimension[Y];
    }

    /***
     * <p>Removes the specified row from the matrixContent.</p>
     *
     * @param index the index of the row to be removed (starting with 0)
     */
    public void removeRow(int index) {
        int[] newMatrix = Arrays.copyOf(matrixContent, getX() * (getY() - 1));
        for (int i = (1 + index) * getX(); i < matrixContent.length; i++) {
            newMatrix[i - getX()] = matrixContent[i];
        }
        dimension[Y]--;
        matrixContent = newMatrix;
    }

    /***
     * <p>Returns a matrixContent with the specified dimension initialised with random values.</p>
     *
     * @param x       the number of columns of the matrixContent
     * @param y       the number of rows of the matrixContent
     * @param mode    the math mode to be applied
     * @return the generated matrixContent
     */
    public static Matrix randomMatrix(int x, int y, MathMode mode) {
        Matrix ret = new Matrix(x, y, mode);
        for (int xl = 0; xl < x; xl++) {
            for (int yl = 0; yl < y; yl++) {
                ret.matrixContent[x * yl + xl] = ExtendedSecureRandom.nextInt(Integer.MAX_VALUE);
            }
        }
        return ret;
    }

    /***
     * <p>Multiplies the current matrixContent with the specified matrixContent.</p>
     *
     * @param m                    the matrixContent to multiply with
     * @return the resulting matrixContent
     * @throws ArithmeticException if multiplication may not be carried out
     */
    public Matrix mul(Matrix m) {
        if (!this.mode.equals(m.mode)) {
            throw new ArithmeticException("illegal matrixContent math mode");
        }
        if (this.getX() != m.getY()) {
            throw new ArithmeticException("illegal matrixContent size");
        }
        Matrix ret = new Matrix(m.getX(), getY(), mode);
        for (int x = 0; x < m.dimension[X]; x++) {
            for (int y = 0; y < this.dimension[Y]; y++) {
                ret.matrixContent[y * ret.dimension[X] + x] = 0;
                for (int i = 0; i < m.dimension[Y]; i++) {
                    ret.matrixContent[y * ret.dimension[X] + x] = mode.add(
                            ret.matrixContent[y * ret.dimension[X] + x],
                            mode.mul(
                                    this.matrixContent[y * this.dimension[X] + i],
                                    m.matrixContent[i * m.dimension[X] + x]
                            )
                    );
                }
                ret.matrixContent[y * ret.dimension[X] + x] =
                        ret.matrixContent[y * ret.dimension[X] + x] % modulo;
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Matrix)) {
            return false;
        }
        Matrix m = (Matrix) o;
        if (m.dimension.length != dimension.length) {
            return false;
        }
        for (int i = 0; dimension.length > i; i++) {
            if (dimension[i] != m.dimension[i]) {
                return false;
            }
        }
        for (int i = 0; m.matrixContent.length > i; i++) {
            if (matrixContent[i] != m.matrixContent[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int y = 0; y < dimension[Y]; y++) {
            sb.append('{');
            for (int x = 0; x < dimension[X]; x++) {
                if (x > 0) {
                    sb.append(',');
                }
                sb.append(matrixContent[y * dimension[X] + x]);
            }
            sb.append('}');
            if (y < dimension[Y] - 1) {
                sb.append(",\n");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /***
     * <p>Sets a modulo value of the matrixContent for all operations.</p>
     *
     * @param i   the value to be used as modulo
     * @return the previously set modulo
     */
    public int setModulo(int i) {
        int ret = modulo;
        if (i < 1) {
            modulo = Integer.MAX_VALUE;
        }
        modulo = i;
        return ret;
    }

    /***
     * <p>Get a row of the matrixContent as array.</p>
     *
     * @param i  the index of the row to be extracted as array
     * @return the row representation as array
     */
    public int[] getRow(int i) {
        if (i < 0 || i >= dimension[Y]) {
            throw new ArithmeticException("row index out of range 0<=i<" + dimension[X]);
        }
        int[] ret = new int[dimension[X]];
        for (int j = 0; j < ret.length; j++) {
            ret[j] = getField(j, i);
        }
        return ret;
    }

    /***
     * <p>Extracts the value of a specified matrixContent field.</p>
     *
     * @param x  the column of the field
     * @param y  the row of the field
     * @return the value of the field
     */
    public int getField(int x, int y) {
        checkField(x, y);
        return matrixContent[y * getX() + x];
    }

    /***
     * <p>Sets the value of a specified matrixContent field.</p>
     *
     * @param x     the column of the field
     * @param y     the row of the field
     * @param value the value to be set
     * @return the previously set value of the field
     */
    public int setField(int x, int y, int value) {
        checkField(x, y);
        int old = getField(x, y);
        matrixContent[y * getX() + x] = value;
        return old;
    }

    private void checkField(int x, int y) {
        if (x < 0 || x >= dimension[X]) {
            throw new ArithmeticException("column index out of range 0<=col[" + x + "]<" + dimension[X]);
        }
        if (y < 0 || y >= dimension[Y]) {
            throw new ArithmeticException("row index out of range 0<=row[" + y + "]<" + dimension[Y]);
        }
    }

    /***
     * <p>Calculates the inverse by applying the Gauss-Jordan-algorithm.</p>
     *
     * @return the inverse of the matrixContent
     * @throws ArithmeticException if matrixContent is not square in dimensions or the algorithm
     *                             was unable to compute an inverse
     */
    public Matrix getInverse() {
        if (dimension[X] != dimension[Y]) {
            throw new ArithmeticException("matrixContent to inverse must have square dimensions "
                    + "(dimension is " + getX() + "/" + getY() + ")");
        }
        long startTime = System.currentTimeMillis();
        Matrix red = new Matrix(this);
        Matrix ret = Matrix.unitMatrix(dimension[X], mode);
        for (int row = 0; row < getY(); row++) {
            // flip rows if required
            int scalar = red.getField(row, row);
            if (scalar == 0) {
                // search next row!=0 at diagonal position
                int flipRow = row + 1;
                while (flipRow < getY() && red.getField(row, flipRow) == 0) {
                    flipRow++;
                }
                if (flipRow == getY()) {
                    throw new ArithmeticException("unable to inverse matrixContent (in flip row)");
                }

                // flip rows
                LOGGER.log(Level.FINEST, "  processing flip row", new Object[]{row, flipRow});
                red.flipRow(row, flipRow);
                ret.flipRow(row, flipRow);
            }


            // make diagonal 1 and left 0 in red
            scalar = red.getField(row, row);
            if (scalar != 1) {
                red.divRow(row, scalar);
                ret.divRow(row, scalar);
            }

            for (int row2 = row + 1; row2 < dimension[Y]; row2++) {
                scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar, false);
                    ret.transformRow(row2, row, scalar, false);
                }
            }

        }
        for (int row = 1; row < dimension[X]; row++) {
            // make right 0 in red
            for (int row2 = row - 1; row2 >= 0; row2--) {
                int scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar, false);
                    ret.transformRow(row2, row, scalar, false);
                }
            }
        }
        if (!Matrix.unitMatrix(red.dimension[X], mode).equals(red)) {
            throw new ArithmeticException("unable to calculate inverse");
        }
        long time = System.currentTimeMillis() - startTime;
        LOGGER.log(Level.FINE,"Got inverse matrix (" + dimension[X] + "/" + dimension[Y] + ") in " + ((double)(time) / (double)1000) + "s");
        return ret;
    }

    /***
     * <p>Get the values of a row as byte arrays.</p>
     *
     * @param row the index of the row to be used (starting with 0)
     * @return the array containing the values of the row
     */
    public byte[] getRowAsByteArray(int row) {
        byte[] ret = new byte[getX()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) (getField(i, row));
        }
        return ret;
    }

    /***
     * <p>Get the content of the matrixContent as byte array.</p>
     *
     * @return the byte array representing the matrixContent values (row by row)
     */
    public byte[] getAsByteArray() {
        byte[] ret = new byte[getX() * getY()];
        for (int y = 0; y < getY(); y++) {
            for (int x = 0; x < getX(); x++) {
                ret[y * getX() + x] = (byte) (getField(x, y));
            }
        }
        return ret;
    }

    /***
     * <p>Multiplies element by element the values of the second column by the specified scalar
     * and subtracts the resulting value from the first element.</p>
     *
     * @param col    the column to be recalculated/altered
     * @param col2   the column to be used for recalculation
     * @param scalar the scalar to be used for division/multiplication
     */
    public void transformColumn(int col, int col2, int scalar) {
        if (col < 0 || col > getX()) {
            throw new ArithmeticException("first column is out of range");
        }
        if (col2 > getX()) {
            throw new ArithmeticException("second column is out of range");
        }
        for (int row = 0; row < getY(); row++) {
            int value1 = getField(col, row);
            int newValue;
            if (col2 < 0) {
                newValue = mode.div(value1, scalar);
            } else {
                int value2 = getField(col2, row);
                newValue = mode.sub(value1, mode.mul(value2, scalar));
            }
            setField(col, row, newValue);
        }
    }

    /***
     * <p>Divides or multiplies element by element the values of the second row by the specified
     * scalar and subtracts the resulting value from the first element.</p>
     *
     * @param row    the row o be recalculated/altered
     * @param row2   the row to be used for recalculation
     * @param scalar the scalar to be used for division/multiplication
     * @param doDiv  flag to specify whether division (true) or multiplication (false) should be used
     */
    public void transformRow(int row, int row2, int scalar, boolean doDiv) {
        if (row < 0 || row > getY()) {
            throw new ArithmeticException("first row is out of range");
        }
        if (row2 < 0 || row2 > getX()) {
            throw new ArithmeticException("second row is out of range");
        }
        for (int col = 0; col < getX(); col++) {
            int value1 = getField(col, row);
            int value2 = getField(col, row2);
            int tempValue;
            if (doDiv) {
                tempValue = mode.div(value2, scalar);
            } else {
                tempValue = mode.mul(value2, scalar);
            }
            int newValue = mode.sub(value1, tempValue);
            setField(col, row, newValue);
        }
    }

    /***
     * <p>Divides all values of the specified row in the matrixContent by the scalar specified.</p>
     *
     * @param row     the index of the row (starting with 0)
     * @param scalar  the scalar to be used as divisor
     */
    public void divRow(int row, int scalar) {
        if (row < 0 || row > getY()) {
            throw new ArithmeticException("first row is out of range");
        }

        for (int col = 0; col < getX(); col++) {
            setField(col, row, mode.div(getField(col, row), scalar));
        }
    }

    /***
     * <p>Flips two rows of the current matrixContent.</p>
     *
     * @param row1 index of the first row (starting with 0)
     * @param row2 index of the second row (starting with 0)
     */
    public void flipRow(int row1, int row2) {
        int tmp;
        for (int i = 0; i < getX(); i++) {
            tmp = getField(i, row1);
            setField(i, row1, getField(i, row2));
            setField(i, row2, tmp);
        }
    }

    /***
     * <p>Enables or disables the matrixContent cache.</p>
     *
     * @param enable  set to true if matrixContent cache should be enabled
     * @return the previously set value
     */
    public static boolean enableMatrixCache(boolean enable) {
        boolean old = !matrixCacheDisabled;
        matrixCacheDisabled = (!enable);
        return old;
    }

}
