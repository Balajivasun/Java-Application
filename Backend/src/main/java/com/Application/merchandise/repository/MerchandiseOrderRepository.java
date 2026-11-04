package com.skyline.merchandise.repository;

import com.skyline.merchandise.model.MerchandiseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MerchandiseOrderRepository extends JpaRepository<MerchandiseOrder, Long> {
    List<MerchandiseOrder> findByUserId(Long userId);
}
