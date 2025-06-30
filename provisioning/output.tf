# k8smaster
output "internal_ip_address_k8smaster" {
  value = yandex_compute_instance.k8smaster.network_interface.0.ip_address
}

output "external_ip_address_k8smaster" {
  value = yandex_compute_instance.k8smaster.network_interface.0.nat_ip_address
}


# liftoffstage
output "internal_ip_address_liftoffstage" {
  value = yandex_compute_instance.liftoffstage.network_interface.0.ip_address
}

output "external_ip_address_liftoffstage" {
  value = yandex_compute_instance.liftoffstage.network_interface.0.nat_ip_address
}

# firststage
output "internal_ip_address_firststage" {
  value = yandex_compute_instance.firststage.network_interface.0.ip_address
}

output "external_ip_address_firststage" {
  value = yandex_compute_instance.firststage.network_interface.0.nat_ip_address
}

# secondstage
output "internal_ip_address_secondstage" {
  value = yandex_compute_instance.secondstage.network_interface.0.ip_address
}

output "external_ip_address_secondstage" {
  value = yandex_compute_instance.secondstage.network_interface.0.nat_ip_address
}

# thirdstage
output "internal_ip_address_thirdstage" {
  value = yandex_compute_instance.thirdstage.network_interface.0.ip_address
}

output "external_ip_address_thirdstage" {
  value = yandex_compute_instance.thirdstage.network_interface.0.nat_ip_address
}

# sast
output "internal_ip_address_sast" {
  value = yandex_compute_instance.sast.network_interface.0.ip_address
}

output "external_ip_address_sast" {
  value = yandex_compute_instance.sast.network_interface.0.nat_ip_address
}
