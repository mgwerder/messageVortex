package net.messagevortex.test;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKeyPreCalculator;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.logging.Level;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class GlobalJunitExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            started = true;
            // set log level
            MessageVortexLogger.setGlobalLogLevel(Level.INFO);
            // Your "before all tests" startup logic goes here
            AsymmetricKeyPreCalculator.setCacheFileName("AsymmetricKey.cache");
            // The following line registers a callback hook when the root test context is shut down
            context.getRoot().getStore(GLOBAL).put("any unique name", this);
        }
    }

    @Override
    public void close() {
        // Your "after all tests" logic goes here
        AsymmetricKeyPreCalculator.setCacheFileName(null);
    }
}