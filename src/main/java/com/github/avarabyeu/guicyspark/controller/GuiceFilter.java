package com.github.avarabyeu.guicyspark.controller;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Guice;
import com.google.inject.Injector;
import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;

/**
 * Servlet 3.0 Filter Configuration
 * Spark needs only one filter to be configured, Servlet is not needed
 * Also added ServletContextListener to enable simple lifecycle support
 *
 * @author Andrei Varabyeu
 */
@WebFilter(urlPatterns = {"/*"})
@WebListener
public class GuiceFilter extends SparkFilter implements ServletContextListener {

    /* Singleton with lazy-loading */
    private static Supplier<Injector> injector = Suppliers.memoize(() -> Guice.createInjector(new ApplicationModule()));

    /* obtains SparkApplication which is basically 'controller' layer */
    @Override
    protected SparkApplication getApplication(FilterConfig filterConfig) throws ServletException {
        return injector.get().getInstance(SparkApplication.class);
    }

    /* Fires 'startup' lifecycle logic */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        injector.get().getInstance(ApplicationModule.LifecycleManager.class).onStart();
    }

    /* Fires 'shutdown' lifecycle logic */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        injector.get().getInstance(ApplicationModule.LifecycleManager.class).onShutdown();
    }
}
