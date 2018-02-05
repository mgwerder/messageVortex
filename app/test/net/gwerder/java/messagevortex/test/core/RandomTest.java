package net.gwerder.java.messagevortex.test.core;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

/**
 * booleanConfigHandlings for {@link net.gwerder.java.messagevortex.MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class RandomTest {

    @Test
    public void linearRandomnessTest() {
        // init classes
        int[] classes = new int[100];
        for( int i = 0; i< classes.length; i++ ) {
            classes[i]=0;
        }

        // get random numbers
        int mul = 10000;
        for( int i=0; i<classes.length*mul; i++ ) {
            classes[ExtendedSecureRandom.nextInt(classes.length)]++;
        }

        System.out.println( plot( classes ) );
        // check result (must be all within 5%)
        for( int i = 0; i< classes.length; i++ ) {
            assertTrue("failed at pos " + i + " (result:" + classes[i] + ")", classes[i] > (0.95 * mul) && classes[i] < (1.05 * mul) );
        }
    }

    @Test
    public void gaussRandomnessTest() {
        int[] sample = new int[] {582,646,839,885,1057,1216,1365,1472,1741,1926,2203,2491,2807,3128,3500,3854,4390,4676,5183,5830,6238,6795,7377,7984,8589,9157,9836,10656,11204,12191,12935,13432,14394,14937,15633,16450,17341,17638,18357,19009,19425,20195,20629,20950,21062,21589,21569,21941,22006,22327,21954,21951,21541,21456,21120,20774,20246,20022,19494,18966,18142,17677,17225,16489,15563,15047,14232,13637,12798,12048,11459,10675,10070,9148,8568,7875,7443,6718,6140,5557,5341,4652,4308,3867,3552,3140,2905,2470,2256,1994,1751,1487,1336,1253,1046,974,781,657,558,0};
        // init classes
        int[] classes = new int[100];
        for( int i = 0; i< classes.length; i++ ) {
            classes[i]=0;
        }

        // get random numbers
        int mul = 10000;
        for( int i=0; i<classes.length*mul; i++ ) {
            classes[(int)(ExtendedSecureRandom.nextGauss()*(classes.length-1))]++;
        }

        System.out.println( plot( classes ) );
        // check result (must be all within 5%)
        StringBuilder sb=new StringBuilder();
        sb.append('{');
        for( int i = 0; i< classes.length; i++ ) {
            sb.append(classes[i]).append(',');
            assertTrue("failed at pos " + i + " (result:" + classes[i] + "; expect:"+sample[i]+")", classes[i] >= (0.8 * sample[i]) && classes[i] <= (1.2 * sample[i]) );
        }
   }

    private String plot( int[] classes ) {
        final int X = 100;
        final int Y = 30;
        char[][] grid = new char[X][Y];
        for( int i=0; i<X; i++ ) {
            for( int j=0; j<Y; j++ ) {
                grid[i][j]=' ';
            }
        }
        int maxY=0;
        for( int i=0; i<classes.length; i++ ) {
            maxY=Math.max(maxY,classes[i]);
        }
        for( int i=0; i<X; i++ ) {
            int avg=0;
            int cnt=0;
            for(int j = X*i/classes.length; j<X*(i+1)/classes.length; j++ ) {
                avg+=classes[j];
                cnt++;
            }
            grid[i][(int)(((double)(avg)/cnt/maxY)*(Y-1))]='#';
        }
        StringBuilder sb=new StringBuilder();
        for( int j=Y-1; j>=-1; j-- ) {
            for( int i=-1; i<X; i++ ) {
                if( j==-1 && i==-1 ) {
                    sb.append('+');
                }else if( j==-1 ) {
                    sb.append('-');
                }else if( i==-1 ) {
                    sb.append('|');
                } else {
                    sb.append(grid[i][j]);
                }
            }
            sb.append( "\r\n" );
        }
        return sb.toString();

    }


}
