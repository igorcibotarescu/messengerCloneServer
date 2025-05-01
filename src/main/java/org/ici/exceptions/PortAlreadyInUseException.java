package org.ici.exceptions;

import java.io.IOException;

public class PortAlreadyInUseException extends IOException {
    public PortAlreadyInUseException(Throwable cause) {
        super(cause);
    }
}
