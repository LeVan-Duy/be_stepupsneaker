package com.ndt.be_stepupsneaker.core.client.dto.response.order;

import com.ndt.be_stepupsneaker.core.client.dto.response.customer.ClientAddressResponse;
import com.ndt.be_stepupsneaker.core.client.dto.response.customer.ClientCustomerResponse;
import com.ndt.be_stepupsneaker.core.client.dto.response.payment.ClientPaymentResponse;
import com.ndt.be_stepupsneaker.core.client.dto.response.voucher.ClientVoucherResponse;
import com.ndt.be_stepupsneaker.infrastructure.constant.OrderStatus;
import com.ndt.be_stepupsneaker.infrastructure.constant.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientOrderResponse {

    private String id;

    private ClientCustomerResponse customer;

    private ClientVoucherResponse voucher;

    private String phoneNumber;

    private ClientAddressResponse address;

    private String fullName;

    private float totalMoney;

    private float shippingMoney;

    private Long confirmationDate;

    private Long expectedDeliveryDate;

    private Long deliveryStartDate;

    private Long receivedDate;

    private Long createdAt;

    private OrderType type;

    private String note;

    private String code;

    private OrderStatus status;

    private List<ClientOrderDetailResponse> orderDetails;

    private List<ClientOrderHistoryResponse> orderHistories;

    private List<ClientPaymentResponse> payments;


}