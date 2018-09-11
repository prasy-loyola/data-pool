package com.ps;

import com.ps.db.DbUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

//        DbUtils.createPoolDataTable();
        ResourceConfig config= new ResourceConfig();
        config.packages("com.ps");
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(config));

        Server server = new Server(Config.PORT);
        ServletContextHandler contextHandler = new ServletContextHandler(server, "/*");
        contextHandler.addServlet(servletHolder,"/*");

        try{
            server.start();
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            server.destroy();
        }


    }
}
