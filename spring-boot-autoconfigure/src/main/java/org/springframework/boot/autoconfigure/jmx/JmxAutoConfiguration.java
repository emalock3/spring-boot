/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jmx;

import javax.management.MBeanServer;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.MBeanExportConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.jmx.support.MBeanServerFactoryBean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} to enable/disable Spring's
 * {@link EnableMBeanExport} mechanism based on configuration properties.
 * <p>
 * To disable auto export of annotation beans set <code>spring.jmx.enabled: false</code>.
 * 
 * @author Christian Dupuis
 */
@Configuration
@ConditionalOnClass({ MBeanExporter.class })
@ConditionalOnExpression("${spring.jmx.enabled:true}")
public class JmxAutoConfiguration {

	@Autowired
	private Environment environment;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private ObjectNamingStrategy namingStrategy;

	@Bean
	@ConditionalOnMissingBean(value = MBeanExporter.class, search = SearchStrategy.CURRENT)
	public AnnotationMBeanExporter mbeanExporter() {
		// Re-use the @EnableMBeanExport configuration
		MBeanExportConfiguration config = new MBeanExportConfiguration();
		config.setEnvironment(this.environment);
		config.setBeanFactory(this.beanFactory);
		config.setImportMetadata(new StandardAnnotationMetadata(Empty.class));
		// But add a custom naming strategy
		AnnotationMBeanExporter exporter = config.mbeanExporter();
		exporter.setNamingStrategy(this.namingStrategy);
		return exporter;
	}

	@Bean
	@ConditionalOnMissingBean(ObjectNamingStrategy.class)
	public ParentAwareNamingStrategy objectNamingStrategy() {
		return new ParentAwareNamingStrategy(new AnnotationJmxAttributeSource());
	}

	@Bean
	@ConditionalOnMissingBean(MBeanServer.class)
	public MBeanServerFactoryBean mbeanServer() {
		MBeanServerFactoryBean factory = new MBeanServerFactoryBean();
		factory.setLocateExistingServerIfPossible(true);
		return factory;
	}

	@EnableMBeanExport(defaultDomain = "${spring.jmx.default_domain:}", server = "${spring.jmx.server:mbeanServer}")
	private static class Empty {

	}

}
