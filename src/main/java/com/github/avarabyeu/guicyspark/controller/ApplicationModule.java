package com.github.avarabyeu.guicyspark.controller;

import com.github.avarabyeu.guicyspark.service.ValidationDao;
import com.github.avarabyeu.guicyspark.service.ValidationService;
import com.github.avarabyeu.guicyspark.service.ValidationServiceImpl;
import com.github.avarabyeu.guicyspark.service.ValidationWorker;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import spark.servlet.SparkApplication;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.io.Resources.asByteSource;
import static com.google.common.io.Resources.getResource;

/**
 * Guice configuration module
 *
 * @author Andrei Varabyeu
 */
class ApplicationModule extends AbstractModule {

    public static final String INIT_DDL_SCRIPT = "init_ddl.sql";
    public static final String APPLICATION_PROPERTIES = "guicyspark.properties";

    @Override
    protected void configure() {

        /* SparkApplication is basically controller */
        binder().bind(SparkApplication.class).to(ValidationController.class);

        /* Service layer with 'business' logic */
        binder().bind(ValidationService.class).to(ValidationServiceImpl.class);

        /* Asynchronous validation worker (periodically runs with delay between executions) */
        binder().bind(ValidationWorker.class).asEagerSingleton();

        /* Just an utility stuff for simple lifecycle support */
        binder().bind(LifecycleManager.class).in(Scopes.SINGLETON);

        /* Application properties */
        Names.bindProperties(binder(), loadApplicationProperties());

        /* MyBatis configuration */
        binder().install(new MyBatisModule() {
            @Override
            protected void initialize() {
                install(JdbcHelper.HSQLDB_Embedded);

                bindDataSourceProviderType(PooledDataSourceProvider.class);
                bindTransactionFactoryType(JdbcTransactionFactory.class);
                addMapperClass(ValidationDao.class);
            }


        });

    }

    /* ServiceManager is in charge of scheduled services execution */
    @Provides
    public ServiceManager provideServiceManager(ValidationWorker validationWorker) {
        return new ServiceManager(Arrays.asList(validationWorker));
    }


    /* loads application properties */
    private Properties loadApplicationProperties() {
        try (InputStream is = asByteSource(getResource(APPLICATION_PROPERTIES)).openBufferedStream()) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load application properties");
        }

    }

    /**
     * Contains startup and shutdown application logic
     */
    public static class LifecycleManager {

        @Inject
        private ServiceManager serviceManager;

        @Inject
        private SqlSessionFactory sqlSessionFactory;

        public void onStart() {

            /* creates DB schema and fills DB with demo data */
            loadInitScript();

            /* Starts scheduled services */
            serviceManager.startAsync();

            /* Stops services in case of application shutdown
             * Actually, this is not needed, because shutdown event is fired
             * by servlet listener
             */
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    onShutdown();
                }
            });
        }


        public void onShutdown() {
            try {
                serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                //do nothing
            }
        }

        private void loadInitScript() {
            try (SqlSession sqlSession = sqlSessionFactory.openSession(true);
                 Connection connection = sqlSession.getConnection()) {
                new ScriptRunner(connection).runScript(Resources.asCharSource(Resources.getResource(INIT_DDL_SCRIPT), Charsets.UTF_8).openBufferedStream());
            } catch (IOException | SQLException e) {
                throw new IllegalStateException("Unable to execute init script!", e);
            }
        }
    }


}
