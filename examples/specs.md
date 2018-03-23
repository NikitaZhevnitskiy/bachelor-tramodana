## Specs v1
### Required fields of span
* trace_id
* span_id
* duration
* operation_name
* parent_id
* process (service_name)
* start_time
* refs (test it)

### Plan
0. Create all topics before run main topology
0. Aggregate by trace_id and write to kafka (raw-traces [trace_id, List[Spans]])
1. Build tree - 
  a. find root of the Tree 
  b. subtrees and order spans (sequential order) 
  c. write to kafka (processed-traces [trace-id, own object contain Tree[Span]])
2. Cleaning
  a. Have same operations in same sequence 

#### Model entity
* trace_model_id
* 