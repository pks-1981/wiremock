/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.common.Strings.normalisedLevenshteinDistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class EqualToPatternWithCaseInsensitivePrefix extends StringValuePattern {

  private final String prefix;
  private final String testValue;

  public EqualToPatternWithCaseInsensitivePrefix(
      @JsonProperty("prefix") String prefix, @JsonProperty("equalTo") String testValue) {
    super(prefix + testValue);
    Objects.requireNonNull(prefix, "prefix cannot be null");
    this.prefix = prefix;
    this.testValue = testValue;
  }

  @Override
  public MatchResult match(final String value) {
    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return value != null
            && value.substring(0, prefix.length()).equalsIgnoreCase(prefix)
            && Objects.equals(testValue, value.substring(prefix.length()));
      }

      @Override
      public double getDistance() {
        return normalisedLevenshteinDistance(expectedValue, value);
      }
    };
  }
}
