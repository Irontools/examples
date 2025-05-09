import { FlinkIO } from "iron-functions-sdk";

class Input {
  id: string;
  timestamp: bigint;
  event_type: string;
  version: string;
  data: string;
}

export function process() {
  const input: Input = FlinkIO.read();

  const result = [];
  const telemetryPayloads = JSON.parse(input.data);
  for (const telemetryPayload of telemetryPayloads) {
    const device_id = telemetryPayload.device.id;
    const device_model = telemetryPayload.device.model;
    const device_tags = telemetryPayload.tags.join(", ");

    const { data, ...otherFields } = input;

    const payload = {
      device_id,
      device_model,
      device_tags,
      value: telemetryPayload.value,
      ...otherFields
    };
    result.push(payload);
  }

  FlinkIO.write(result);
}
