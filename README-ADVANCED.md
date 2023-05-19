# orbitoo-toolkit: Advanced Guide (IN-PROGRESS)

The advanced guide contains several patterns, in order to show how to implement:
* the service callback
* the application workflow

The concrete application can adapt these patterns to its specific context.

## The Service Callback
Callbacks allows us to compose the larger activity from smaller activities. For example we can have the entity,
which represents customer's `Order`. During processing of `Order` we will need to perform the payment,
which is represented by `Payment` entity. Once the customer finishes `Payment` we need to notify `Order`,
which will continue with its processing.

First it is necessary to define an entity for the service reference:
```java
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ServiceRef {
    @Tag(name = "value")
    private String value = null;
}
```

Then we need to define callback `@ServicePoint` with a service reference as `@Subject`:
```java
@ServicePoint("paymentCallback")
public interface PaymentCallback {
    public void paymentExecuted(String paymentId, @Subject ServiceRef serviceRef);
}
```

After that we should be able to send the callback from our `PaymentService`:
```java
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    @ServicePointReference
    private PaymentCallback paymentCallback;

    @Async
    @Override
    public void executePayment(String paymentId, BigDecimal amount, ServiceRef callbackRef) {
        ...
        // send the callback
        paymentCallback.paymentExecuted(paymentId, callbackRef);
    }
}
```

Finally we can invoke `PaymentService` from `OrderService` and process the callback.
This will be done in three steps:
* first we will choose an unique service reference
* then we will invoke `PaymentService` with the chosen service reference
* finally we will specify callback `@Bean` and we will bind it to the chosen service reference
```java
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private static final String ORDER_PAYMENT_CALLBACK = "OrderServiceImpl#PaymentCallback";

    @Autowired
    private PaymentService paymentService;

    @Override
    public void orderPayment(String orderId) {
        log.info("orderPayment started: " + orderId);
        paymentService.executePayment(orderId, new BigDecimal("4999.00"), new ServiceRef(ORDER_PAYMENT_CALLBACK));
    }

    @Bean
    @DomainService(servicePointName = "paymentCallback", subjectClass = ServiceRef.class, //
            subjectTaggedValues = @TaggedValue(tag = "value", value = ORDER_PAYMENT_CALLBACK))
    public PaymentCallback getOrderPaymentCallback() {
        return (paymentId, serviceRef) -> log.info("orderPayment finished: " + paymentId);
    }
}
```

Output:
```
orderPayment started: ORDER-2023-01-01-0001
orderPayment finished: ORDER-2023-01-01-0001
```
