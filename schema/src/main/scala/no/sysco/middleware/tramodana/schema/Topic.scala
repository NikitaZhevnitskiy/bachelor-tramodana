package no.sysco.middleware.tramodana.schema

object Topic {
  // topic names
  final val SPANS_ORIGINAL_TOPIC: String = "spans-json-original"

  final val SPANS_JSON_ORIGINAL = "spans-json-original"
  final val SPANS = "spans"
  final val TRACES = "traces"
  final val PROCESSED_TRACES = "processed-traces"
  final val TRACE_ID_ROOT_OPERATION = "trace-id-root-operation"
  final val TRACE_ID_SEQ_SPAN = "trace-id-seq-span"
  final val ROOT_SPAN_SEQ_SPAN = "root-span-seq-span"
  final val ROOT_OPERATION_LIST_SEQ_SPAN = "root-operation-list-seq-span"
  final val ROOT_OPERATION_SET_SEQ_SPANS = "root-operation-set-seq-spans"
  final val ROOT_OPERATION_SET_SEQ_SPANS_TABLE = "root-operation-set-seq-spans-table"
  final val ROOT_OPERATION_SET_SPAN_TREES = "root-operation-set-spantree"
  final val ROOT_OPERATION_SET_SPAN_TREES_TABLE = "root-operation-set-spantree-table"
  final val ROOT_OPERATION_BPMN_XML = "root-operation-bpmn-xml"


  // utils
  final val EMPTY_KEY: String = ""
}
