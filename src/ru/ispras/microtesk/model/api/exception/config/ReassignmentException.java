package ru.ispras.microtesk.model.api.exception.config;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class ReassignmentException extends ConfigurationException
{
    private static final long serialVersionUID = 1L;

    public ReassignmentException(String message)
    {
        super(message);
    }
}
