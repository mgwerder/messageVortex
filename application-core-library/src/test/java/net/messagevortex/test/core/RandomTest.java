package net.messagevortex.test.core;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;


/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class RandomTest {

  @Test
  public void linearRandomnessTest() {
    // init classes
    long[] classes = new long[100];
    for (int i = 0; i < classes.length; i++) {
      classes[i] = 0;
    }

    // get random numbers
    int mul = 100000;
    for (int i = 0; i < classes.length * mul; i++) {
      classes[ExtendedSecureRandom.nextInt(classes.length)]++;
    }

    System.out.println(plot(classes));
    // check result (must be all within 10%)
    for (int i = 0; i < classes.length; i++) {
      Assertions.assertTrue(classes[i] > (0.90 * mul) && classes[i] < (1.1 * mul), "failed at pos " + i + " (result:" + classes[i] + ")");
    }
  }

  @Test
  public void gaussRandomnessTest() {
    long[] sample = new long[] {582, 646, 839, 885, 1057, 1216, 1365, 1472, 1741, 1926, 2203, 2491, 2807, 3128, 3500, 3854, 4390, 4676, 5183, 5830, 6238, 6795, 7377, 7984, 8589, 9157, 9836, 10656, 11204, 12191, 12935, 13432, 14394, 14937, 15633, 16450, 17341, 17638, 18357, 19009, 19425, 20195, 20629, 20950, 21062, 21589, 21569, 21941, 22006, 22327, 21954, 21951, 21541, 21456, 21120, 20774, 20246, 20022, 19494, 18966, 18142, 17677, 17225, 16489, 15563, 15047, 14232, 13637, 12798, 12048, 11459, 10675, 10070, 9148, 8568, 7875, 7443, 6718, 6140, 5557, 5341, 4652, 4308, 3867, 3552, 3140, 2905, 2470, 2256, 1994, 1751, 1487, 1336, 1253, 1046, 974, 781, 657, 558, 0};
    // init classes
    long[] classes = new long[100];
    for (int i = 0; i < classes.length; i++) {
      classes[i] = 0;
    }

    // get random numbers
    int mul = 100000;
    for (int i = 0; i < classes.length * mul; i++) {
      classes[(int) (ExtendedSecureRandom.nextGauss() * (classes.length - 1))]++;
    }

    System.out.println(plot(classes));
    // check result (must be all within 20%)
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (int i = 0; i < classes.length; i++) {
      sb.append(classes[i]).append(',');
      Assertions.assertTrue(classes[i] >= (0.8 * sample[i] * mul / 10000) && classes[i] <= (1.2 * sample[i] * mul / 10000), "failed at pos " + i + " (result:" + classes[i] + "; expect:" + sample[i] + ")");
    }
  }

  @Test
  public void randomTimeTest() {
    long[] sample = new long[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 15, 26, 69, 129, 277, 536, 958, 1791, 3160, 5600, 9422, 15507, 25002, 38857, 58724, 87181, 124831, 174392, 236271, 311747, 398559, 498277, 603739, 713380, 823227, 916652, 997760, 1053790, 1084019, 1086509, 1082256, 1075075, 1062300, 1043252, 1027942, 1002467, 975186, 944444, 911750, 877160, 839900, 801388, 761587, 721938, 679272, 639137, 598583, 558526, 517693, 479282, 440742, 404022, 370816, 336002, 306397, 277229, 248579, 222382, 198983, 176425, 156839, 137968, 121895, 106666, 93289, 80478, 69562, 60133, 51379, 44113, 37804, 31822, 26945, 22577, 18985, 16134, 13326, 11018, 9116, 7527, 6148, 4893, 4114, 3310, 2670, 2195, 1658, 1353, 1136, 818, 674, 540, 431, 342, 226, 211, 157, 128, 83, 62, 44, 29, 24, 27, 11, 6, 7, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    // init classes
    long[] classes = new long[300];
    for (int i = 0; i < classes.length; i++) {
      classes[i] = 0;
    }

    // get random numbers
    int mul = 10000;
    for (int i = 0; i < classes.length * mul; i++) {
      if (i % (mul*classes.length/100) == 0) {
        MessageVortexLogger.getGlobalLogger().log(Level.INFO, "  did get " + i + "/" + (mul*classes.length) + " random values ("+(100*i/(mul*classes.length))+"%)");
      }
      double d = -1;
      while (d == -1 || (int) (d) > classes.length - 1) {
        d = ExtendedSecureRandom.nextRandomTime(90, 120, 200);
        Assertions.assertTrue(d >= 90, "value below start");
        Assertions.assertTrue(d <= 200, "value above end (expected max is 200; got: " + d + ")");
      }
      classes[(int) (d)]++;
    }
    System.out.println(plot(classes));

    // output latex code
    System.out.println("%% latex");
    System.out.println("\\begin{tikzpicture}");
    System.out.println("\\begin{axis}[ytick={0,10000},yticklabels={min,max},xtick={90,100,200},xticklabels={$90$,$100$,$200$}]");
    System.out.println("\\addplot[smooth] coordinates {");
    for (int i = 0; i < classes.length; i++) {
      System.out.print("(" + i + "," + classes[i] + ")");
      if (i % 20 == 19) {
        System.out.println();
      }
    }
    System.out.println("};");
    System.out.println("\\end{axis}");
    System.out.println("\\end{tikzpicture}");

    // check result (must be all within 5%)
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (int i = 0; i < classes.length; i++) {
      if (i != 0) {
        sb.append(',');
      }
      sb.append(classes[i]);
      long t = (long) (Math.max(280, sample[i] * mul / 100000 * 0.2));
      Assertions.assertTrue(classes[i] >= (sample[i] * mul / 100000 - t) && classes[i] <= (sample[i] * mul / 100000 + t), "failed at pos " + i + " (result:" + classes[i] + "; expect:" + sample[i] * mul / 100000 + ")");
    }
    System.out.println(sb + "}");
  }

  private String plot(long[] classes) {
    final int X = 100;
    final int Y = 30;
    char[][] grid = new char[X][Y];
    for (int i = 0; i < X; i++) {
      for (int j = 0; j < Y; j++) {
        grid[i][j] = ' ';
      }
    }
    long maxY = 0;
    double interval = (double) (classes.length) / X;
    for (int i = 0; i < X; i++) {
      int avg = 0;
      int cnt = 0;
      int start = (int) (i * interval);
      for (int j = start; j < start + interval; j++) {
        avg += classes[j];
        cnt++;
      }
      long d = (long) (avg / cnt);
      maxY = maxY > d ? maxY : d;
    }

    for (int i = 0; i < X; i++) {
      int avg = 0;
      int cnt = 0;
      int start = (int) (i * interval);
      for (int j = start; j < start + interval; j++) {
        avg += classes[j];
        cnt++;
      }
      grid[i][(int) (((double) (avg) / cnt / maxY) * (Y - 1))] = '#';
    }
    StringBuilder sb = new StringBuilder();
    for (int j = Y - 1; j >= -1; j--) {
      for (int i = -1; i < X; i++) {
        if (j == -1 && i == -1) {
          sb.append('+');
        } else if (j == -1) {
          sb.append('-');
        } else if (i == -1) {
          sb.append('|');
        } else {
          sb.append(grid[i][j]);
        }
      }
      sb.append("\r\n");
    }
    return "maxY=" + maxY / interval + "\r\n" + sb;

  }


}
