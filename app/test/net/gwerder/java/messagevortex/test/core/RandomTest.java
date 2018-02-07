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

    @Test
    public void randomTimeTest() {
        int[] sample = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,35468,69246,124068,206639,321990,463734,623584,775748,898432,967241,97972,98003,98023,97556,96920,96472,96497,95877,95639,94869,93757,93227,92957,91514,90571,89095,88993,87285,86521,84640,83854,82440,80707,79842,79117,76746,75796,74205,72886,70479,69193,68001,66121,64422,63326,61498,59949,58528,56588,55348,53641,51831,50026,48384,47347,45487,43850,42123,41099,39354,38266,37152,35381,33728,32507,31569,29771,28655,27662,26253,25579,24010,23104,22018,20844,20004,18939,17994,17405,16340,15814,15105,13795,13147,12637,11856,11456,10581,10198,9603,8907,8398,7971,7561,7115,6595,6180,5816,5349,5004,4636,4554,4229,3804,3445,3338,3211,2840,2674,2573,2365,2191,2042,1849,1758,1579,1461,1375,1192,1151,1053,985,917,854,835,774,694,615,556,517,449,406,363,365,305,298,284,246,206,187,191,162,129,123,118,82,115,91,87,56,69,58,44,47,36,43,41,31,31,26,30,28,21,24,20,12,22,12,5,11,8,5,6,11,4,1,3,1,6,3,2,4,3,2,1,1,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0};
        // init classes
        int[] classes = new int[300];
        for( int i = 0; i< classes.length; i++ ) {
            classes[i]=0;
        }

        // get random numbers
        int mul = 30000;
        for( int i=0; i<classes.length*mul; i++ ) {
            double d = -1;
            while( d == -1 || (int)(d) > classes.length-1 ) {
                d=ExtendedSecureRandom.nextRandomTime( 90, 100, 200 );
            }
            assertTrue( "value below start", d >= 90 );
            classes[(int)(d)]++;
        }

        System.out.println( plot( classes ) );
        // check result (must be all within 5%)
        StringBuilder sb=new StringBuilder();
        sb.append('{');
        for( int i = 0; i< classes.length; i++ ) {
            if ( i != 0 )sb.append( ',' );
            sb.append( classes[i] );
            int t=(int)(Math.max( 60, sample[i] * 0.3 ) );
            assertTrue("failed at pos " + i + " (result:" + classes[i] + "; expect:"+sample[i]+")", classes[i] >= (sample[i] - t) && classes[i] <= (sample[i] + t) );
        }
        System.out.println( sb.toString() +"}");
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
        double interval = classes.length/X;
        for( int i=0; i<X; i++ ) {
            int avg=0;
            int cnt=0;
            int start=(int)(i*interval);
            for(int j = start; j<start + interval; j++ ) {
                avg+=classes[j];
                cnt++;
            }
            maxY=Math.max( maxY, (int)((double)(avg/cnt)) );
        }

        for( int i=0; i<X; i++ ) {
            int avg=0;
            int cnt=0;
            int start=(int)(i*interval);
            for(int j = start; j<start+interval; j++ ) {
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
        return "maxY="+maxY+"\r\n"+sb.toString();

    }


}
