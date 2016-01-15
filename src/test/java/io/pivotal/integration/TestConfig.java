package io.pivotal.integration;

import io.pivotal.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import retrofit.http.Path;

@Configuration
@Profile("test")
public class TestConfig {
    @Bean
    public IOneBusAwayService getOneBusAwayService() {
        return new IOneBusAwayService() {
            @Override
            public ArrivalsAndDeparturesForStopResponse getDeparturesForStop(@Path("stop") String stopId) {
                return null;
            }

            @Override
            public StopResponse getCoordinatesForStop(@Path("stop") String stopId) {
                return new StopResponseBuilder().build();
            }
        };
    }

    @Bean
    public IWundergroundService getWundergroundService() {
        return new IWundergroundService() {
            @Override
            public WeatherConditionsResponse getConditionsResponse(@Path("latitude") String latitude, @Path("longitude") String longitude) {
                return new WeatherConditionsResponseBuilder().build();
            }

            @Override
            public WeatherForecastResponse getForecastResponse(@Path("latitude") String latitude, @Path("longitude") String longitude) {
                return null;
            }
        };
    }
}