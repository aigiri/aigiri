package io.kutumbini.auth.persistence.dao;

import java.util.List;

import io.kutumbini.auth.persistence.model.DeviceMetadata;
import org.springframework.stereotype.Service;

@Service
public class DeviceMetadataRepository  {

	public List<DeviceMetadata> findByUserId(Long userId) {
		return null;
	}

	public void save(DeviceMetadata deviceMetadata) {
	}
}
