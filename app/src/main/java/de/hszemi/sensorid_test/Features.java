package de.hszemi.sensorid_test;

import java.util.List;
import java.util.Map;

/**
 * Created by zemanek on 03.10.16.
 */
public class Features {

    public static double mean(List<Map<String, Double>> data, String index){

        double m = 0;

        for (Map<String, Double> x: data) {
            m += x.get(index);
        }

        return m / data.size();
    }

    public static double stddev(List<Map<String, Double>> data, String index, double mean){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.pow(x.get(index)-mean, 2);
        }

        val /= Math.max(data.size() - 1, 1);

        return Math.sqrt(val);
    }


    public static double avgdev(List<Map<String, Double>> data, String index, double mean){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.abs(x.get(index)-mean);
        }

        return val / data.size();
    }

    public static double skewness(List<Map<String, Double>> data, String index, double mean, double stddev){
        if(stddev == 0.0){
            return 0;
        }

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.pow(((x.get(index) - mean)/stddev), 3);
        }

        return val / data.size();
    }


    public static double kurtosis(List<Map<String, Double>> data, String index, double mean, double stddev){
        if(stddev == 0.0){
            return 0;
        }

        double val = 0;

        for (Map<String, Double> x: data) {
            val += (Math.pow(((x.get(index) - mean)/stddev), 4) - 3);
        }

        return val / data.size();
    }

    public static double rmsamplitude(List<Map<String, Double>> data, String index){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.pow(x.get(index), 2);
        }

        val /= data.size();

        return Math.sqrt(val);
    }

    public static double lowest(List<Map<String, Double>> data, String index){

        double val = data.get(0).get(index);

        for (Map<String, Double> x: data) {
            val = Math.min(val, x.get(index));
        }

        return val;
    }

    public static double highest(List<Map<String, Double>> data, String index){

        double val = data.get(0).get(index);

        for (Map<String, Double> x: data) {
            val = Math.max(val, x.get(index));
        }

        return val;
    }

}
