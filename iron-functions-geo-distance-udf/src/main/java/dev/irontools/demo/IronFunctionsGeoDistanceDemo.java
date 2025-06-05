package dev.irontools.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class IronFunctionsGeoDistanceDemo {

  public static void main(String[] args) {
    EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
    settings.getConfiguration().setString("parallelism.default", "4");
    TableEnvironment tEnv = TableEnvironment.create(settings);

    // Feel free to change the path to your local path
    Path currentPath =
        Paths.get(System.getProperty("user.dir") + "/iron-functions-geo-distance-udf");

    tEnv.executeSql(
        "CREATE FUNCTION geo_distance AS 'com.demo.geo.GeoDistance' LANGUAGE JAVA USING JAR"
            + " '"
            + currentPath
            + "/GeoDistance.jar'");

    tEnv.sqlQuery(
            "SELECT geo_distance(ARRAY[51.5072, -0.1275], ARRAY[49.2608, -123.1138], 'Distance: ')"
                + " as Distance")
        .execute()
        .print();
  }
}
