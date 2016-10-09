package de.hszemi.sensorid_test;

import java.util.List;
import java.util.Map;

/**
 * This class provides methods for computing features
 */
public class Features {

    /**
     * Compute the mean of data[:][index]
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @return the mean of the values at index index of all maps in data
     */
    public static double mean(List<Map<String, Double>> data, String index){

        double m = 0;

        for (Map<String, Double> x: data) {
            m += x.get(index);
        }

        return m / data.size();
    }

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @param mean the mean, calculated by mean()
     * @return the standard deviation of the values at index index of all maps in data
     */
    public static double stddev(List<Map<String, Double>> data, String index, double mean){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.pow(x.get(index)-mean, 2);
        }

        val /= Math.max(data.size() - 1, 1);

        return Math.sqrt(val);
    }

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @param mean the mean, calculated by mean()
     * @return the average deviation of the values at index index of all maps in data
     */
    public static double avgdev(List<Map<String, Double>> data, String index, double mean){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.abs(x.get(index)-mean);
        }

        return val / data.size();
    }

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @param mean the mean, calculated by mean()
     * @param stddev the stddev, calculated by stddev()
     * @return the skewness of the values at index index of all maps in data
     */
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

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @param mean the mean, calculated by mean()
     * @param stddev the stddev, calculated by stddev()
     * @return The kurtosis of the values at index index of all maps in data
     */
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

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @return The root mean square amplitude of the values at index index of all maps in data
     */
    public static double rmsamplitude(List<Map<String, Double>> data, String index){

        double val = 0;

        for (Map<String, Double> x: data) {
            val += Math.pow(x.get(index), 2);
        }

        val /= data.size();

        return Math.sqrt(val);
    }

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @return The minimum value of all values at index index of all maps in data
     */
    public static double lowest(List<Map<String, Double>> data, String index){

        if(data.size() < 1){
            return 0;
        }

        double val = data.get(0).get(index);

        for (Map<String, Double> x: data) {
            val = Math.min(val, x.get(index));
        }

        return val;
    }

    /**
     *
     * @param data the data as a list of maps
     * @param index the index to be accessed in each map
     * @return The maximum value of all values at index index of all maps in data
     */
    public static double highest(List<Map<String, Double>> data, String index){

        if(data.size() < 1){
            return 0;
        }

        double val = data.get(0).get(index);

        for (Map<String, Double> x: data) {
            val = Math.max(val, x.get(index));
        }

        return val;
    }

}
