resource "aws_sqs_queue" "incident_workflow" {
  name = "incident-workflow"

  visibility_timeout_seconds = 30
  message_retention_seconds  = 86400
}
