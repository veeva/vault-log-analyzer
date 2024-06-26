DROP TABLE IF EXISTS stats;
CREATE TABLE stats AS
SELECT
    substr( timestamp, 1, 10 ) as api_date,
    vault_id,
    username,
    user_id,
    connection,
    api_version,
    api_endpoint,
    api_resource,
    api_response_status,
    api_response_error_type,
    api_response_error_message,
    api_response_warning_type,
    api_response_warning_message,
    client_id,
    reference_id,
    client_ip,
    http_method,
    http_response_status,
    COUNT(*) AS api_count,
    SUM(duration) AS duration_milliseconds,
    CAST(SUM(duration) as REAL) / 1000 AS duration_seconds,
    CAST(SUM(duration) as REAL) / 60000 AS duration_minutes,
    SUM(response_delay) AS response_delay_milliseconds,
    CAST(SUM(response_delay) as REAL) / 1000 AS response_delay_seconds,
    CAST(SUM(response_delay) as REAL) / 60000 AS response_delay_minutes,
    SUM(sdk_count) AS sdk_count,
    SUM(sdk_elapsed_time) AS sdk_elapsed_time_milliseconds,
    CAST(SUM(sdk_elapsed_time) as REAL) / 1000 AS sdk_elapsed_time_seconds,
    CAST(SUM(sdk_elapsed_time) as REAL) / 60000 AS sdk_elapsed_time_minutes,
    SUM(sdk_cpu_time) AS sdk_cpu_time_nanoseconds,
    CAST(SUM(sdk_cpu_time) as REAL) / 1000000000 AS sdk_cpu_time_seconds,
    SUM(sdk_gross_memory) AS sdk_gross_memory,
    CAST(SUM(sdk_gross_memory) as REAL) / 1000000 AS sdk_gross_memory_mb
FROM api
GROUP BY
    api_date,
    vault_id,
    username,
    user_id,
    connection,
    api_version,
    api_endpoint,
    api_resource,
    api_response_status,
    api_response_error_type,
    client_id,
    client_ip,
    http_method,
    http_response_status
ORDER BY timestamp
;