[master]
master ansible_host=${master_node_external_ip}

[liftoffstage]
liftoffstage ansible_host=${liftoffstage_node_external_ip}

[firststage]
firststage ansible_host=${firststage_node_external_ip}

[secondstage]
secondstage ansible_host=${secondstage_node_external_ip}

[thirdstage]
thirdstage ansible_host=${thirdstage_node_external_ip}

[sast]
sast ansible_host=${sast_node_external_ip}

