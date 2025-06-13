package dev.irontools.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class IronFunctionsOtelParsingDemo {

  public static void main(String[] args) {
    EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
    settings.getConfiguration().setString("parallelism.default", "4");
    TableEnvironment tEnv = TableEnvironment.create(settings);

    // Feel free to change the path to your local path
    Path currentPath =
        Paths.get(System.getProperty("user.dir") + "/iron-functions-otel-parsing-udf");

    tEnv.executeSql(
        "CREATE FUNCTION parse_otel_log AS 'com.demo.otel.LogParser' LANGUAGE JAVA USING JAR"
            + " '"
            + currentPath
            + "/LogParser.jar'");

    tEnv.sqlQuery(
            "SELECT parse_otel_log('{\"resourceLogs\":[{\"resource\":{\"attributes\":[{\"key\":\"resource-attr\",\"value\":{\"stringValue\":\"resource-attr-val-1\"}}]},\"scopeLogs\":[{\"scope\":{},\"logRecords\":[{\"timeUnixNano\":\"1581452773000000789\",\"severityNumber\":9,\"severityText\":\"Info\",\"body\":{\"stringValue\":\"something happened\"},\"attributes\":[{\"key\":\"customer\",\"value\":{\"stringValue\":\"acme\"}},{\"key\":\"env\",\"value\":{\"stringValue\":\"dev\"}}],\"droppedAttributesCount\":1,\"traceId\":\"\",\"spanId\":\"\"}]}]}]}') as log")
        .execute()
        .print();
  }
}
