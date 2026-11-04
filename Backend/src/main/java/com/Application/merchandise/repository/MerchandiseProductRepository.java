package com.skyline.merchandise.repository;

import com.skyline.merchandise.model.MerchandiseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchandiseProductRepository extends JpaRepository<MerchandiseProduct, Long> {
}
