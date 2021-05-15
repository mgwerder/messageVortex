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

    /** samples from https://math.stackexchange.com/questions/1067963/looking-for-gf16-gf32-gf-256-tables **/
    private static final GaloisEntry[] galoisList8 = {
            new GaloisEntry(GaloisTableType.LOG, 0, -1),
            new GaloisEntry(GaloisTableType.ILOG, 0, 1),
            new GaloisEntry(GaloisTableType.ILOG, 255, -1),
            new GaloisEntry(GaloisTableType.LOG, 255, 175),
            new GaloisEntry(GaloisTableType.ILOG, 8, 29),
    };

    /** samples from https://math.stackexchange.com/questions/1067963/looking-for-gf16-gf32-gf-256-tables **/
    private static final GaloisEntry[] galoisList16 = {
            new GaloisEntry(GaloisTableType.LOG, 0, -1),
            new GaloisEntry(GaloisTableType.ILOG, 0, 1),
            new GaloisEntry(GaloisTableType.ILOG, 65535, -1),
            new GaloisEntry(GaloisTableType.LOG, 65535, 23025)
    };

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
