# orbitoo-toolkit: Advanced Guide (IN-PROGRESS)

The advanced guide contains several pattern, in order to show to implement:
* the service callback
* the application workflow

The concrete application can use these patterns and adapt them to their specific context.

## The Service Callback
Callbacks allows us to compose larger the activity from smaller activities. For example:
we can have the entity, which represents customer's `Order`. During processing of `Order` the customer
will need to perform payment, which is represented by `Payment` entity. Once the customer finishes `Payment`
we need to notify `Order`, which can perform transition to the next state.

First it is necessary to define DTO to store the service (callback) address:
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

Then we need to define `@ServicePoint` with one functional method:
```java
@ServicePoint("callbackServicePoint")
@FunctionalInterface
public interface CallbackHandler {
    public void process(@Subject Callback callback);
}
```

After that we can send the callback from our `PaymentService`:
```java
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    @ServicePointReference
    private CallbackHandler callbackHandler;

    @Async
    @Override
    public void createPayment(String paymentId, Callback callback) {
        // simulate the payment
        ...
        // send the callback
        callbackHandler.process(callback);
    }
}
```

Finally we can invoke `PaymentService` from `OrderService` and wait for the callback,
which will trigger the method annotated by `@SignalMapping`:
```java
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private PaymentService paymentService;

    @Override
    public void orderPayment(String orderId) {
        log.info("orderPayment started: " + orderId);
        Callback callback = new Callback(CallbackTarget.ORDER_SERVICE, orderId);
        paymentService.createPayment(orderId, callback);
    }

    @SignalMapping(servicePointName = "callbackServicePoint",
            servicePointClass = CallbackHandler.class,
            subjectClass = Callback.class,
            subjectTaggedValues = @TaggedValue(tag = "target", value = "ORDER_SERVICE"))
    public void acceptOrderPaymentCallback(Callback callback) {
        log.info("orderPayment finished: " + callback.getOrderId());
    }
}
```

Output:
```
orderPayment started: ORDER-2023-01-01-0001
orderPayment finished: ORDER-2023-01-01-0001
```
