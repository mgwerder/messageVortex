package net.gwerder.java.messagevortex.transport;

/**
 * Created by Martin on 23.01.2018.
 */
public enum SecurityRequirement {
    PLAIN,
    UNTRUSTED_STARTTLS,
    STARTTLS,
    UNTRUSTED_SSLTLS,
    SSLTLS;
}
