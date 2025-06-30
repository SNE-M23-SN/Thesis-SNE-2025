variable "image-id" {
  type        = string
  description = "The ID of the operating system image for the VMs."
  validation {
    condition     = length(var.image-id) > 4
    error_message = "The image-id value must be a valid id, more than 4 "
  }
}
