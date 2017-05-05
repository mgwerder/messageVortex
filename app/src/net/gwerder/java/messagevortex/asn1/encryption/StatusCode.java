package net.gwerder.java.messagevortex.asn1.encryption;

import org.bouncycastle.asn1.ASN1Enumerated;

/**
 * This enum maps the ASN1 error coders as defined in StatusCode.
 */
public enum StatusCode {
    // system messages (no failure)
    OK                          (2000),
    TRANSFER_QUOTA_STATUS       (2101),
    MESSAGE_QUOTA_STATUS        (2102),
    PUZZLE_REQUIRED             (2201),

    // protocol usage failures
    TRANSFER_QUOTA_EXCEEDED     (3001),
    MESSAGE_QUOTA_EXCEEDED      (3002),
    IDENTITY_UNKNOWN            (3101),
    MESSAGE_CHUNK_MISSING       (3201),
    MESSAGE_LIFE_EXPIRED        (3202),
    PUZZLE_UNKNOWN              (3301),

    // capability errors
    MAC_ALGORITHM_UNKNOWN       (3801),
    SYMMETRIC_ALGORITHM_UNKNOWN (3802),
    ASYMMETRIC_ALGORITHM_UNKNOWN(3803),
    PRNG_ALGORITHM_UNKNOWN      (3804),
    MISSING_PARAMETERS          (3820),
    BAD_PARAMETERS              (3821),
    HOST_ERROR                  (5001);

    private final int id;
    private final ASN1Enumerated asn;

    private StatusCode(int i) {
        id=i;
        asn=new ASN1Enumerated(id);
    }

    /***
     * Gets the ASN1 constant for this status code
     * @return the requested constant for ASN1 encoding
     */
    public int getId() {
        return id;
    }

    /***
     * returns the corresponding ASN1 enumeration
     * @return the ASN1 enumeration representing this status
     */
    public ASN1Enumerated toASN1() {
        return asn;
    }
}
