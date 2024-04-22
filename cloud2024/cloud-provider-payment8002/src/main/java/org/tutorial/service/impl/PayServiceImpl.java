package org.tutorial.service.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.entities.Pay;
import org.tutorial.repositories.PayRepository;
import org.tutorial.service.PayService;

@Service
public class PayServiceImpl implements PayService {
	@Autowired
	private PayRepository payRepository;

	@Override
	public int add(Pay pay) {
		payRepository.save(pay);
		return 1;
	}

	@Override
	public int delete(Integer id) {
		if (payRepository.findById(id).isPresent()) {
			payRepository.deleteById(id);
			return 1;
		}
		return 0;
	}

	@Override
	public int update(Pay source) {
		if (payRepository.findById(source.getId()).isPresent()) {
			Pay target = payRepository.findById(source.getId()).get();
			// 獲取target的屬性列表
			Field[] targetFields = target.getClass().getDeclaredFields();

			// 遍歷target的屬性，如果source中不包含對應屬性，則保留target中的屬性值
			for (Field field : targetFields) {
				// 設置字段為可訪問
				field.setAccessible(true);
				try {
					Object sourceValue = field.get(source);
					if (sourceValue != null) {
						field.set(target, sourceValue);
		            }
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			payRepository.save(target);
			return 1;
		}
		return 0;
	}

	@Override
	public Pay getById(Integer id) {
		return payRepository.findById(id).orElse(null);
	}

	@Override
	public List<Pay> getAll() {
		return payRepository.findAll();
	}
}
