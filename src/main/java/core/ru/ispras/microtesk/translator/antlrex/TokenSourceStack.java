/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.antlrex;

import java.util.Stack;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * Composite token source for hierarchically organized sub-sources.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class TokenSourceStack implements TokenSource {
  public static class TokenSourceEntry {
    public final TokenSource source;

    /** The latest token of the parent stream. */
    public Token lastParentToken = null;

    public TokenSourceEntry(final TokenSource source) {
      InvariantChecks.checkNotNull(source);
      this.source = source;
    }
  }

  private Stack<TokenSourceEntry> sources = new Stack<TokenSourceEntry>();

  public TokenSourceStack() {}

  public void push(final TokenSource source) {
    sources.push(new TokenSourceEntry(source));
  }

  public void pop() {
    sources.pop();
  }

  public Token getLastParentToken() {
    return sources.peek().lastParentToken;
  }

  public void setLastParentToken(final Token token) {
    sources.peek().lastParentToken = token;
  }

  public void setLastParentToken(final int i, final Token token) {
    sources.get(i).lastParentToken = token;
  }

  public TokenSource getSource() {
    return sources.peek().source;
  }

  public boolean isRootSource() {
    return sources.size() == 1;
  }

  public boolean hasSources() {
    return !sources.empty();
  }

  private static boolean isEof(final Token token) {
    return token == null || token.getType() == Token.EOF;
  }

  @Override
  public Token nextToken() {
    // If there are no sources, returns EOF.
    if (!hasSources()) {
      return Token.EOF_TOKEN;
    }

    int size = -1;
    TokenSource source = null;
    Token token = Token.EOF_TOKEN;

    boolean subsourceCreated = true;

    while (subsourceCreated) {
      // Request the active source for the next token.
      size = sources.size();
      source = getSource();
      token = source.nextToken();

      // New sub-sources were created during the recent nextToken() call and pushed into the stack
      // (e.g., for macro expansion or file inclusion). Note that if there are several includes in
      // a row, the corresponding number of sub-sources are created. 
      subsourceCreated = (source != getSource());
      InvariantChecks.checkTrue(!subsourceCreated || size < sources.size());

      if (subsourceCreated) {
        // Store a token of the parent source in the stack (it will be returned as soon as the
        // created sub-sources are completed).
        setLastParentToken(size, token);
      }
    }

    // Skip EOFs of sub-sources (sub-sources are invisible for a user).
    while (isEof(token) && !isRootSource()) {
      // Try the latest token of the parent stream.
      token = getLastParentToken();

      // Remove an exhausted sub-source from the stack.
      pop();

      if (isEof(token)) {
        source = getSource();
        token = source.nextToken();
      }
    }

    // The root source stays in the stack even if EOF is achieved.
    return token;
  }

  @Override
  public String getSourceName() {
    if (!hasSources()) {
      return "";
    }

    final TokenSource source = getSource();
    return source.getSourceName();
  }
}
