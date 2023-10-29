package com.ndt.be_stepupsneaker.core.admin.repository.product;

import com.ndt.be_stepupsneaker.core.admin.dto.request.product.AdminBrandRequest;
import com.ndt.be_stepupsneaker.entity.product.Brand;
import com.ndt.be_stepupsneaker.infrastructure.constant.ProductPropertiesStatus;
import com.ndt.be_stepupsneaker.repository.product.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminBrandRepository extends BrandRepository {
    @Query("""
    SELECT x FROM Brand x 
    WHERE (:#{#request.name} IS NULL OR :#{#request.name} LIKE '' OR x.name LIKE  CONCAT('%', :#{#request.name}, '%'))
     AND 
     (:#{#request.q} IS NULL OR :#{#request.q} LIKE '' OR x.name LIKE  CONCAT('%', :#{#request.q}, '%')) 
     AND 
     ((:status IS NULL) OR (x.status = :status)) 
     AND
     x.deleted=false
    """)
    Page<Brand> findAllBrand(@Param("request") AdminBrandRequest request, @Param("status") ProductPropertiesStatus status, Pageable pageable);

    Optional<Brand> findByName(String name);

    @Query("""
    SELECT x FROM Brand x WHERE (x.name = :name AND :name IN (SELECT y.name FROM Brand y WHERE y.id != :id))
    """)
    Optional<Brand> findByName(@Param("id") UUID id, @Param("name") String name);
}
