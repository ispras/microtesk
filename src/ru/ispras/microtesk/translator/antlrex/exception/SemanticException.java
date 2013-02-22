package ru.ispras.microtesk.translator.antlrex.exception;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;

public final class SemanticException extends RecognitionException
{
    private static final long serialVersionUID = 209516770104977723L;

    private final ISemanticError error;
    
    public SemanticException(IntStream input, ISemanticError error)
    {
        super(input);
        this.error = error;
    }
    
    public SemanticException(Where location, ISemanticError error)
    {
        super();
        this.error = error;

        this.line                = location.getLine();
        this.charPositionInLine  = location.getPosition();
        this.approximateLineInfo = true;
    }

    @Override
    public String getMessage()
    {
        return error.getMessage();
    }
}
