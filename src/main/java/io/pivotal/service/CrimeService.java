package io.pivotal.service;

import io.pivotal.service.response.CrimeInfo;
import io.pivotal.service.response.CrimeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrimeService {
    private IFeignCrimeService crimeService;

    @Autowired
    public CrimeService(IFeignCrimeService crimeService) {
        this.crimeService = crimeService;
    }

    public CrimeInfo getCrimeInfo(double latitude, double longitude) {
        CrimeResponse crimeInfo = crimeService.getCrimeInfo(latitude,longitude);
        return crimeInfo.getData();
    }
}
