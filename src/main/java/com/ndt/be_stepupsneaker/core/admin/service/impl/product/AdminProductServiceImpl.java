package com.ndt.be_stepupsneaker.core.admin.service.impl.product;

import com.ndt.be_stepupsneaker.core.admin.dto.request.product.AdminProductRequest;
import com.ndt.be_stepupsneaker.core.admin.dto.response.product.AdminProductResponse;
import com.ndt.be_stepupsneaker.core.admin.mapper.product.AdminProductMapper;
import com.ndt.be_stepupsneaker.core.admin.repository.product.AdminProductRepository;
import com.ndt.be_stepupsneaker.core.admin.service.product.AdminProductService;
import com.ndt.be_stepupsneaker.core.common.base.PageableObject;
import com.ndt.be_stepupsneaker.entity.product.Product;
import com.ndt.be_stepupsneaker.infrastructure.exception.ApiException;
import com.ndt.be_stepupsneaker.infrastructure.exception.ResourceNotFoundException;
import com.ndt.be_stepupsneaker.util.CloudinaryUpload;
import com.ndt.be_stepupsneaker.util.PaginationUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Log4j2
public class AdminProductServiceImpl implements AdminProductService {
    
    @Autowired
    private CloudinaryUpload cloudinaryUpload;
    
    @Autowired
    private AdminProductRepository adminProductRepository;

    @Autowired
    private PaginationUtil paginationUtil;

    @Override
    public PageableObject<AdminProductResponse> findAllEntity(AdminProductRequest productRequest) {

        Pageable pageable = paginationUtil.pageable(productRequest);
        Page<Product> resp = adminProductRepository.findAllProduct(productRequest, productRequest.getStatus(), pageable);
        Page<AdminProductResponse> adminProductResponses = resp.map(AdminProductMapper.INSTANCE::productToAdminProductResponse);
        return new PageableObject<>(adminProductResponses);
    }

    @Override
    public Object create(AdminProductRequest productDTO) {
        Optional<Product> productOptional = adminProductRepository.findByName(productDTO.getName());
        if (productOptional.isPresent()){
            throw new ApiException("NAME IS EXIST");
        }

        productOptional = adminProductRepository.findByCode(productDTO.getCode());
        if (productOptional.isPresent()){
            throw new ApiException("CODE IS EXIST");
        }
        productDTO.setImage(cloudinaryUpload.upload(productDTO.getImage()));

        Product product = adminProductRepository.save(AdminProductMapper.INSTANCE.adminProductRequestToProduct(productDTO));

        return AdminProductMapper.INSTANCE.productToAdminProductResponse(product);
    }

    @Override
    public AdminProductResponse update(AdminProductRequest productDTO) {

        Optional<Product> productOptional = adminProductRepository.findByName(productDTO.getId(), productDTO.getName());
        if (productOptional.isPresent()){
            throw new ApiException("NAME IS EXIST");
        }
        productOptional = adminProductRepository.findByCode(productDTO.getId(), productDTO.getCode());
        if (productOptional.isPresent()){
            throw new ApiException("CODE IS EXIST");
        }

        productOptional = adminProductRepository.findById(productDTO.getId());
        if (productOptional.isEmpty()){
            throw new ResourceNotFoundException("Product IS NOT EXIST");
        }

        Product productSave = productOptional.get();

        productSave.setName(productDTO.getName());
        productSave.setStatus(productDTO.getStatus());
        productSave.setCode(productDTO.getCode());
        productSave.setDescription(productDTO.getDescription());
        productSave.setImage(productDTO.getImage());
        productSave.setImage(cloudinaryUpload.upload(productDTO.getImage()));
        return AdminProductMapper.INSTANCE.productToAdminProductResponse(adminProductRepository.save(productSave));
    }

    @Override
    public AdminProductResponse findById(String id) {
        Optional<Product> productOptional = adminProductRepository.findById(id);
        if (productOptional.isEmpty()){
            throw new RuntimeException("LOOI");
        }

        return AdminProductMapper.INSTANCE.productToAdminProductResponse(productOptional.get());
    }

    @Override
    public Boolean delete(String id) {
        Optional<Product> productOptional = adminProductRepository.findById(id);
        if (productOptional.isEmpty()){
            throw new ResourceNotFoundException("Product NOT FOUND");
        }
        Product product = productOptional.get();
        product.setDeleted(true);
        adminProductRepository.save(product);
        return true;
    }
}
