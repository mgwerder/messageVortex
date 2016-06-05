package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 03.06.2016.
 */
public class SymmetricKeyTest {

    @Test
    public void symmetricKeySizeTest() {
        assertTrue( "getKeySize for AES256 is bad", Algorithm.AES256.getKeySize() == 256 );
    }


}