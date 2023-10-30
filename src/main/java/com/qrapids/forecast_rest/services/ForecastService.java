package com.qrapids.forecast_rest.services;

import Forecast.Common;
import Forecast.MongoDB_RForecast;
import Forecast.ForecastDTO;
import com.qrapids.forecast_rest.configuration.Connection;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ForecastService {

    @Autowired
    private Connection connection;

    @RequestMapping("api/ForecastTechniques")
    public synchronized Common.ForecastTechnique[] getForecastTechniques(@RequestParam("host") String host, @RequestParam("port") String port, @RequestParam("database") String database, @RequestParam("user") String user, @RequestParam("pwd") String pwd) throws Exception {
        return connection.getConnection(host, port, database, user, pwd).getForecastTechniques();
    }

    @RequestMapping("api/Train")
    public void train(@RequestParam("host") String host, @RequestParam("port") String port, @RequestParam("database") String database, @RequestParam("user") String user, @RequestParam("pwd") String pwd, @RequestParam("index") String index, @RequestParam("elements") List<String> elements, @RequestParam("frequency") String freq, @RequestParam(value = "technique", required = false) String technique) {
        MongoDB_RForecast mongo_rForecast = connection.getConnection(host, port, database, user, pwd);
        if (technique != null) {
            Common.ForecastTechnique forecastTechnique = Common.ForecastTechnique.valueOf(technique);
            trainTechnique(mongo_rForecast, elements, index, freq, forecastTechnique);
        } else {
            for (Common.ForecastTechnique forecastTechnique : Common.ForecastTechnique.values()) {
                trainTechnique(mongo_rForecast, elements, index, freq, forecastTechnique);
            }
        }
    }

    private synchronized void trainTechnique (MongoDB_RForecast mongo_rForecast, List<String> elements, String index, String freq, Common.ForecastTechnique forecastTechnique) {
        try {
            mongo_rForecast.multipleElementTrain(elements.toArray(new String[0]), index, freq, forecastTechnique);
        }
        catch (Exception ignored) { }
    }

    @RequestMapping("/api/Metrics/Forecast")
    public List<ForecastDTO> getForecastMetric(@RequestParam("host") String host, @RequestParam("port") String port, @RequestParam("database") String database, @RequestParam("user") String user, @RequestParam("pwd") String pwd, @RequestParam("index_metrics") String indexMetrics, @RequestParam("metric") List<String> metric, @RequestParam("frequency") String freq, @RequestParam("horizon") String horizon, @RequestParam("technique") String technique) throws Exception {
        return getForecast(host, port, database, user, pwd, indexMetrics, metric, freq, horizon, technique);
    }

    @RequestMapping("/api/QualityFactors/Forecast")
    public List<ForecastDTO> getForecastFactor(@RequestParam("host") String host, @RequestParam("port") String port, @RequestParam("database") String database, @RequestParam("user") String user, @RequestParam("pwd") String pwd, @RequestParam("index_factors") String indexFactors, @RequestParam("factor") List<String> factor, @RequestParam("frequency") String freq, @RequestParam("horizon") String horizon, @RequestParam("technique") String technique) throws  Exception {
        return getForecast(host, port, database, user, pwd, indexFactors, factor, freq, horizon, technique);
    }

    @RequestMapping("/api/StrategicIndicators/Forecast")
    public List<ForecastDTO> getForecastStrategicIndicator(@RequestParam("host") String host, @RequestParam("port") String port, @RequestParam("database") String database, @RequestParam("user") String user, @RequestParam("pwd") String pwd, @RequestParam("index_strategic_indicators") String indexSIs, @RequestParam("strategic_indicator") List<String> si, @RequestParam("frequency") String freq, @RequestParam("horizon") String horizon, @RequestParam("technique") String technique) throws Exception {
        return getForecast(host, port, database, user, pwd, indexSIs, si, freq, horizon, technique);
    }

    private synchronized List<ForecastDTO> getForecast(String host, String port, String database, String user, String pwd, String index, List<String> list, String freq, String horizon, String technique) throws REXPMismatchException, REngineException {
        try {
            Common.ForecastTechnique forecastTechnique = Common.ForecastTechnique.valueOf(technique);
            return connection.getConnection(host, port, database, user, pwd).multipleElementForecast(list.toArray(new String[0]), index, freq, horizon, forecastTechnique);
        }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forecast technique not supported");
        }
    }
}
