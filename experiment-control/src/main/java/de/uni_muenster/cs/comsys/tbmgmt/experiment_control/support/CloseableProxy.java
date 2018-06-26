package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import java.io.Closeable;
import java.io.IOException;

/**
 * {@link CloseableProxy} enables the use of try-with-resource-statements, when you want the possibility to safely
 * skip closing when the resource gets out of context. Advantage of try-with-resource over a try-finally is that the
 * exception of the main block is propagated and exceptions during close are automatically added as suppressed.
 */
public class CloseableProxy<C extends Closeable> implements Closeable {
    private final ReuseConsumer<C> reuseConsumer;
    private final C closable;
    private boolean reusable = false;

    public CloseableProxy(final C closable, final ReuseConsumer<C> reuseConsumer) {
        this.reuseConsumer = reuseConsumer;
        this.closable = closable;
    }

    public C get() {
        return closable;
    }

    public void setReusable() {
        reusable = true;
    }

    @Override
    public void close() throws IOException {
        if (closable != null) {
            boolean reused = false;
            try {
                if (reusable) {
                    reuseConsumer.accept(closable);
                    reused = true;
                }
            } finally {
                if (!reused) {
                    closable.close();
                }
            }
        }
    }

    @FunctionalInterface
    public interface ReuseConsumer<C extends Closeable> {
        void accept(C c) throws IOException;
    }
}
