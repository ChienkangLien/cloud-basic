package org.tutorial.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.repositories.StorageRepository;
import org.tutorial.service.StorageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

	@Autowired
	private StorageRepository storageRepository;

	@Override
	public void decrease(Long productId, Integer count) {
		log.info("------------->StorageService 開始扣減庫存");
		storageRepository.decrease(productId, count);
		log.info("------------->StorageService 扣減庫存結束");
	}

}
