variable "product" {
  default = ""
}

variable "component" {
  default = ""
}
variable "location" {
  default = "UK South"
}

variable "env" {
  default = ""
}

variable "subscription" {
  default = ""
}

variable "deployment_namespace" {
  default = ""
}

variable "common_tags" {
  type = map(string)
}

variable "appinsights_instrumentation_key" {
  default = ""
}
