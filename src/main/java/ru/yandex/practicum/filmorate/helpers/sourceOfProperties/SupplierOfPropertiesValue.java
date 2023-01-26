package ru.yandex.practicum.filmorate.helpers.sourceOfProperties;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Data
@RequiredArgsConstructor
@Component
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class SupplierOfPropertiesValue {
    @Autowired
    private final Environment env;

    public String getConfigValue(String configKey) {
        return env.getProperty(configKey);
    }
}
