package ru.ispras.microtesk.model.api.exception.config;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class UninitializedException extends ConfigurationException
{
    private static final long serialVersionUID = -7342125331426913913L;

    public UninitializedException(String message)
    {
        super(message);
    }

}
