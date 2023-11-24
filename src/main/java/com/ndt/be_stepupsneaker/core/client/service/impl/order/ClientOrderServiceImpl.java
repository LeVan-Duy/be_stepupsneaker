package com.ndt.be_stepupsneaker.core.client.service.impl.order;

import com.ndt.be_stepupsneaker.core.admin.repository.employee.AdminEmployeeRepository;
import com.ndt.be_stepupsneaker.core.client.dto.request.customer.ClientAddressRequest;
import com.ndt.be_stepupsneaker.core.client.dto.request.order.ClientCartItemRequest;
import com.ndt.be_stepupsneaker.core.client.dto.request.order.ClientOrderRequest;
import com.ndt.be_stepupsneaker.core.client.dto.request.order.ClientShippingRequest;
import com.ndt.be_stepupsneaker.core.client.dto.response.order.ClientOrderResponse;
import com.ndt.be_stepupsneaker.core.client.dto.response.order.ClientShippingDataResponse;
import com.ndt.be_stepupsneaker.core.client.dto.response.order.ClientShippingResponse;
import com.ndt.be_stepupsneaker.core.client.mapper.customer.ClientAddressMapper;
import com.ndt.be_stepupsneaker.core.client.mapper.order.ClientOrderMapper;
import com.ndt.be_stepupsneaker.core.client.repository.customer.ClientAddressRepository;
import com.ndt.be_stepupsneaker.core.client.repository.customer.ClientCustomerRepository;
import com.ndt.be_stepupsneaker.core.client.repository.order.ClientOrderDetailRepository;
import com.ndt.be_stepupsneaker.core.client.repository.order.ClientOrderHistoryRepository;
import com.ndt.be_stepupsneaker.core.client.repository.order.ClientOrderRepository;
import com.ndt.be_stepupsneaker.core.client.repository.payment.ClientPaymentMethodRepository;
import com.ndt.be_stepupsneaker.core.client.repository.payment.ClientPaymentRepository;
import com.ndt.be_stepupsneaker.core.client.repository.product.ClientProductDetailRepository;
import com.ndt.be_stepupsneaker.core.client.repository.voucher.ClientVoucherHistoryRepository;
import com.ndt.be_stepupsneaker.core.client.repository.voucher.ClientVoucherRepository;
import com.ndt.be_stepupsneaker.core.client.service.order.ClientOrderService;
import com.ndt.be_stepupsneaker.core.client.service.vnpay.VNPayService;
import com.ndt.be_stepupsneaker.core.common.base.PageableObject;
import com.ndt.be_stepupsneaker.entity.customer.Address;
import com.ndt.be_stepupsneaker.entity.order.Order;
import com.ndt.be_stepupsneaker.entity.order.OrderDetail;
import com.ndt.be_stepupsneaker.entity.order.OrderHistory;
import com.ndt.be_stepupsneaker.entity.payment.Payment;
import com.ndt.be_stepupsneaker.entity.payment.PaymentMethod;
import com.ndt.be_stepupsneaker.entity.product.ProductDetail;
import com.ndt.be_stepupsneaker.entity.voucher.Voucher;
import com.ndt.be_stepupsneaker.entity.voucher.VoucherHistory;
import com.ndt.be_stepupsneaker.infrastructure.constant.EntityProperties;
import com.ndt.be_stepupsneaker.infrastructure.constant.OrderStatus;
import com.ndt.be_stepupsneaker.infrastructure.constant.OrderType;
import com.ndt.be_stepupsneaker.infrastructure.constant.VoucherType;
import com.ndt.be_stepupsneaker.infrastructure.exception.ApiException;
import com.ndt.be_stepupsneaker.infrastructure.exception.ResourceNotFoundException;
import com.ndt.be_stepupsneaker.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClientOrderServiceImpl implements ClientOrderService {

    private final ClientOrderRepository clientOrderRepository;
    private final ClientOrderHistoryRepository clientOrderHistoryRepository;
    private final ClientCustomerRepository clientCustomerRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final AdminEmployeeRepository adminEmployeeRepository;
    private final ClientVoucherRepository clientVoucherRepository;
    private final ClientVoucherHistoryRepository clientVoucherHistoryRepository;
    private final PaginationUtil paginationUtil;
    private final ClientProductDetailRepository clientProductDetailRepository;
    private final ClientOrderDetailRepository clientOrderDetailRepository;
    private static final Logger logger = LoggerFactory.getLogger(ClientOrderServiceImpl.class);
    private final ClientPaymentMethodRepository clientPaymentMethodRepository;
    private final ClientPaymentRepository clientPaymentRepository;
    private final VNPayService vnPayService;


    @Autowired
    public ClientOrderServiceImpl(ClientOrderRepository clientOrderRepository,
                                  ClientOrderHistoryRepository clientOrderHistoryRepository,
                                  ClientCustomerRepository clientCustomerRepository,
                                  ClientAddressRepository clientAddressRepository,
                                  AdminEmployeeRepository adminEmployeeRepository,
                                  ClientVoucherRepository clientVoucherRepository,
                                  ClientVoucherHistoryRepository clientVoucherHistoryRepository,
                                  PaginationUtil paginationUtil,
                                  ClientProductDetailRepository clientProductDetailRepository,
                                  ClientOrderDetailRepository clientOrderDetailRepository, ClientPaymentMethodRepository clientPaymentMethodRepository, ClientPaymentRepository clientPaymentRepository, VNPayService vnPayService) {
        this.clientOrderRepository = clientOrderRepository;
        this.clientOrderHistoryRepository = clientOrderHistoryRepository;
        this.clientCustomerRepository = clientCustomerRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.adminEmployeeRepository = adminEmployeeRepository;
        this.clientVoucherRepository = clientVoucherRepository;
        this.clientVoucherHistoryRepository = clientVoucherHistoryRepository;
        this.paginationUtil = paginationUtil;
        this.clientProductDetailRepository = clientProductDetailRepository;
        this.clientOrderDetailRepository = clientOrderDetailRepository;
        this.clientPaymentMethodRepository = clientPaymentMethodRepository;
        this.clientPaymentRepository = clientPaymentRepository;
        this.vnPayService = vnPayService;
    }

    @Override
    public PageableObject<ClientOrderResponse> findAllEntity(ClientOrderRequest orderRequest) {
        return null;
    }

    @Override
    public Object create(ClientOrderRequest clientOrderRequest) {
        Order orderSave = ClientOrderMapper.INSTANCE.clientOrderRequestToOrder(clientOrderRequest);
        orderSave.setType(OrderType.ONLINE);
        orderSave.setStatus(OrderStatus.WAIT_FOR_CONFIRMATION);
        Address address = ClientAddressMapper.INSTANCE.clientAddressRequestToAddress(clientOrderRequest.getAddressShipping());
        if (clientOrderRequest.getAddressShipping().getCustomer() == null) {
            address.setCustomer(null);
        }
        // shipping
        float shippingFee = calculateShippingFee(clientOrderRequest.getAddressShipping());
        if (clientOrderRequest.getTransactionInfo() == null && clientOrderRequest.getPaymentMethod().equals("Card")) {
            float totalVnPay = totalVnPay(clientOrderRequest.getVoucher(), totalCartItem(clientOrderRequest.getCartItems()), shippingFee);
            String vnpayUrl = vnPayService.createOrder((int) totalVnPay, clientOrderRequest.getNote());
            return vnpayUrl;
        }
        if (clientOrderRequest.getTransactionInfo().getPaymentStatus() != 0) {
            throw new ApiException("Transaction failed!");
        }
        Address newAddress = clientAddressRepository.save(address);
        orderSave.setAddress(newAddress);
        orderSave.setShippingMoney(shippingFee);
        setOrderDetails(orderSave, clientOrderRequest);
        applyVoucherToOrder(orderSave, clientOrderRequest.getVoucher(), totalCartItem(clientOrderRequest.getCartItems()), orderSave.getShippingMoney());
        Order orderResult = clientOrderRepository.save(orderSave);
        createOrderDetails(orderResult, clientOrderRequest);
        clientOrderRepository.save(orderResult);
        createPayment(orderResult, clientOrderRequest);
        createVoucherHistory(orderResult);
        createOrderHistory(orderResult);
        return ClientOrderMapper.INSTANCE.orderToClientOrderResponse(orderResult);
    }

    @Override
    public ClientOrderResponse update(ClientOrderRequest orderRequest) {

        Optional<Order> orderOptional = clientOrderRepository.findById(orderRequest.getId());
        if (orderOptional.isEmpty()) {
            throw new ResourceNotFoundException("ORDER IS NOT EXIST");
        }
        Order newOrder = orderOptional.get();
        if (newOrder.getStatus() != OrderStatus.WAIT_FOR_DELIVERY && newOrder.getStatus() != OrderStatus.WAIT_FOR_CONFIRMATION) {
            throw new ApiException("Orders cannot be updated while the status is being shipped or completed !");
        }
        Address address = clientAddressRepository.findById(newOrder.getAddress().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address Not Found !"));
        if (address.getCustomer() == null) {
            address.setCustomer(null);
        }
        float shippingFee = calculateShippingFee(orderRequest.getAddressShipping());
        newOrder.setShippingMoney(shippingFee);
        newOrder.setType(OrderType.ONLINE);
        newOrder.setFullName(orderRequest.getFullName());
        newOrder.setPhoneNumber(orderRequest.getPhoneNumber());
        newOrder.setNote(orderRequest.getNote());
        setOrderDetails(newOrder, orderRequest);
        float totalOrderPrice = calculateTotalPriceOrderDetailOfOrder(newOrder.getOrderDetails());
        applyVoucherToOrder(newOrder, orderRequest.getVoucher(), totalOrderPrice, newOrder.getShippingMoney());
        Order order = clientOrderRepository.save(newOrder);
        Optional<OrderHistory> existingOrderHistoryOptional = clientOrderHistoryRepository.findByOrder_IdAndActionStatus(order.getId(), order.getStatus());
        if (existingOrderHistoryOptional.isEmpty()) {
            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setOrder(order);
            orderHistory.setActionStatus(order.getStatus());
            orderHistory.setNote(orderRequest.getOrderHistoryNote());
            orderHistory.setActionDescription(order.getStatus().action_description);
            clientOrderHistoryRepository.save(orderHistory);
        }
        return ClientOrderMapper.INSTANCE.orderToClientOrderResponse(order);
    }

    @Override
    public ClientOrderResponse findById(String id) {
        Optional<Order> orderOptional = clientOrderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new ResourceNotFoundException("ORDER IS NOT EXIST");
        }
        return ClientOrderMapper.INSTANCE.orderToClientOrderResponse(orderOptional.get());
    }

    @Override
    public Boolean delete(String id) {
        Optional<Order> orderOptional = clientOrderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new ResourceNotFoundException("ORDER IS NOT EXIST");
        }
        Order order = orderOptional.get();
        if (order.getStatus() == OrderStatus.PENDING) {
            clientOrderHistoryRepository.deleteAllByOrder(List.of(order.getId()));
            clientOrderRepository.delete(order);
        } else {
            order.setDeleted(true);
            clientOrderRepository.save(order);
            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setOrder(order);
            orderHistory.setNote(order.getNote());
            orderHistory.setActionDescription("Order is deleted");
            clientOrderHistoryRepository.save(orderHistory);
        }

        return true;
    }

    public float calculateTotalPriceOrderDetailOfOrder(List<OrderDetail> orderDetails) {
        if (orderDetails != null) {
            return orderDetails.stream()
                    .map(OrderDetail::getTotalPrice)
                    .reduce(0.0f, Float::sum);
        }
        return 0;
    }

    public float totalCartItem(List<ClientCartItemRequest> clientCartItemRequests) {
        float total = 0.0f;
        if (clientCartItemRequests != null) {
            for (ClientCartItemRequest request : clientCartItemRequests) {
                ProductDetail productDetail = clientProductDetailRepository.findById(request.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProductDetail Not Found"));
                float price = productDetail.getPrice();
                total += price * request.getQuantity();

            }

        }
        return total;
    }

    private void setOrderDetails(Order order, ClientOrderRequest orderRequest) {
        String customerId = orderRequest.getCustomer();
        String employeeId = orderRequest.getEmployee();
        order.setCustomer(customerId != null ? clientCustomerRepository.findById(customerId).orElse(null) : null);
        order.setEmployee(employeeId != null ? adminEmployeeRepository.findById(employeeId).orElse(null) : null);
    }

    private void applyVoucherToOrder(Order order, String voucherId, float totalOrderPrice, float shippingFee) {
        if (voucherId != null) {
            Voucher voucher = clientVoucherRepository.findById(voucherId).orElse(null);
            order.setVoucher(voucher);
            if (voucher != null) {
                float discount = voucher.getType() == VoucherType.CASH ? voucher.getValue() : (voucher.getValue() / 100) * totalOrderPrice;
                float finalTotalPrice = Math.max(0, totalOrderPrice - discount);
                order.setTotalMoney(finalTotalPrice + shippingFee);

            }
        } else {
            order.setTotalMoney(totalOrderPrice + shippingFee);
        }
    }

    private float totalVnPay(String voucherId, float totalCartItem, float shippingFee) {
        if (voucherId != null) {
            Voucher voucher = clientVoucherRepository.findById(voucherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher Not Found !"));
            float discount = voucher.getType() == VoucherType.CASH ? voucher.getValue() : (voucher.getValue() / 100) * totalCartItem;
            float finalTotalPrice = Math.max(0, totalCartItem - discount);
            return finalTotalPrice + shippingFee;
        }
        return totalCartItem + shippingFee;
    }

    public float calculateShippingFee(ClientAddressRequest addressRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", EntityProperties.VITE_GHN_USER_TOKEN);
        headers.set("shop_id", EntityProperties.VITE_GHN_SHOP_ID);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ClientShippingRequest shippingRequest = createShippingRequest(addressRequest);
        String apiUrl = EntityProperties.GHN_API_FEE_URL;
        HttpEntity<ClientShippingRequest> requestEntity = new HttpEntity<>(shippingRequest, headers);
        ResponseEntity<ClientShippingResponse> responseEntity;
        try {
            RestTemplate restTemplate = new RestTemplate();
            responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    ClientShippingResponse.class
            );
            if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                ClientShippingResponse response = responseEntity.getBody();
                if (response != null) {
                    ClientShippingDataResponse data = response.getData();
                    return data.getService_fee();
                } else {
                    System.out.println("API Success Response but Failed: " + response.getData());
                }
            } else {
                System.out.println("API Call Failed with Status Code: " + responseEntity.getStatusCode());
                System.out.println("API Response: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Client Error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            System.out.println("Server Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return 0.0f;
    }

    private ClientShippingRequest createShippingRequest(ClientAddressRequest addressRequest) {
        ClientShippingRequest shippingRequest = new ClientShippingRequest();
        shippingRequest.setFromDistrictId(1542);
        shippingRequest.setToDistrictId(Integer.parseInt(addressRequest.getDistrictId()));
        shippingRequest.setToWardCode(addressRequest.getWardCode());
        shippingRequest.setServiceId(53321);
        shippingRequest.setHeight(15);
        shippingRequest.setLength(15);
        shippingRequest.setWeight(500);
        shippingRequest.setWidth(15);
        return shippingRequest;
    }

    private Payment createPayment(Order order, ClientOrderRequest orderRequest) {
        PaymentMethod paymentMethod = clientPaymentMethodRepository.findByNameMethod(orderRequest.getPaymentMethod())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentMethod NOT FOUND !"));
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setTotalMoney(order.getTotalMoney());
        payment.setTransactionCode(orderRequest.getTransactionInfo().getTransactionCode() == null ? "CASH" : orderRequest.getTransactionInfo().getTransactionCode());
        payment.setDescription(order.getNote());
        return clientPaymentRepository.save(payment);
    }

    private VoucherHistory createVoucherHistory(Order order) {
        if (order.getVoucher() != null) {
            VoucherHistory voucherHistory = new VoucherHistory();
            float totalMoney = order.getTotalMoney();
            float voucherValue = order.getVoucher().getValue();
            float reduceMoney = order.getVoucher().getType() == VoucherType.CASH ? voucherValue : ((totalMoney * voucherValue) / 100);
            voucherHistory.setVoucher(order.getVoucher());
            voucherHistory.setOrder(order);
            voucherHistory.setMoneyReduction(reduceMoney);
            voucherHistory.setMoneyBeforeReduction(totalMoney);
            voucherHistory.setMoneyAfterReduction(reduceMoney);
            clientVoucherHistoryRepository.save(voucherHistory);
            return voucherHistory;
        }
        return null;
    }

    private OrderHistory createOrderHistory(Order order) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setOrder(order);
        orderHistory.setNote(order.getNote());
        orderHistory.setActionDescription(OrderStatus.WAIT_FOR_CONFIRMATION.action_description);
        clientOrderHistoryRepository.save(orderHistory);
        return orderHistory;
    }

    private List<OrderDetail> createOrderDetails(Order order, ClientOrderRequest clientOrderRequest) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ClientCartItemRequest clientCartItemRequest : clientOrderRequest.getCartItems()) {
            ProductDetail productDetail = clientProductDetailRepository.findById(clientCartItemRequest.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductDetail Not Found !"));
            if (productDetail != null) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setProductDetail(productDetail);
                orderDetail.setQuantity(clientCartItemRequest.getQuantity());
                orderDetail.setOrder(order);
                orderDetail.setPrice(productDetail.getPrice());
                orderDetail.setTotalPrice(orderDetail.getPrice() * orderDetail.getQuantity());
                orderDetails.add(orderDetail);
            }
        }
        return clientOrderDetailRepository.saveAll(orderDetails);
    }
}