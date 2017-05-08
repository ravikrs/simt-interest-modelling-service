package de.rwth.i9.cimt.ke.configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("de.rwth.i9.cimt.**")
@EnableTransactionManagement
@EnableCaching
public class JPAConfig {

	@Bean
	public DataSource dataSource(Environment env) {

		DriverManagerDataSource ds = new DriverManagerDataSource(env.getProperty("spring.datasource.url"),
				env.getProperty("spring.datasource.username"), env.getProperty("spring.datasource.password"));
		return ds;
	}

	@Bean("entityManagerFactory")
	public EntityManagerFactory localContainerEntityManagerFactoryBean(Environment env) {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.MYSQL);
		vendorAdapter.setGenerateDdl(false);
		vendorAdapter.setShowSql(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

		factory.setJpaVendorAdapter(vendorAdapter);

		factory.setPackagesToScan("de.rwth.i9.cimt.**");

		factory.setDataSource(dataSource(env));

		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean("entityManager")
	public EntityManager entityManager(Environment env) {
		return localContainerEntityManagerFactoryBean(env).createEntityManager();
	}

	@Bean("transactionManager")
	public PlatformTransactionManager transactionManager(Environment env) {
		JpaTransactionManager manager = new JpaTransactionManager();
		manager.setEntityManagerFactory(localContainerEntityManagerFactoryBean(env));
		return manager;
	}

	@Bean
	public HibernateExceptionTranslator hibernateExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}
}