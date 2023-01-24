package ru.yandex.practicum.filmorate.helpers.connector;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.helpers.sourceOfProperties.SupplierOfPropertiesValue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Data
@Component
public class ConnectToDB {

    private Connection connection;
    @Autowired
    private final SupplierOfPropertiesValue propertiesValue;

    public ConnectToDB(SupplierOfPropertiesValue propertiesValue) {
        this.propertiesValue = propertiesValue;
        try {
            String url = propertiesValue.getConfigValue("spring.datasource.url");
            String password = propertiesValue.getConfigValue("spring.datasource.password");
            String user = propertiesValue.getConfigValue("spring.datasource.username");
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Statement getStatement() throws SQLException {
        return connection.createStatement();
    }
}
