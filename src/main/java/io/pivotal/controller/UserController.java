package io.pivotal.controller;

import com.google.gson.Gson;
import io.pivotal.domain.BusStop;
import io.pivotal.domain.BusStopRepository;
import io.pivotal.domain.User;
import io.pivotal.domain.UserRepository;
import io.pivotal.errorHandling.StopNotFoundException;
import io.pivotal.errorHandling.UserAlreadyExistsException;
import io.pivotal.errorHandling.UserNotFoundException;
import io.pivotal.service.BusService;
import io.pivotal.view.JsonListPresenter;
import io.pivotal.view.JsonPresenter;
import io.pivotal.view.StopPresenter;
import io.pivotal.view.UserPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/users")
@Controller
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    BusStopRepository busStopRepository;

    @Autowired
    BusService busService;

    Gson gson = new Gson();

    @RequestMapping("stops")
    public
    @ResponseBody
    String getStops(@RequestParam String username) throws Exception {
        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            throw new UserNotFoundException();
        }

        List<StopPresenter> stopPresenters = new ArrayList<>();
        for (BusStop stop : user.getStops()) {
            stopPresenters.add(new StopPresenter(stop));
        }
        stopPresenters.sort((o1, o2) -> o1.getId().compareTo(o2.getId()));
        List<JsonPresenter> jsonPresenters = new ArrayList<>(stopPresenters);

        return new JsonListPresenter(jsonPresenters).toJson();
    }

    @RequestMapping
    public @ResponseBody String getAllUsers(){
        List<UserPresenter> userPresenters = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            userPresenters.add(new UserPresenter(user));
        }
        List<JsonPresenter> jsonPresenters = new ArrayList<>(userPresenters);

        return new JsonListPresenter(jsonPresenters).toJson();
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    String addUser(@RequestBody String usernameJson) throws Exception {
        Map<String, String> mapOfJson = gson.fromJson(usernameJson, HashMap.class);
        String nameOfParam = "username";

        if (!mapOfJson.containsKey(nameOfParam)) {
            throw new IllegalArgumentException();
        }
        String username = mapOfJson.get(nameOfParam);

        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user != null) {
            throw new UserAlreadyExistsException();
        }
        userRepository.save(new User(username));
        return "";
    }

    @RequestMapping(
            value = "{username}/stops",
            method = RequestMethod.POST)
    public
    @ResponseBody
    String addStop(
            @PathVariable("username") String username,
            @RequestBody String stopIdJson) throws
            UnknownServiceException,
            UserNotFoundException,
            StopNotFoundException {
        Map<String, String> mapOfJson = gson.fromJson(stopIdJson, HashMap.class);
        String nameOfParam = "stopId";

        if (!mapOfJson.containsKey(nameOfParam)) {
            throw new IllegalArgumentException();
        }
        String stopId = mapOfJson.get(nameOfParam);
        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user == null) {
            throw new UserNotFoundException();
        }
        BusStop stop = busStopRepository.findByApiId(stopId);
        if (stop == null) {
            stop = new BusStop(
                    busService.getStopName(stopId),
                    stopId);
            busStopRepository.save(stop);
        }
        user.getStops().add(stop);
        userRepository.save(user);
        return "";
    }
}

