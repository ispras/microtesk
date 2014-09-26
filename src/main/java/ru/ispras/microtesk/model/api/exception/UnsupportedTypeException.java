package ru.ispras.microtesk.model.api.exception;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class UnsupportedTypeException extends ConfigurationException
{
    private static final long serialVersionUID = 1984643413810914282L;

    public UnsupportedTypeException(String message)
    {
        super(message);
    }
}
