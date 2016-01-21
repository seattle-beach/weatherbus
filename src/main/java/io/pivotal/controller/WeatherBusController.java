package io.pivotal.controller;

import io.pivotal.model.Coordinate;
import io.pivotal.model.DepartureWithTemperature;
import io.pivotal.service.BusService;
import io.pivotal.service.Departure;
import io.pivotal.service.IWeatherService;
import io.pivotal.service.response.ForecastResponse;
import io.pivotal.service.response.TemperatureResponse;
import io.pivotal.view.WeatherBusPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/wb")
public class WeatherBusController {
    @Autowired
    private BusService busService;

    @Autowired
    private IWeatherService weatherService;

    @RequestMapping("")
    public @ResponseBody String getWeatherBus(@RequestParam String stopId) throws Exception {
        List<Departure> departures = busService.getDeparturesForStop(stopId);
        Coordinate coordinate = busService.getCoordinatesForStop(stopId);

        ForecastResponse forecastResponse = weatherService.getForecast(
                coordinate.getLatitude(),
                coordinate.getLongitude());
        TemperatureResponse temperatureResponse = weatherService.getTemperature(
                coordinate.getLatitude(),
                coordinate.getLongitude());

        SortedMap<Date, Double> forecast = new TreeMap<>();
        forecast.put(new Date(), temperatureResponse.getTemp());
        for (ForecastResponse.TimedTemp tt : forecastResponse.getForecast()) {
            forecast.put(new Date(tt.getTimeInMillisec() * 1000), tt.getTemp());
        }

        List<DepartureWithTemperature> dwt = new ArrayList<>();

        for (Departure departure : departures) {
            long departureTimeMs = departure.getPredictedTime();
            if (departureTimeMs == 0) {
                departureTimeMs = departure.getScheduledTime();
            }

            for (Map.Entry<Date, Double> temp : forecast.entrySet()) {
                if (departureTimeMs < temp.getKey().getTime()) {
                    dwt.add(new DepartureWithTemperature(departure, temp.getValue()));
                    break;
                }
            }
        }

        double lastTemp = forecast.get(forecast.lastKey());
        List<Departure> remainingDepartures = departures.subList(dwt.size(), departures.size());
        for (Departure departure : remainingDepartures) {
            dwt.add(new DepartureWithTemperature(departure, lastTemp));
        }

        return new WeatherBusPresenter(coordinate.getLatitude(), coordinate.getLongitude(), stopId, dwt).toJson();
    }
}
