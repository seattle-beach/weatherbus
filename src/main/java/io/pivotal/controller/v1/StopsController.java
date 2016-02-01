package io.pivotal.controller.v1;

import io.pivotal.model.Coordinate;
import io.pivotal.model.DepartureWithTemperature;
import io.pivotal.service.BusService;
import io.pivotal.service.Departure;
import io.pivotal.service.WeatherService;
import io.pivotal.service.response.ForecastResponse;
import io.pivotal.service.response.TemperatureResponse;
import io.pivotal.view.WeatherBusPresenter;
import io.pivotal.view.v1.StopsObjectPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.UnknownServiceException;
import java.util.*;

@Controller
@RequestMapping("/api/v1/stops")
public class StopsController {

    @Autowired
    private BusService busService;
    @Autowired
    private WeatherService weatherService;

    @RequestMapping("/{stopId}")
    public
    @ResponseBody
    String getWeatherBus(@PathVariable String stopId) throws UnknownServiceException {
        List<Departure> departures = busService.getDeparturesForStop(stopId);
        Coordinate coordinate = busService.getCoordinatesForStop(stopId);

        ForecastResponse forecastResponse = weatherService.getForecast(coordinate);
        TemperatureResponse temperatureResponse = weatherService.getTemperature(coordinate);

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

        return new StopsObjectPresenter(
                new WeatherBusPresenter(coordinate.getLatitude(),
                        coordinate.getLongitude(),
                        stopId,
                        dwt)
        ).toJson();
    }
}
