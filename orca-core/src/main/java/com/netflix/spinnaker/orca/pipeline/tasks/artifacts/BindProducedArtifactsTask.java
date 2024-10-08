/*
 * Copyright 2018 Google, Inc.
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
 *
 */

package com.netflix.spinnaker.orca.pipeline.tasks.artifacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.kork.artifacts.model.Artifact;
import com.netflix.spinnaker.kork.artifacts.model.ExpectedArtifact;
import com.netflix.spinnaker.orca.api.pipeline.Task;
import com.netflix.spinnaker.orca.api.pipeline.TaskResult;
import com.netflix.spinnaker.orca.api.pipeline.models.ExecutionStatus;
import com.netflix.spinnaker.orca.api.pipeline.models.StageExecution;
import com.netflix.spinnaker.orca.config.TaskConfigurationProperties;
import com.netflix.spinnaker.orca.config.TaskConfigurationProperties.BindProducedArtifactsTaskConfig;
import com.netflix.spinnaker.orca.pipeline.util.ArtifactResolver;
import com.netflix.spinnaker.orca.pipeline.util.ArtifactUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BindProducedArtifactsTask implements Task {
  public static final String TASK_NAME = "bindProducedArtifacts";
  private final ArtifactUtils artifactUtils;
  private final ObjectMapper objectMapper;
  private final BindProducedArtifactsTaskConfig configProperties;

  @Autowired
  public BindProducedArtifactsTask(
      ArtifactUtils artifactUtils,
      ObjectMapper objectMapper,
      TaskConfigurationProperties configProperties) {
    this.artifactUtils = artifactUtils;
    this.objectMapper = objectMapper;
    this.configProperties = configProperties.getBindProducedArtifactsTask();
    log.info("output keys to filter: {}", this.configProperties.getExcludeKeysFromOutputs());
  }

  @Nonnull
  @Override
  public TaskResult execute(@Nonnull StageExecution stage) {
    Map<String, Object> context = stage.getContext();
    Map<String, Object> outputs = new HashMap<>();

    List<ExpectedArtifact> expectedArtifacts =
        objectMapper.convertValue(
            context.get("expectedArtifacts"), new TypeReference<List<ExpectedArtifact>>() {});

    if (expectedArtifacts == null || expectedArtifacts.isEmpty()) {
      return TaskResult.SUCCEEDED;
    }

    List<Artifact> artifacts = artifactUtils.getArtifacts(stage);
    ArtifactResolver.ResolveResult resolveResult =
        ArtifactResolver.getInstance(artifacts, /* requireUniqueMatches= */ false)
            .resolveExpectedArtifacts(expectedArtifacts);

    outputs.put("artifacts", resolveResult.getResolvedArtifacts());
    outputs.put("resolvedExpectedArtifacts", resolveResult.getResolvedExpectedArtifacts());

    // exclude certain configured keys from being stored in the stage outputs
    Map<String, Object> filteredOutputs =
        filterContextOutputs(outputs, configProperties.getExcludeKeysFromOutputs());
    log.info("context outputs will only contain: {} keys", filteredOutputs.keySet());

    return TaskResult.builder(ExecutionStatus.SUCCEEDED)
        .context(outputs)
        .outputs(filteredOutputs)
        .build();
  }
}
