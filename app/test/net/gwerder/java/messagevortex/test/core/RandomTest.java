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
        int[] sample = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,35444,68981,123933,206197,321767,464121,620762,776586,898148,969054,97294,97806,98449,97135,97141,96721,96744,96069,95137,94632,94162,93309,92561,92184,90421,89539,88659,87217,86543,85163,83437,82153,81452,79386,78732,76943,75738,74209,72718,71037,69492,67733,66052,64815,62835,61677,60052,58455,56777,55017,53229,52041,50487,48766,47100,45563,43825,42335,41225,39779,38215,36989,35415,34129,32529,30932,29848,28965,27790,26544,25148,24203,23035,22175,21230,20364,19165,17955,17152,16436,15465,14744,13877,13318,12606,11872,11191,10566,10036,9502,8912,8437,7921,7333,6793,6611,6189,5672,5605,5095,4747,4520,4093,3847,3633,3368,3166,2939,2720,2466,2278,2211,1991,1855,1782,1597,1502,1319,1225,1157,1157,993,895,841,772,716,626,609,558,492,471,434,345,340,320,317,258,230,210,221,176,156,140,142,121,114,83,103,90,80,62,63,68,52,45,48,25,30,22,28,36,12,16,22,18,8,18,10,7,7,9,8,11,4,3,6,3,4,3,3,3,3,1,1,3,2,2,3,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0};
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
            int t=(int)(Math.max( 3, sample[i] * 0.1 ) );
            // assertTrue("failed at pos " + i + " (result:" + classes[i] + "; expect:"+sample[i]+")", classes[i] >= (sample[i] - t) && classes[i] <= (sample[i] + t) );
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
