package it.water.service.discovery;

import com.intuit.karate.junit5.Karate;
import it.water.core.api.service.Service;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WaterTestExtension.class)
public class ServiceDiscoveryInternalRestApiTest implements Service {

    @Karate.Test
    Karate internalRestInterfaceTest() {
        return Karate.run("classpath:karate/ServiceRegistration-internal.feature")
                .systemProperty("webServerPort", TestRuntimeInitializer.getInstance().getRestServerPort())
                .systemProperty("host", "localhost")
                .systemProperty("protocol", "http");
    }
}
