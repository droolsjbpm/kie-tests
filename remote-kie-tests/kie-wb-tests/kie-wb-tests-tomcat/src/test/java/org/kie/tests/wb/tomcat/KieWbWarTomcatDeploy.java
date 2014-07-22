package org.kie.tests.wb.tomcat;

import static org.kie.remote.tests.base.DeployUtil.*;
import static org.kie.tests.wb.base.util.TestConstants.PROJECT_VERSION;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieWbWarTomcatDeploy {

    protected static final Logger logger = LoggerFactory.getLogger(KieWbWarTomcatDeploy.class);

    static WebArchive createTestWar() {
        return createTestWar(true);
    }
    
    static WebArchive createTestWar(boolean replace) {
        // Import kie-wb war
        WebArchive war = getWebArchive("org.kie", "kie-wb-distribution-wars", "tomcat7", PROJECT_VERSION);

        war.addAsWebInfResource("war/logging.properties", "classes/logging.properties");

        if( replace ) { 
            String [][] jarsToReplace = {
                    { "org.kie.remote", "kie-remote-services" }, 
                    { "org.kie.remote", "kie-remote-jaxb" },
                    { "org.kie.remote", "kie-remote-common" }
            };
            replaceJars(war, PROJECT_VERSION, jarsToReplace);
        }
        // temporary
        String servletJar = "javax.servlet-api-3.0.1.jar";
        logger.info( "Deleting {}", servletJar);
        war.delete("WEB-INF/lib/" + servletJar);
        
        String [][] jarsToAdd = { 
                { "org.jboss.resteasy", "resteasy-jaxb-provider" }
        };
        addNewJars(war, jarsToAdd);
        
        // Add data service resource for tests
        war.addPackage("org/kie/tests/wb/base/services/data");
       
        return war;
    }

    protected void printTestName() { 
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        logger.info( "] Starting " + ste.getMethodName());
    }
    
}
