terraform {
  required_providers {
    yandex = {
      source = "yandex-cloud/yandex"
    }
  }

    backend "s3" {
      endpoints = {
        s3 = "https://storage.yandexcloud.net"
      }
      bucket = "tofuz"
      region = "ru-central1-a"
      key    = "terraform.tfstate"
  
      skip_region_validation      = true
      skip_credentials_validation = true
      skip_requesting_account_id  = true # Необходимая опция Terraform для версии 1.6.1 и старше.
      skip_s3_checksum            = true # Необходимая опция при описании бэкенда для Terraform версии 1.6.3 и старше.
  
    }
  
}

provider "yandex" {
  token     = var.yandex_token
  cloud_id  = var.yandex_cloud_id
  folder_id = "b1g5enj0v0e6600a5o4a"
  zone      = "ru-central1-a"
}



# K8S-master(For controlling cluster)
resource "yandex_compute_instance" "k8smaster" {
  name                      = "k8smaster"
  allow_stopping_for_update = "true"

  platform_id = "standard-v1"

  scheduling_policy {
    preemptible = true
  }


  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 50
    }

  }

  resources {
    core_fraction = 100
    cores         = 2
    memory        = 4
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}


# K8S-LiftOffStage(For deploying LiftOffStage)
resource "yandex_compute_instance" "liftoffstage" {
  name                      = "liftoffstage"
  allow_stopping_for_update = "true"

  platform_id = "standard-v3"

  scheduling_policy {
    preemptible = true
  }

  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 100
    }

  }

  resources {
    core_fraction = 100
    cores         = 16
    memory        = 16
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}



# K8S-FirstStage(For deploying FirstStage)
resource "yandex_compute_instance" "firststage" {
  name                      = "firststage"
  allow_stopping_for_update = "true"

  platform_id = "standard-v3"

  scheduling_policy {
    preemptible = true
  }

  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 25
    }

  }

  resources {
    core_fraction = 100
    cores         = 8
    memory        = 8
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}



# K8S-SecondStage(For deploying SecondStage)
resource "yandex_compute_instance" "secondstage" {
  name                      = "secondstage"
  allow_stopping_for_update = "true"

  platform_id = "standard-v3"

  scheduling_policy {
    preemptible = true
  }

  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 20
    }

  }

  resources {
    core_fraction = 100
    cores         = 12
    memory        = 12
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}



# K8S-ThirdStage(For deploying ThirdStage)
resource "yandex_compute_instance" "thirdstage" {
  name                      = "thirdstage"
  allow_stopping_for_update = "true"

  platform_id = "standard-v3"

  scheduling_policy {
    preemptible = true
  }

  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 20
    }

  }

  resources {
    core_fraction = 100
    cores         = 4
    memory        = 4
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}


# K8S-SAST(For deploying SAST)
resource "yandex_compute_instance" "sast" {
  name                      = "sast"
  allow_stopping_for_update = "true"

  platform_id = "standard-v3"

  scheduling_policy {
    preemptible = true
  }

  boot_disk {
    initialize_params {
      image_id = var.image-id
      size     = 50
    }

  }

  resources {
    core_fraction = 100
    cores         = 24
    memory        = 24
  }

  network_interface {
    subnet_id = yandex_vpc_subnet.subnet-1.id
    nat       = true
  }


  metadata = {
    ssh-keys = var.public_key
  }

}



resource "yandex_vpc_network" "network-1" {
  name = "from-final-project-network"
}

resource "yandex_vpc_subnet" "subnet-1" {
  name           = "from-final-project-subnet"
  zone           = "ru-central1-a"
  network_id     = yandex_vpc_network.network-1.id
  v4_cidr_blocks = ["192.168.0.0/16"]
}

variable "yandex_token" {
  description = "Yandex Cloud token"
}

variable "yandex_cloud_id" {
  description = "Yandex Cloud ID"
}

variable "public_key" {
  type = string
}

resource "local_file" "inventory_tmpl" {
  content = templatefile("${path.module}/templates/inventory.tpl",
    {
      master_node_external_ip       = yandex_compute_instance.k8smaster.network_interface.0.nat_ip_address
      liftoffstage_node_external_ip = yandex_compute_instance.liftoffstage.network_interface.0.nat_ip_address
      firststage_node_external_ip   = yandex_compute_instance.firststage.network_interface.0.nat_ip_address
      secondstage_node_external_ip  = yandex_compute_instance.secondstage.network_interface.0.nat_ip_address
      thirdstage_node_external_ip   = yandex_compute_instance.thirdstage.network_interface.0.nat_ip_address
      sast_node_external_ip         = yandex_compute_instance.sast.network_interface.0.nat_ip_address
    }
  )
  filename = "../configuring/inventory"
}




