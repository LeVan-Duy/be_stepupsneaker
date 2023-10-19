package com.ndt.be_stepupsneaker.core.admin.repository.product;

import com.ndt.be_stepupsneaker.core.admin.dto.request.product.AdminProductRequest;
import com.ndt.be_stepupsneaker.entity.product.Product;
import com.ndt.be_stepupsneaker.infrastructure.constant.ProductStatus;
import com.ndt.be_stepupsneaker.repository.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;



@Repository
public interface AdminProductRepository extends ProductRepository {
    @Query("""
    SELECT x FROM Product x 
    WHERE (
    :#{#request.name} IS NULL OR :#{#request.name} LIKE '' OR x.name LIKE  CONCAT('%', :#{#request.name}, '%') 
    AND 
    :#{#request.code} IS NULL OR :#{#request.code} LIKE '' OR x.name LIKE  CONCAT('%', :#{#request.code}, '%')
    ) 
    AND 
    ((:status IS NULL) OR (x.status = :status)) 
    AND
    x.deleted=false 
    """)
    Page<Product> findAllProduct(@Param("request") AdminProductRequest request, @Param("status") ProductStatus status, Pageable pageable);

    Optional<Product> findByName(String name);

    @Query("""
    SELECT x FROM Product x WHERE (x.name = :name AND :name IN (SELECT y.name FROM Product y WHERE y.id != :id))
    """)
    Optional<Product> findByName(@Param("id") UUID id, @Param("name") String name);

    @Query("""
    SELECT x FROM Product x WHERE x.code = :code AND :code IN (SELECT y.code FROM Product y WHERE y.id != :id)
    """)
    Optional<Product> findByCode(@Param("id")UUID id, @Param("code") String code);
    
    Optional<Product> findByCode(String code);

}