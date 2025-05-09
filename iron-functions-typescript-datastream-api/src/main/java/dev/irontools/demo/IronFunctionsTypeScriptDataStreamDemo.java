package dev.irontools.demo;

import dev.irontools.flink.functions.row.IronWasmRowFunction;
import java.util.List;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.runtime.types.TypeInfoDataTypeConverter;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;

public class IronFunctionsTypeScriptDataStreamDemo {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    String wasmResourceFile =
        System.getenv().getOrDefault("WASM_RESOURCE_FILE", "/wasm/demo-telemetry.wasm");

    DataType sourceDataType =
        DataTypes.ROW(
            DataTypes.FIELD("id", DataTypes.STRING()),
            DataTypes.FIELD("timestamp", DataTypes.BIGINT()),
            DataTypes.FIELD("event_type", DataTypes.STRING()),
            DataTypes.FIELD("version", DataTypes.STRING()),
            DataTypes.FIELD("data", DataTypes.STRING()));

    RowTypeInfo rowTypeInfo =
        (RowTypeInfo) TypeInfoDataTypeConverter.fromDataTypeToTypeInfo(sourceDataType);

    List<Row> sourceData = DataGen.generatePayloadsAsRows(100);

    DataStream<Row> sourceStream = env.fromData(sourceData).returns(rowTypeInfo);

    DataType transformedDataType =
        DataTypes.ROW(
            DataTypes.FIELD("id", DataTypes.STRING()),
            DataTypes.FIELD("version", DataTypes.STRING()),
            DataTypes.FIELD("timestamp", DataTypes.BIGINT()),
            DataTypes.FIELD("event_type", DataTypes.STRING()),
            DataTypes.FIELD("value", DataTypes.DOUBLE()),
            DataTypes.FIELD("device_id", DataTypes.STRING()),
            DataTypes.FIELD("device_model", DataTypes.STRING()),
            DataTypes.FIELD("device_tags", DataTypes.STRING()));

    DataStream<Row> transformedStream =
        sourceStream.process(
            IronWasmRowFunction.builder()
                .withInputTypeInfo(sourceStream.getType())
                .withOutputDataType(transformedDataType)
                .unnestOutput()
                .withWasmResourceFile(wasmResourceFile)
                .build());

    transformedStream.print();

    env.execute("Iron Functions TypeScript DataStream Demo");
  }
}
