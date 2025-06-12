use serde_json::Value;
use extism_pdk::*;
use iron_functions_sdk::*;
use serde::{Serialize, Deserialize};
use ethabi_decode::{Event, Param, ParamKind, Token, H256};

#[flink_input]
#[derive(Deserialize)]
struct InputData {
    abi: String,
    topics: String,
    data: String,
}

#[flink_output]
#[derive(Serialize)]
struct OutputData {
    decoded: String,
}

#[derive(Debug, Serialize)]
struct EventParams(Vec<String>);

#[plugin_fn]
pub fn process(input: String) -> FnResult<String> {
    let input: InputData = serde_json::from_str(&input)?;
    let output = process_internal(input);
    Ok(serde_json::to_string(&output)?)
}

fn process_internal(input: InputData) -> OutputData {
    match decode_log(input.abi, input.topics, input.data) {
        Ok(params) => {
            let decoded = params.0.join(",");
            OutputData { decoded }
        }
        Err(e) => {
            error!("Could not decode log: {}", e);
            OutputData {
                decoded: format!("error: {}", e),
            }
        }
    }
}

fn decode_log(abi_json_str: String, topics_str: String, data_hex_str: String) -> Result<EventParams, String> {
    let abi: Value = serde_json::from_str(&abi_json_str)
        .map_err(|e| format!("Failed to parse ABI JSON: {}", e))?;
    
    // Get the first event from the ABI array
    let event_abi = abi.as_array()
        .and_then(|arr| arr.first())
        .ok_or_else(|| "No event found in ABI".to_string())?;
    
    let name = event_abi.get("name")
        .and_then(|v| v.as_str())
        .ok_or_else(|| "Missing event name in ABI".to_string())?;
    
    let inputs = event_abi.get("inputs")
        .and_then(|v| v.as_array())
        .ok_or_else(|| "Missing inputs in ABI".to_string())?;
    
    // Build the event signature
    let mut signature = format!("{}(", name);
    let mut param_types = Vec::new();
    for input in inputs {
        let param_type = input.get("type")
            .and_then(|v| v.as_str())
            .ok_or_else(|| "Missing type in input".to_string())?;
        param_types.push(param_type.to_string());
    }
    signature.push_str(&param_types.join(","));
    signature.push(')');
    
    // Create event parameters
    let mut event_params = Vec::new();
    for input in inputs {
        let param_type = input.get("type")
            .and_then(|v| v.as_str())
            .ok_or_else(|| "Missing type in input".to_string())?;
        let indexed = input.get("indexed")
            .and_then(|v| v.as_bool())
            .unwrap_or(false);
        
        let kind = match param_type {
            "address" => ParamKind::Address,
            "uint256" => ParamKind::Uint(256),
            "int256" => ParamKind::Int(256),
            "bool" => ParamKind::Bool,
            "string" => ParamKind::String,
            "bytes" => ParamKind::Bytes,
            _ => return Err(format!("Unsupported parameter type: {}", param_type)),
        };
        
        event_params.push(Param { kind, indexed });
    }
    
    // Create the event
    let event = Event {
        signature: &signature,
        inputs: &event_params,
        anonymous: event_abi.get("anonymous")
            .and_then(|v| v.as_bool())
            .unwrap_or(false),
    };
    
    // Parse topics from comma-separated string
    let topics: Vec<H256> = topics_str
        .split(',')
        .map(|s| s.trim())
        .filter(|s| !s.is_empty())
        .map(|s| {
            s.strip_prefix("0x")
                .ok_or_else(|| format!("Topic must start with 0x: {}", s))
                .and_then(|s| hex::decode(s)
                    .map_err(|e| format!("Failed to decode topic hex: {}", e)))
                .map(|bytes| {
                    let mut result = [0u8; 32];
                    result.copy_from_slice(&bytes);
                    H256(result)
                })
        })
        .collect::<Result<Vec<H256>, String>>()?;
    
    let data = data_hex_str.strip_prefix("0x")
        .ok_or_else(|| "Data must start with 0x".to_string())?;
    let data = hex::decode(data)
        .map_err(|e| format!("Failed to decode data: {}", e))?;
    
    let tokens = event.decode(topics, data)
        .map_err(|e| format!("Failed to decode event: {:?}", e))?;
    
    // Convert tokens to EventParams
    let params: Vec<String> = tokens.iter()
        .map(|token| match token {
            Token::Address(addr) => format!("0x{}", hex::encode(addr)),
            Token::Uint(val) => val.to_string(),
            Token::Int(val) => val.to_string(),
            Token::Bool(val) => val.to_string(),
            Token::String(s) => String::from_utf8_lossy(s).to_string(),
            Token::Bytes(b) => format!("0x{}", hex::encode(b)),
            _ => format!("{:?}", token),
        })
        .collect();
    
    Ok(EventParams(params))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_decode_simple_event() {
        let abi_json = r#"{
            "anonymous": false,
            "inputs": [
                {"indexed": true, "internalType": "address", "name": "from", "type": "address"},
                {"indexed": true, "internalType": "address", "name": "to", "type": "address"},
                {"indexed": false, "internalType": "uint256", "name": "amount", "type": "uint256"}
            ],
            "name": "Transfer",
            "type": "event"
        }"#;
        let event_abi_json = format!("[{}]", abi_json);

        let topics_str = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef,0x000000000000000000000000c02aaa39b223fe8d0a0e5c4f27ead9083c756cc2,0x00000000000000000000000028c6c06298d514db089934071355e5743bf21d60";
        let data_hex = "0x00000000000000000000000000000000000000000000000000000000000186a0";

        match decode_log(event_abi_json.to_string(), topics_str.to_string(), data_hex.to_string()) {
            Ok(params) => {
                assert_eq!(params.0.len(), 3);
                assert_eq!(params.0[0], "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2");
                assert_eq!(params.0[1], "0x28c6c06298d514db089934071355e5743bf21d60");
                assert_eq!(params.0[2], "100000");
            }
            Err(e) => panic!("decode_log failed: {}", e),
        }
    }
}
