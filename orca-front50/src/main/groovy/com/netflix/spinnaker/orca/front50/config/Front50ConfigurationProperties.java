/*
 * Copyright 2023 Salesforce, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.front50.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("front50")
public class Front50ConfigurationProperties {
  boolean enabled;

  String baseUrl;

  /**
   * Controls the front50 endpoint that DependentPipelineExecutionListener calls to retrieve
   * pipelines.
   *
   * <p>When true: GET /pipelines/triggeredBy/{pipelineId}/{status} When false: GET /pipelines
   */
  boolean useTriggeredByEndpoint = true;

  OkHttpConfigurationProperties okhttp = new OkHttpConfigurationProperties();

  @Data
  public static class OkHttpConfigurationProperties {
    int readTimeoutMs = 10000;

    int writeTimeoutMs = 10000;

    int connectTimeoutMs = 10000;
  }
}
