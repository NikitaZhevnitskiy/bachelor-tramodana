package no.sysco.middleware.tramodana.schema

object Topic {
  // topic names
  val SPANS_JSON_ORIGINAL = "spans-json-original"
  val SPANS = "spans"
  val TRACES = "traces"
  val PROCESSED_TRACES = "processed-traces"
  val TRACE_ID_ROOT_OPERATION = "trace-id-root-operation"
  val TRACE_ID_SEQ_SPAN = "trace-id-seq-span"
  val ROOT_SPAN_SEQ_SPAN = "root-span-seq-span"
  val ROOT_OPERATION_LIST_SEQ_SPAN = "root-operation-list-seq-span"
  val ROOT_OPERATION_SET_SEQ_SPANS = "root-operation-set-seq-spans"
  val ROOT_OPERATION_SET_SEQ_SPANS_TABLE = "root-operation-set-seq-spans-table"
  val ROOT_OPERATION_SET_SPAN_TREES = "root-operation-set-spantree"
  val ROOT_OPERATION_SET_SPAN_TREES_TABLE = "root-operation-set-spantree-table"
  // utils
  val EMPTY_KEY: String = ""
}
