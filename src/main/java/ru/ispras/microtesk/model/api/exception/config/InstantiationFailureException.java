package ru.ispras.microtesk.model.api.exception.config;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class InstantiationFailureException extends ConfigurationException
{
    /**
     * 
     */
    private static final long serialVersionUID = -5952137767948105546L;

    public InstantiationFailureException(String message)
    {
        super(message);
    }

    public InstantiationFailureException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
