package dev.irontools.demo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

public class IronFunctionsAbiDecoderDemo {

  private static String readResourceFile(String resourceName) throws IOException {
    try (InputStream is = IronFunctionsAbiDecoderDemo.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new IllegalArgumentException("File not found: " + resourceName);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static void main(String[] args) throws IOException {
    EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
    settings.getConfiguration().setString("parallelism.default", "4");
    TableEnvironment tEnv = TableEnvironment.create(settings);

    String abiJson = readResourceFile("abi.json");

    // Feel free to change the path to your local path
    Path currentPath =
        Paths.get(System.getProperty("user.dir") + "/iron-functions-abi-decoding");

    tEnv.executeSql(
        "CREATE FUNCTION decode_log AS 'com.demo.abi.AbiDecoder' LANGUAGE JAVA USING JAR"
            + " '"
            + currentPath
            + "/AbiDecoder.jar'");

    // ABI JSON can also be passed inline
    tEnv.sqlQuery(
            "SELECT decode_log('" + abiJson + "', '0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef,0x000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2,0x00000000000000000000000028c6c06298d514db089934071355e5743bf21d60', '0x00000000000000000000000000000000000000000000000000000000000186a0')"
                + " as event_params")
        .execute()
        .collect()
        .forEachRemaining(System.out::println);
  }
}
