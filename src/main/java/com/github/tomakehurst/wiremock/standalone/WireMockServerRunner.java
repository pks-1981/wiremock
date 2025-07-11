/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.standalone;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.lang.System.out;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Version;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.Set;

public class WireMockServerRunner {

  private static final String BANNER =
      "\n"
          + "\u001B[34m██     ██ ██ ██████  ███████ \u001B[33m███    ███  ██████   ██████ ██   ██ \n"
          + "\u001B[34m██     ██ ██ ██   ██ ██      \u001B[33m████  ████ ██    ██ ██      ██  ██  \n"
          + "\u001B[34m██  █  ██ ██ ██████  █████   \u001B[33m██ ████ ██ ██    ██ ██      █████   \n"
          + "\u001B[34m██ ███ ██ ██ ██   ██ ██      \u001B[33m██  ██  ██ ██    ██ ██      ██  ██  \n"
          + "\u001B[34m ███ ███  ██ ██   ██ ███████ \u001B[33m██      ██  ██████   ██████ ██   ██ \n"
          + "\n\u001B[0m"
          + "----------------------------------------------------------------\n"
          + "|               Cloud: https://wiremock.io/cloud               |\n"
          + "|                                                              |\n"
          + "|               Slack: https://slack.wiremock.org              |\n"
          + "----------------------------------------------------------------";

  private WireMockServer wireMockServer;

  public void run(String... args) {
    CommandLineOptions options = new CommandLineOptions(args);
    if (options.help()) {
      out.println(options.helpText());
      return;
    }
    if (options.version()) {
      out.println(Version.getCurrentVersion());
      return;
    }

    FileSource fileSource = options.filesRoot();
    fileSource.createIfNecessary();
    FileSource filesFileSource = fileSource.child(FILES_ROOT);
    filesFileSource.createIfNecessary();
    FileSource mappingsFileSource = fileSource.child(MAPPINGS_ROOT);
    mappingsFileSource.createIfNecessary();

    wireMockServer = new WireMockServer(options);

    if (options.recordMappingsEnabled()) {
      wireMockServer.enableRecordMappings(mappingsFileSource, filesFileSource);
    }

    if (options.specifiesProxyUrl()) {
      addProxyMapping(options.proxyUrl());
    }

    try {
      wireMockServer.start();
      boolean https = options.httpsSettings().enabled();

      if (!options.getHttpDisabled()) {
        options.setActualHttpPort(wireMockServer.port());
      }

      if (https) {
        options.setActualHttpsPort(wireMockServer.httpsPort());
      }

      if (!options.bannerDisabled()) {
        out.println(BANNER);
        out.println();
      } else {
        out.println();
        out.println("The WireMock server is started .....");
      }
      out.println(options);

      final Set<String> loadedExtensionNames = wireMockServer.getLoadedExtensionNames();
      if (!loadedExtensionNames.isEmpty()) {
        out.println("extensions:                   " + String.join(",", loadedExtensionNames));
      }

    } catch (FatalStartupException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  private void addProxyMapping(final String baseUrl) {
    wireMockServer.loadMappingsUsing(
        stubMappings -> {
          RequestPattern requestPattern = newRequestPattern(ANY, anyUrl()).build();
          ResponseDefinition responseDef = responseDefinition().proxiedFrom(baseUrl).build();

          StubMapping proxyBasedMapping = new StubMapping(requestPattern, responseDef);
          proxyBasedMapping.setPriority(
              10); // Make it low priority so that existing stubs will take precedence
          stubMappings.addMapping(proxyBasedMapping);
        });
  }

  public void stop() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  public boolean isRunning() {
    if (wireMockServer == null) {
      return false;
    } else {
      return wireMockServer.isRunning();
    }
  }

  public int port() {
    return wireMockServer.port();
  }
}
