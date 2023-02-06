package com.veeva.vault.tools.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veeva.vault.vapil.api.model.VaultModel;

public class SdkDebugLogEntry extends VaultModel {

	@JsonProperty("timestamp")
	String getTimestanp() { return this.getString("timestamp"); }
	void setTimestamp(String timestamp) { this.set("timestamp", timestamp); }

	@JsonProperty("log_file")
	String getLogFile() { return this.getString("log_file"); }
	void setLogFile(String logFile) { this.set("log_file", logFile); }

	@JsonProperty("type")
	String getType() { return this.getString("type"); }
	void setType(String type) { this.set("type", type); }

	@JsonProperty("category")
	String getCategory() { return this.getString("category"); }
	void setCategory(String category) { this.set("category", category); }

	@JsonProperty("class_name")
	String getClassName() { return this.getString("class_name"); }
	void setClassName(String className) { this.set("class_name", className); }

	@JsonProperty("service_method")
	String getServiceMethod() { return this.getString("service_method"); }
	void setServiceMethod(String serviceMethod) {
		this.set("service_method", serviceMethod);
	}

	@JsonProperty("service_name")
	String getServiceName() {
		String serviceMethod = getServiceMethod();
		if (serviceMethod != null) {
			return serviceMethod.substring(0, serviceMethod.indexOf("#"));
		}
		return null;
	}

	@JsonProperty("method_name")
	String getMethodName() {
		String serviceMethod = getServiceMethod();
		if (serviceMethod != null) {
			return serviceMethod.substring(serviceMethod.indexOf("#") + 1);
		}
		return null;
	}

	@JsonProperty("elapsed_time")
	Long getElapsedTime() { return this.getLong("elapsed_time", Long.valueOf(0)); }
	void setElapsedTime(Long elapsedTime) {
		this.set("elapsed_time", elapsedTime);
	}

	@JsonProperty("elapsed_time_seconds")
	Double getElapsedTimeSeconds() {
		return getLongAsDouble(getElapsedTime(), 1000);
	}

	@JsonProperty("cpu_time")
	Long getCpuTime() { return this.getLong("cpu_time", Long.valueOf(0)); }
	void setCpuTime(Long cpuTime) { this.set("cpu_time", cpuTime); }

	@JsonProperty("cpu_time_seconds")
	Double getCpuTimeSeconds() {
		return getLongAsDouble(getCpuTime(), 1000000000);
	}

	@JsonProperty("memory")
	Long getMemory() { return this.getLong("memory", Long.valueOf(0)); }
	void setMemory(Long memory) { this.set("memory", memory); }

	@JsonProperty("memory_mb")
	Double getMemoryMb() {
		return getLongAsDouble(getMemory(), 1000000);
	}

	@JsonProperty("gross_memory")
	Long getGrossMemory() { return this.getLong("gross_memory", Long.valueOf(0)); }
	void setGrossMemory(Long grossMemory) { this.set("gross_memory", grossMemory); }

	@JsonProperty("gross_memory_mb")
	Double getGrossMemoryMb() {
		return getLongAsDouble(getGrossMemory(), 1000000);
	}

	@JsonProperty("invocation_count")
	Long getInvocationCount() { return this.getLong("invocation_count", Long.valueOf(0)); }
	void setInvocationCount(Long invocationCount) { this.set("invocation_count", invocationCount); }

	@JsonProperty("message")
	String getMessage() { return this.getString("message"); }
	void setMessage(String message) { this.set("message", message); }


	private Integer getInteger(String fieldName, Integer defaultValue) {
		Integer value = this.getInteger(fieldName);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	private Long getLong(String fieldName, Long defaultValue) {
		Long value = this.getLong(fieldName);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	private Double getIntegerAsDouble(Integer value, Integer factor) {
		if (value != null) {
			if (factor != null && factor.intValue() > 0) {
				return Double.valueOf(value) / factor;
			}
			else {
				return Double.valueOf(value);
			}
		}
		return null;
	}

	private Double getLongAsDouble(Long value, Integer factor) {
		if (value != null) {
			if (factor != null && factor.intValue() > 0) {
				return Double.valueOf(value) / factor;
			}
			else {
				return Double.valueOf(value);
			}
		}
		return null;
	}
}
