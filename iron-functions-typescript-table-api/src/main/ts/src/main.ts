import {FlinkInput, FlinkIO, FlinkOutput, flinkType} from "iron-functions-sdk";
import { getDistance } from 'geolib';

class Input implements FlinkInput {
  @flinkType("ARRAY(DOUBLE)")
  coords_x: number[];
  @flinkType("ARRAY(DOUBLE)")
  coords_y: number[];
  prefix: string;
}

class Output implements FlinkOutput {
  result: string;

  constructor(result: string) {
    this.result = result;
  }
}

export function process() {
  const input: Input = FlinkIO.read();

  const start_coords = { latitude: input.coords_x[0], longitude: input.coords_x[1] };
  const end_coords = { latitude: input.coords_y[0], longitude: input.coords_y[1] };

  const distance = getDistance(start_coords, end_coords);

  const result = input.prefix + distance / 1000 + " km";

  FlinkIO.write(new Output(result));
}
