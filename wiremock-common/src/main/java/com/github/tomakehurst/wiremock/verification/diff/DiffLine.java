/*
 * Copyright (C) 2017-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.verification.diff;

import static com.github.tomakehurst.wiremock.common.Strings.isEmpty;

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MatchResult.DiffDescription;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import java.util.List;

class DiffLine<V> {

  protected final String requestAttribute;
  protected final NamedValueMatcher<V> pattern;
  protected final V value;
  protected final String printedPatternValue;

  public DiffLine(
      String requestAttribute, NamedValueMatcher<V> pattern, V value, String printedPatternValue) {
    this.requestAttribute = requestAttribute;
    this.pattern = pattern;
    this.value = value;
    this.printedPatternValue = printedPatternValue;
  }

  public String getRequestAttribute() {
    return requestAttribute;
  }

  public Object getActual() {
    return value;
  }

  public String getPrintedPatternValue() {
    return printedPatternValue;
  }

  public boolean isForNonMatch() {
    return !isExactMatch();
  }

  protected boolean isExactMatch() {
    return pattern.match(value).isExactMatch();
  }

  public String getMessage() {
    String message;
    if (value == null || isEmpty(value.toString())) {
      message = requestAttribute + " is not present";
    } else {
      message = isExactMatch() ? null : requestAttribute + " does not match";
    }

    if (isUrlRegexPattern() && !anyQuestionsMarksAreEscaped(pattern.getExpected())) {
      message += ". When using a regex, \"?\" should be \"\\?\"";
    }

    if (pattern instanceof UrlPattern
        && pattern != UrlPattern.ANY
        && !pattern.getExpected().startsWith("/")) {
      message += ". URLs must start with a /";
    }

    return message;
  }

  private static boolean anyQuestionsMarksAreEscaped(String s) {
    int index = s.indexOf('?');
    if (index == -1) {
      return true;
    }

    if (index < 2) {
      return false;
    }

    String sub = s.substring(index - 1, index);
    return sub.equals("\\");
  }

  private boolean isUrlRegexPattern() {
    return pattern instanceof UrlPattern && ((UrlPattern) pattern).isRegex();
  }

  public List<DiffDescription> getDiffDescriptions() {
    List<DiffDescription> diffDescriptions = getMatchResult().getDiffDescriptions();
    if (diffDescriptions.isEmpty()) {
      return List.of(
          new DiffDescription(
              this.getPrintedPatternValue(), this.getActual().toString(), this.getMessage()));
    }
    return diffDescriptions;
  }

  public MatchResult getMatchResult() {
    return pattern.match(value);
  }
}
