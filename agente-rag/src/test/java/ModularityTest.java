import br.com.simulado.agenterag.AgenteRagApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ModularityTest {
    @Test
    void verifyModuleStructure() {
        assertDoesNotThrow(() -> {
            var modules = ApplicationModules.of(AgenteRagApplication.class);
            modules.forEach(System.out::println);
            modules.verify();
        });
    }
}
