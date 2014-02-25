/*
 * Copyright 2012-2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.translator.antlrex;

import java.util.*;
import org.antlr.runtime.*;

/**
 * Composite token source for hierarchically organized sub-sources.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TokenSourceStack implements TokenSource
{
    protected class TokenSourceEntry
    {
        /// The latest token of the parent stream.
        public Token token;
        public TokenSource source;

        public TokenSourceEntry(TokenSource source)
        {
	    this.token  = null;
            this.source = source;
        }

        public TokenSourceEntry(Token token, TokenSource source)
        {
            this.token  = token;
            this.source = source;
        }
    }

    protected Stack<TokenSourceEntry> sources = new Stack<TokenSourceEntry>();

    public TokenSourceStack() {}
    
    public void push(final TokenSource source)
    {
        sources.push(new TokenSourceEntry(source));
    }
    
    public void pop()
    {
        sources.pop();
    }
 
    public Token getToken()
    {
        return sources.peek().token;
    }

    public void setToken(final Token token)
    {
        sources.peek().token = token;
    }

    public TokenSource getSource()
    {
        return sources.peek().source;
    }
    
    public boolean isRootSource()
    {
        return sources.size() == 1;
    }
    
    public boolean hasSources()
    {
        return !sources.empty();
    }

    private static boolean isEof(final Token token)
    {
        return token == null || token.getType() == Token.EOF;
    }

    @Override
    public Token nextToken()
    {
        // If there are no sources, returns EOF.
        if(!hasSources())
            { return Token.EOF_TOKEN; }
            
        TokenSource source = null;
        Token token = Token.EOF_TOKEN;

        boolean subsource_created = true;

        while(subsource_created)
        {
            // Request the active source for the next token.
            source = getSource();
            token  = source.nextToken();

            // A new sub-source was created during the nextToken() call
            // (e.g., for macro expansion or file inclusion).
            if(subsource_created = (source != getSource()))
            {
                // Store a token of the parent source in the stack
                // (it will be returned after this sub-source is completed).
                setToken(token);
            }
        }

        // Skip EOFs of sub-sources (sub-sources are invisible for a user).
        while(isEof(token) && !isRootSource())
        {
            // Try the latest token of the parent stream.
            token = getToken();

            // Remove an exhausted sub-source from the stack.
            pop();

            if(isEof(token))
            {
                source = getSource();
                token  = source.nextToken();
            }
        }
        
        // The root source stays in the stack even if EOF is achieved.

        return token;
    }
    
    @Override
    public String getSourceName()
    {
        if(!hasSources())
            { return ""; }

        TokenSource source = getSource();
        
        return source.getSourceName();
    }
}

