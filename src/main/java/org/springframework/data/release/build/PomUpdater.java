/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.release.build;

import static org.springframework.data.release.model.Projects.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.release.build.Pom.RepositoryElementFactory;
import org.springframework.data.release.model.ArtifactVersion;
import org.springframework.data.release.model.Project;
import org.springframework.data.release.utils.Logger;
import org.springframework.util.Assert;

/**
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@RequiredArgsConstructor
class PomUpdater {

	private final Logger logger;
	private final UpdateInformation information;
	private final @Getter Project project;

	public boolean isBuildProject() {
		return BUILD.equals(project);
	}

	public boolean isBomProject() {
		return BOM.equals(project);
	}

	public void updateArtifactVersion(Pom pom) {

		ArtifactVersion version = information.getProjectVersionToSet(project);
		logger.log(project, "Updated project version to %s.", version);
		pom.setVersion(version);
	}

	public void updateDependencyProperties(Pom pom) {

		project.getDependencies().forEach(dependency -> {

			String dependencyProperty = dependency.getDependencyProperty();

			if (pom.getProperty(dependencyProperty) == null) {
				return;
			}

			ArtifactVersion version = information.getProjectVersionToSet(dependency);

			logger.log(project, "Updating %s dependency version property %s to %s.", dependency.getFullName(),
					dependencyProperty, version);
			pom.setProperty(dependencyProperty, version);
		});
	}

	/**
	 * Updates the version of the parent project in the given {@link Pom}.
	 *
	 * @param pom must not be {@literal null}.
	 */
	public void updateParentVersion(Pom pom) {

		Assert.notNull(pom, "Pom must not be null!");

		ArtifactVersion version = information.getParentVersionToSet();

		logger.log(project, "Updating Spring Data Build Parent version to %s.", version);
		pom.setParentVersion(version);
	}

	/**
	 * Updates the repository section in the given {@link Pom}.
	 *
	 * @param pom must not be {@literal null}.
	 */
	public void updateRepository(Pom pom) {

		Assert.notNull(pom, "Pom must not be null!");

		List<Repository> repositories = information.getRepositories();
		pom.deleteRepositories();

		if (repositories.isEmpty()) {
			logger.log(project, "Removing <repositories> declaration.");
		} else {
			logger.log(project, "Switching to Spring repositories %s.", repositories);
			pom.setRepositories(RepositoryElementFactory.of(repositories));
		}
	}
}
