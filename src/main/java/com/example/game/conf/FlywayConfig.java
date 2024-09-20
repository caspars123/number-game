package com.example.game.conf;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

	private final ConfigurableEnvironment configurableEnvironment;

	private static ClassicConfiguration getConfiguration(Flyway flyway) {
		try {
			Field field = Flyway.class.getDeclaredField("configuration");
			field.setAccessible(true);

			return (ClassicConfiguration) field.get(flyway);
		} catch (ReflectiveOperationException | SecurityException e) {
			throw new RuntimeException("Failed to get Flyway configuration", e);
		}
	}

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return flyway -> {

			ClassicConfiguration conf = getConfiguration(flyway);

			List<String> locations = new ArrayList<>();
			Arrays.stream(conf.getLocations())
				.forEach(l -> {
					locations.add(l.getDescriptor() + "/all");

					Arrays.stream(configurableEnvironment.getActiveProfiles())
						.forEach(profile -> locations.add(l.getDescriptor() + "/" + profile));
				});

			conf.setLocationsAsStrings(locations.toArray(String[]::new));
			flyway.migrate();
		};
	}
}
