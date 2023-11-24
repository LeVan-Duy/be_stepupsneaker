package com.ndt.be_stepupsneaker.core.client.service.impl.voucher;
import com.ndt.be_stepupsneaker.core.client.dto.request.voucher.ClientVoucherRequest;
import com.ndt.be_stepupsneaker.core.client.dto.response.voucher.ClientVoucherResponse;
import com.ndt.be_stepupsneaker.core.client.mapper.voucher.ClientVoucherMapper;
import com.ndt.be_stepupsneaker.core.client.repository.voucher.ClientVoucherRepository;
import com.ndt.be_stepupsneaker.core.client.service.voucher.ClientVoucherService;
import com.ndt.be_stepupsneaker.core.common.base.PageableObject;
import com.ndt.be_stepupsneaker.entity.voucher.Voucher;
import com.ndt.be_stepupsneaker.infrastructure.exception.ResourceNotFoundException;
import com.ndt.be_stepupsneaker.infrastructure.scheduled.AutoScheduledService;
import com.ndt.be_stepupsneaker.repository.voucher.CustomerVoucherRepository;
import com.ndt.be_stepupsneaker.util.CloudinaryUpload;
import com.ndt.be_stepupsneaker.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientVoucherServiceImpl implements ClientVoucherService {


    @Qualifier("clientVoucherRepository")
    private ClientVoucherRepository ClientVoucherRepository;
    private PaginationUtil paginationUtil;
    private CustomerVoucherRepository customerVoucherRepository;
    private AutoScheduledService autoScheduledService;
    private CloudinaryUpload cloudinaryUpload;

    @Autowired
    public ClientVoucherServiceImpl(ClientVoucherRepository ClientVoucherRepository,
                                    PaginationUtil paginationUtil,
                                    CustomerVoucherRepository customerVoucherRepository,
                                    AutoScheduledService autoScheduledService,
                                    CloudinaryUpload cloudinaryUpload) {
        this.ClientVoucherRepository = ClientVoucherRepository;
        this.paginationUtil = paginationUtil;
        this.customerVoucherRepository = customerVoucherRepository;
        this.autoScheduledService = autoScheduledService;
        this.cloudinaryUpload = cloudinaryUpload;
    }

    @Override
    public PageableObject<ClientVoucherResponse> findAllEntity(ClientVoucherRequest voucherRequest) {
        return null;
    }

    @Override
    public Object create(ClientVoucherRequest voucherRequest) {
        return null;
    }

    @Override
    public ClientVoucherResponse update(ClientVoucherRequest voucherRequest) {
        return null;
    }

    @Override
    public ClientVoucherResponse findById(String id) {
        Optional<Voucher> optionalVoucher = ClientVoucherRepository.findById(id);
        if (optionalVoucher.isEmpty()) {
            throw new ResourceNotFoundException("VOUCHER IS NOT EXIST :" + id);
        }

        return ClientVoucherMapper.INSTANCE.voucherToClientVoucherResponse(optionalVoucher.get());
    }

    @Override
    public Boolean delete(String id) {
        Optional<Voucher> optionalVoucher = ClientVoucherRepository.findById(id);
        if (optionalVoucher.isEmpty()) {
            throw new ResourceNotFoundException("VOUCHER NOT FOUND :" + id);
        }
        Voucher newVoucher = optionalVoucher.get();
        newVoucher.setDeleted(true);
        ClientVoucherRepository.save(newVoucher);
        return true;
    }


    @Override
    public PageableObject<ClientVoucherResponse> findAllVoucher(ClientVoucherRequest voucherReq, String customerId, String noCustomerId) {
        Pageable pageable = paginationUtil.pageable(voucherReq);
        Page<Voucher> resp = ClientVoucherRepository.findAllVoucher(voucherReq, pageable, voucherReq.getStatus(), voucherReq.getType(), customerId, noCustomerId);
        Page<ClientVoucherResponse> ClientVoucherResponsePage = resp.map(ClientVoucherMapper.INSTANCE::voucherToClientVoucherResponse);
        return new PageableObject<>(ClientVoucherResponsePage);
    }


}