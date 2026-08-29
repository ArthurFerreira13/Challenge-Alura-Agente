package br.com.agenterag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/status")
public class DatabaseStatusController {

    private final DataSource dataSource;

    public DatabaseStatusController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/db")
    public Map<String, String> getDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return Map.of(
                    "url", metaData.getURL(),
                    "product", metaData.getDatabaseProductName(),
                    "version", metaData.getDatabaseProductVersion(),
                    "user", metaData.getUserName()
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}