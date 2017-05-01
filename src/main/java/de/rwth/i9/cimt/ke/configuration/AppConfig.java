package de.rwth.i9.cimt.ke.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ComponentScan({ "de.rwth.i9.cimt.**" })
@PropertySource("classpath:opennlp.properties")
@Lazy(true)
@EnableSwagger2
public class AppConfig {
	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
	private @Value("${cimt.home}") String cimtHome;
	private @Value("${cimt.wikipedia.sql.host}") String wikipediaSqlHost;
	private @Value("${cimt.wikipedia.sql.database}") String wikipediaSqlDatabase;
	private @Value("${cimt.wikipedia.sql.user}") String wikipediaSqlUser;
	private @Value("${cimt.wikipedia.sql.password}") String wikipediaSqlPassword;
	private @Value("${cimt.wikipedia.language}") String wikipediaSqlLanguage;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public Docket swaggerSettings() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(Predicates.not(PathSelectors.regex("/error.*"))).build().apiInfo(apiInfo()).pathMapping("/");
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Semantic Keyterm Extraction Toolkit - SKET API")
				.description("Rest API can be used to extract keyphrases/keywords")
				.contact("Ravi" + " https://learntech.rwth-aachen.de/" + " ravi.singh@rwth-aachen.de")
				.license("Apache License Version 2.0")
				.licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE").version("1.0").build();
	}

}
