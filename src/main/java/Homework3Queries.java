import java.util.ArrayList;
import java.util.List;

public class Homework3Queries {
    private static String PREFIX = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX dsbase:<http://data.cdc.gov/resource/>" +
            "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX socrata:<http://www.socrata.com/rdf/terms#>" +
            "PREFIX ds:<http://data.cdc.gov/resource/hc4f-j6nb/>";

    private static List<QueryObject> queryObjects;

    public static void main(String[] args) {
        QueryService queryService = QueryService.buildOwlQueryService("covidData.ttl");

        queryObjects.stream().forEach(queryObject -> queryService.selectQuery(queryObject));
    }

    static {
        queryObjects = new ArrayList<>();
        queryObjects.add(new QueryObject("What are some statistics on covid-19 for people age 55-64?",
                        PREFIX + "SELECT  ?expectedDeathPercentage ?covidDeaths " +
                                "WHERE {" +
                                "?subject ds:group 'By age';" +
                                            "ds:indicator '55-64 years';" +
                                            "ds:percent_expected_deaths ?expectedDeathPercentage;" +
                                            "ds:covid_deaths ?covidDeaths." +
                                "}", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("What are the number of deaths in 15 states from covid-19 based on current CDC data?",
                                        PREFIX + "SELECT  ?state ?deaths " +
                                                "WHERE {" + "?subject ds:group 'By state';" +
                                                                        "ds:indicator ?state;" +
                                                                        "ds:covid_deaths ?deaths." +
                                                "} LIMIT 15", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("What are the number of deaths from covid-19 organzied by week?",
                                        PREFIX + "SELECT ?week ?deaths " +
                                                "WHERE { ?subject ds:group 'By week';" +
                                                                    "ds:indicator ?week;" +
                                                                    "ds:covid_deaths ?deaths." +
                                                "}", QueryObject.QueryType.SELECT));
    }
}
