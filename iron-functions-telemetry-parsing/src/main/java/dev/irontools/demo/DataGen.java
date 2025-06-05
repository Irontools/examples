package dev.irontools.demo;

import com.alibaba.fastjson2.JSON;
import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.flink.types.Row;

public class DataGen {
  public static class Payload {
    public String id;
    public long timestamp;
    public String version;
    public String event_type;
    public String data;
  }

  public static class Device {
    public String id;
    public String model;
  }

  public static class DeviceData {
    public Device device;
    public double value;
    public List<String> tags = new ArrayList<>();
  }

  private static final Faker faker = new Faker();
  private static final Random random = new Random();

  private static final List<String> eventTypes = List.of("AccK", "Temp", "Prs", "Zsc");

  public static List<Row> generatePayloadsAsRows(int numRecords) {
    List<Payload> payloads = generatePayloads(numRecords);
    List<Row> rows = new ArrayList<>();
    for (Payload payload : payloads) {
      Row row =
          Row.of(payload.id, payload.timestamp, payload.version, payload.event_type, payload.data);
      rows.add(row);
    }
    return rows;
  }

  public static List<Payload> generatePayloads(int numRecords) {
    List<Payload> payloads = new ArrayList<>();

    for (int i = 0; i < numRecords; i++) {
      Payload payload = new Payload();

      payload.id = UUID.randomUUID().toString();
      payload.timestamp = System.currentTimeMillis();
      payload.version =
          "v" + faker.number().numberBetween(1, 8) + "." + faker.number().numberBetween(0, 4);
      payload.event_type = eventTypes.get(random.nextInt(eventTypes.size()));
      payload.data = JSON.toJSONString(generateDeviceData(faker.number().numberBetween(3, 10)));

      payloads.add(payload);
    }

    return payloads;
  }

  private static List<DeviceData> generateDeviceData(int numRecords) {
    List<DeviceData> deviceDataList = new ArrayList<>();

    for (int i = 0; i < numRecords; i++) {
      Device device = new Device();
      device.id = UUID.randomUUID().toString();
      device.model = generateModel();

      DeviceData deviceData = new DeviceData();
      deviceData.device = device;
      deviceData.value = faker.number().randomDouble(2, 0, 100);
      deviceData.tags.add(faker.lorem().word());
      deviceData.tags.add(faker.color().name());
      if (random.nextBoolean()) {
        deviceData.tags.add(faker.lorem().word());
      }

      deviceDataList.add(deviceData);
    }

    return deviceDataList;
  }

  private static String generateModel() {
    String model = faker.hobbit().thorinsCompany();
    if (model.contains(" ") || model.contains("Gandalf")) {
      // skipping Thorin, Bilbo, and Gandalf
      model = generateModel();
    }
    return model;
  }
}
