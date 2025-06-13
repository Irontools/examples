package main

import (
	"encoding/json"
	"github.com/extism/go-pdk"
)

// +irontools:flink_input
type InputRawLog struct {
	RawLog string `json:"rawLog"`
}

// OtelLogs is the root struct for the OTEL logs payload
type OtelLogs struct {
	ResourceLogs []ResourceLogs `json:"resourceLogs"`
}

// ResourceLogs represents a collection of logs from a resource
type ResourceLogs struct {
	Resource  Resource    `json:"resource"`
	ScopeLogs []ScopeLogs `json:"scopeLogs"`
}

// Resource represents a source of logs
type Resource struct {
	Attributes []Attribute `json:"attributes"`
}

// ScopeLogs represents logs from a specific scope
type ScopeLogs struct {
	Scope      Scope       `json:"scope"`
	LogRecords []LogRecord `json:"logRecords"`
}

// Scope represents the instrumentation scope
type Scope struct {
	// Empty in the example, but included for completeness
}

// LogRecord represents an individual log entry
type LogRecord struct {
	TimeUnixNano           string      `json:"timeUnixNano"`
	SeverityNumber         int         `json:"severityNumber"`
	SeverityText           string      `json:"severityText"`
	Body                   Value       `json:"body"`
	Attributes             []Attribute `json:"attributes"`
	DroppedAttributesCount int         `json:"droppedAttributesCount"`
	TraceId                string      `json:"traceId"`
	SpanId                 string      `json:"spanId"`
}

// Attribute represents a key-value pair
type Attribute struct {
	Key   string `json:"key"`
	Value Value  `json:"value"`
}

// Value represents different types of values that can be stored
type Value struct {
	StringValue string `json:"stringValue,omitempty"`
	IntValue    string `json:"intValue,omitempty"`
	// Other value types could be added here as needed
}

// +irontools:flink_output
type Output struct {
	Result string `json:"result"`
}

//go:wasmexport process
func process() int32 {
	var rawInput InputRawLog
	err := pdk.InputJSON(&rawInput)
	if err != nil {
		pdk.SetError(err)
		return 1
	}

	var logs OtelLogs
	err = json.Unmarshal([]byte(rawInput.RawLog), &logs)
	if err != nil {
		pdk.SetError(err)
		return 1
	}

	// The logic assumes there is only one log in the ResourceLogs array
	var transformedLog Output
	for _, resourceLog := range logs.ResourceLogs {
		for _, scopeLog := range resourceLog.ScopeLogs {
			for _, logRecord := range scopeLog.LogRecords {
				// Extract severity and body, combine them
				transformedLog.Result = "[" + logRecord.SeverityText + "]: " + logRecord.Body.StringValue
			}
		}
	}

	err = pdk.OutputJSON(transformedLog)
	if err != nil {
		pdk.SetError(err)
		return 1
	}
	return 0
}

func main() {}
