package edu.casetools.icase.custom;

/**
 * Created by Unai Alegre-Ibarra <u.alegre@mdx.ac.uk> on 26/01/2018.
 */

public class CustomReasoningRules {


    private static final String newWeatherOKAY           = "TEMP_OKAY and PRECIP_DRY iff WEATHER_OKAY";
    private static final String newWeatherRAINING        = "TEMP_OKAY and PRECIP_RAINING iff WEATHER_RAINING";
    private static final String newWeatherCOLD           = "TEMP_COLD and PRECIP_DRY iff WEATHER_COLD";
    private static final String newWeatherCOLDANDRAINING = "TEMP_COLD and PRECIP_RAINING iff WEATHER_RAININGANDCOLD";
    private static final String newWeatherHOT            = "TEMP_HOT and PRECIP_DRY iff WEATHER_HOT";
    private static final String newTEMPCOLD              = "TEMP_COLD[06:00:00-21:00:00] and PRECIP_RAIN[#06:06:00-11:00:00] iff WEATHER_MISERABLE";

    public String getNewWeatherOKAY(){
        return newWeatherOKAY;
    }

    public String getNewWeatherRAINING(){
        return newWeatherRAINING;
    }

    public String getNewWeatherCOLDANDRAINING(){
        return newWeatherCOLDANDRAINING;
    }

    public String getNewWeatherCOLD(){
        return newWeatherCOLD;
    }

    public String getNewWeatherHOT(){
        return newWeatherHOT;
    }

    public String getNewTEMPCOLD(){
        return newTEMPCOLD;
    }

}
