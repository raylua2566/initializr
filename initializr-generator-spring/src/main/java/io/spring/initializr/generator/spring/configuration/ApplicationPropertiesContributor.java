/*
 * Copyright 2012-2019 the original author or authors.
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

package io.spring.initializr.generator.spring.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;
import io.spring.initializr.generator.spring.util.LambdaSafe;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A {@link SingleResourceProjectContributor} that contributes a
 * {@code application.properties} file to a project.
 *
 * @author Stephane Nicoll
 */
public class ApplicationPropertiesContributor extends SingleResourceProjectContributor
		implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public ApplicationPropertiesContributor() {
		this("classpath:configuration/application.properties");
	}

	public ApplicationPropertiesContributor(String resourcePattern) {
		super("src/main/resources/application.properties", resourcePattern);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void contributeFile(Path resourceFile) throws IOException {
		if (this.applicationContext == null) {
			return;
		}

		Properties properties = new Properties();
		try (InputStream inStream = Files.newInputStream(resourceFile)) {
			properties.load(inStream);
		}

		List<ApplicationConfigurationFileCustomizer> customizers = this.applicationContext
				.getBeanProvider(ApplicationConfigurationFileCustomizer.class).orderedStream()
				.collect(Collectors.toList());

		LambdaSafe
				.callbacks(ApplicationConfigurationFileCustomizer.class, customizers, properties)
				.invoke((customizer) -> customizer.customize(properties));

		properties.store(Files.newOutputStream(resourceFile), null);
	}

}
