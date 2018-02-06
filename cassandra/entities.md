## Tables
[Cassandra datatypes](https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cql_data_types_c.html)

### operation_names
```
service_name text,
operation_name text,
PRIMARY KEY (service_name, operation_name)
```
### service_names
```
service_name text PRIMARY KEY
```
### tag_index
``` 
service_name text,
tag_key text,
tag_value text,
start_time bigint,
trace_id blob,
span_id bigint,
PRIMARY KEY ((service_name, tag_key, tag_value), start_time, trace_id, span_id)
```
### traces
```
trace_id blob,
span_id bigint,
span_hash bigint,
duration bigint,
flags int,
logs list<frozen<log>>,
operation_name text,
parent_id bigint,
process frozen<process>,
refs list<frozen<span_ref>>,
start_time bigint,
tags list<frozen<keyvalue>>,
PRIMARY KEY (trace_id, span_id, span_hash)
```

### service_name_index
``` 
service_name text,
bucket int,
start_time bigint,
trace_id blob,
PRIMARY KEY ((service_name, bucket), start_time)
```
### dependencies
``` 
ts timestamp PRIMARY KEY,
dependencies list<frozen<dependency>>,
ts_index timestamp
```
### service_operation_index
```
service_name text,
operation_name text,
start_time bigint,
trace_id blob,
PRIMARY KEY ((service_name, operation_name), start_time)
```
### duration_index
``` 
service_name text,
operation_name text,
bucket timestamp,
duration bigint,
start_time bigint,
trace_id blob,
PRIMARY KEY ((service_name, operation_name, bucket), duration, start_time, trace_id)
```