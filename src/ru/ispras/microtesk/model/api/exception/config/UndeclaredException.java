package ru.ispras.microtesk.model.api.exception.config;


import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class UndeclaredException extends ConfigurationException
{
    private static final long serialVersionUID = -4197185882652716717L;

    public UndeclaredException(String message)
    {
        super(message);
    }
}
