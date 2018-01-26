package edu.casetools.icase.custom;

/**
 * Created by Unai Alegre-Ibarra <u.alegre@mdx.ac.uk> on 26/01/2018.
 */

public class CustomModellingRules {

    /*
      Check that the battery has less than 25 percent remaining.
   */
    private static final String batteryLOWQuery =
            "REGISTER STREAM batteryContextIsLOW AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_LOW\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + "WHERE { ?s ex:batteryRemaining ?o "
                    + "FILTER ( ?o < 25) }";

    /*
        Check that the battery has 25 percent or more remaining.
     */
    private static final String batteryOkQuery =
            "REGISTER STREAM batteryContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_OKAY\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + "WHERE { ?s ex:batteryRemaining ?o "
                    + "FILTER ( ?o >= 25) }";

    /*
        Check that the precipitation value is greater than zero, and temperature is less than 15c
     */
    private static final String weatherRainingAndColdQuery =
            "REGISTER QUERY weatherContextIsRainingAndCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAININGANDCOLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "FILTER (?tempValue < 15) "
                    + "}";

    /*
        Check that the temperature is 15c or greater, and that precipitation is greater than zero.
     */
    private static final String weatherRainingQuery =
            "REGISTER QUERY weatherContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "FILTER (?tempValue >= 15) "
                    + "}";

    /*
        Check that precipitation is less than 0.1mm, and temperature is less than 15c.
     */
    private static final String weatherColdQuery =
            "REGISTER QUERY weatherContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue < 15) "
                    + "}";

    /*
        Check that precipitation is less than 0.1mm, and temperature is greater than 25c.
     */
    private static final String weatherHotQuery =
            "REGISTER QUERY weatherContextIsHot AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_HOT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue >= 25) "
                    + "}";

    /*
        Check that precipitation is less than 0.1mm, temperature is between 15c-24.9c.
     */
    private static final String weatherOkayQuery =
            "REGISTER QUERY weatherContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_OKAY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue >= 15) "
                    + " FILTER (?tempValue < 25) "
                    + "}";

    private static final String tempOkay =
            "REGISTER QUERY tempContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_OKAY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue >= $$pref_cold) "
                    + "FILTER (?tempValue < $$pref_hot) "
                    + "}";

    private static final String tempCold =
            "REGISTER QUERY tempContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue < $$pref_cold) "
                    + "}";

    private static final String tempHot =
            "REGISTER QUERY tempContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_HOT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue >= $$pref_hot) "
                    + "}";

    private static final String precipRain =
            "REGISTER QUERY precipContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "}";

    private static final String precipDry =
            "REGISTER QUERY precipContextIsDry AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_DRY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue < 0.1) "
                    + "}";

    /*
        Checks if navigation assistance is required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated a 2 or more times (albeit small deviations) within 10 minutes.

        Deviation data is received in terms of integers:
        0 - Navigation is off
        1 - Navigation is on, and a critical deviation has happened.
        2 - Navigation is on, and a small deviation has happened.
        3 - Navigation is on, currently no deviation.

        We therefore firstly count the number of small deviations (subquery). We then see if
        either the number of small deviations is 2 or greater, or if a critical deviation has
        occured.
     */
    private static final String navigationAssistNeededQuery =
            "REGISTER QUERY needNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?smallDevNum) WHERE { ?user ex:hasNavigationStatus 2 . }"
                    + " }"
                    + " FILTER( ?smallDevNum >= $$pref_max_dev || ?o = 1 ) "
                    + " } ";

    /*
        Checks if navigation assistance is NOT required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated less than 2 times (albeit small deviations) within 10 minutes.

        Deviation data is received in terms of integers:
        0 - Navigation is off
        1 - Navigation is on, and a critical deviation has happened.
        2 - Navigation is on, and a small deviation has happened.
        3 - Navigation is on, currently no deviation.

        We therefore firstly count the number of small deviations (subquery 1), and critical
        deviations (subquery 2). We then see if either the number of small deviations is less than 3
        times, if a critical deviation has occured, or if no deviation has occured.
     */
    private static final String navigationAssistNotNeededQuery =
            "REGISTER QUERY dontNeedNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNOTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?smallDevNum) WHERE { ?user ex:hasNavigationStatus 2 . }"
                    + " } . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?largeDevNum) WHERE { ?user ex:hasNavigationStatus 1 . }"
                    + " } . "
                    + " FILTER( ?smallDevNum < $$pref_max_dev && ?largeDevNum < 1) "
                    //+ " FILTER( ?o = 3) "
                    + " } ";

    /*
        Checks to see how fast the user is walking, to tell if they are too standstill for too long.
        Checks that the user is walking less than 20m every 5 minutes.
     */
    private static final String isStandstillForLongQuery =
            "REGISTER QUERY notWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_LONG\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE $$timem STEP $$timem] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER ( ?totalDistance < $$tDistance) "
                    + " }";

    /*
        Checks to see how fast the user is walking, to tell if they are not stanstill for long.
        Checks that the user is walking 20m or more every 5 minutes.
     */
    private static final String isStandstillForShortQuery =
            "REGISTER QUERY isWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_SHORT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE $$timem STEP $$timem] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER( ?totalDistance >= $$tDistance) "
                    + " }";

    public String getColdQuery(String coldValue) {
        String cold = new String(tempCold);
        return cold.replace("$$pref_cold", String.valueOf(coldValue));
    }

    public String getTempHotQuery(String tempHotValue){
        String hot = new String(tempHot);
        return hot.replace("$$pref_hot", String.valueOf(tempHotValue));
    }

    public String getTempOkQuery(String hotTempValue, String coldTempValue){
        String ok = new String(tempOkay);
        ok = ok.replace("$$pref_hot", String.valueOf(hotTempValue));
        return ok.replace("$$pref_cold", String.valueOf(coldTempValue));
    }

    public String getPrecipRain(){
        return precipRain;
    }

    public String getPrecipDry(){
        return  precipDry;
    }



    public String getIsStandstillForLongQuery(String maxTimeValue, String maxDistanceValue){
        String standStillLong = new String(isStandstillForLongQuery);
        standStillLong = standStillLong.replace("$$time", String.valueOf(maxTimeValue));
        return standStillLong.replace("$$tDistance", String.valueOf(maxDistanceValue));
    }

    public String getIsStandstillForShortQuery(String maxTimeValue, String maxDistanceValue){
        String standStillShort = new String(isStandstillForShortQuery);
        standStillShort = standStillShort.replace("$$time", String.valueOf(maxTimeValue));
        return standStillShort.replace("$$tDistance", String.valueOf(maxDistanceValue));
    }

    public String getNavigationAssistNeededQuery(String maxDevValue){
        String navAssNeeded = new String(navigationAssistNeededQuery);
        return navAssNeeded.replace("$$pref_max_dev", maxDevValue);
    }

    public String getNavigationAssistNotNeededQuery(String maxDevValue){
        String navAssNotNeeded = new String(navigationAssistNotNeededQuery);
        return navAssNotNeeded.replace("$$pref_max_dev", maxDevValue);
    }

    public String getWeatherRainingAndColdQuery(){
        return  weatherRainingAndColdQuery;
    }

    public String getWeatherColdQuery(){
        return weatherColdQuery;
    }

    public String getWeatherHotQuery(){
        return  weatherHotQuery;
    }

    public String getWeatherRainingQuery(){
        return weatherRainingQuery;
    }

    public String getWeatherOkayQuery(){
        return weatherOkayQuery;
    }

    public String getBatteryLOWQuery(){
        return  batteryLOWQuery;
    }

    public String getBatteryOkQuery(){
        return batteryOkQuery;
    }
}
