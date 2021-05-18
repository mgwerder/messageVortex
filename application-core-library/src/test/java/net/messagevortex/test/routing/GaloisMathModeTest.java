package net.messagevortex.test.routing;

import net.messagevortex.router.operation.GaloisFieldMathMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GaloisMathModeTest {

    private enum GaloisTableType {
        LOG,
        ILOG;
    }

    private static class GaloisEntry {

        private GaloisTableType type;
        private int entry;
        private long expected;

        public GaloisEntry(GaloisTableType type, int entry, long expected) {
            this.type = type;
            this.entry = entry;
            this.expected = expected;
        }

        public boolean isLog() {
            return this.type == GaloisTableType.LOG;
        }

        public int getEntry() {
            return this.entry;
        }

        public long getExpected() {
            return this.expected;
        }

    }

    private static final GaloisEntry[] galoisList8 = {
            new GaloisEntry(GaloisTableType.LOG, 0, -1),
            new GaloisEntry(GaloisTableType.ILOG, 0, 1),
            new GaloisEntry(GaloisTableType.ILOG, 255, -1),
            new GaloisEntry(GaloisTableType.LOG, 255, 175),
            new GaloisEntry(GaloisTableType.ILOG, 8, 29),
    };

    private static final GaloisEntry[] galoisList16 = {
            new GaloisEntry(GaloisTableType.LOG, 0, -1),
            new GaloisEntry(GaloisTableType.ILOG, 0, 1),
            new GaloisEntry(GaloisTableType.ILOG, 65535, -1),
            new GaloisEntry(GaloisTableType.LOG, 65535, 4725)
    };

    @Test
    public void tableGenericBitTest() {
        int[] max = new int[]{
                2, 5, 12, 15, 58, 121, 175,
                385, 949, 1020, 4079, 7351, 15744, 32753,4725,
                0,0,0,0,0,0,0,545708
        };
        for (int i = 0; i < 16; i++) {
            if(i==15) {
                i=22;
            }
            GaloisFieldMathMode mm = new GaloisFieldMathMode(i + 2);
            Assertions.assertEquals(-1, mm.getGfLog()[0],
                    "table entry 0 (" + (i + 2) + ") LOG");
            Assertions.assertEquals(1, mm.getGfIlog()[0],
                    "table entry 0 (" + (i + 2) + ") iLOG");
            int l = (int) (Math.pow(2, i + 2));
            Assertions.assertEquals(max[i], mm.getGfLog()[l - 1],
                    "table entry " + l + " (" + (i + 2) + ") LOG");
            Assertions.assertEquals(-1, mm.getGfIlog()[l - 1],
                    "table entry " + l + " (" + (i + 2) + ") iLOG");
        }
    }

    @Test
    public void tableEightBitTest() {
        GaloisFieldMathMode mm = new GaloisFieldMathMode(8);
        System.out.println(mm.getTableDump());
        for (GaloisEntry ge : galoisList8) {
            Assertions.assertEquals(ge.getExpected(),
                    ge.isLog() ? mm.getGfLog()[ge.getEntry()] : mm.getGfIlog()[ge.getEntry()],
                    "table entry " + (ge.isLog() ? "LOG" : "iLOG")
            );
        }
    }

    @Test
    public void tableSixteenBitTest() {
        GaloisFieldMathMode mm = new GaloisFieldMathMode(16);
        System.out.println(mm.getTableDump());
        for (GaloisEntry ge : galoisList16) {
            Assertions.assertEquals(ge.getExpected(),
                    ge.isLog() ? mm.getGfLog()[ge.getEntry()] : mm.getGfIlog()[ge.getEntry()],
                    "table entry " + (ge.isLog() ? "LOG" : "iLOG")
            );
        }
    }

}
