package streams;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionalStream {

    OrderRepository orderRepository = OrderRepository.builder().build();

    public static void main(String[] args) {

    }

    /**
     * Snippet                                  - Type
     * Optional.ofNullable(orderId)             - Optional<Long>
     * stream()                                 - Stream<Long>
     * map(orderRepository::findByOrderId)      - Stream<List<OrderLine>>
     * flatMap(Collection::stream)              - Stream<OrderLine>
     * map(OrderLine::getPrice)                 - Stream<BigDecimal>
     * reduce(BigDecimal.ZERO, BigDecimal::add) - BigDecimal
     */
    public BigDecimal getOrderPrice(Long orderId) {
        return Optional.ofNullable(orderId)
                .stream()
                .map(orderRepository::findByOrderId)
                .flatMap(Collection::stream)
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 1. Wrap the orderId in an Optional
     * 2. Find relevant order lines
     * 3. Use flatMap() to get an Optional<BigDecimal>; map() would get an Optional<Optional<BigDecimal>>
     * 4. We need to wrap the result into an Optional to conform to the method signature
     * 5. If the Optional doesn’t contain a value, the sum is 0
     */
    public BigDecimal getOrderPrice_optional(Long orderId) {
        return Optional.ofNullable(orderId)
                .map(orderRepository::findByOrderId)
                .flatMap(lines -> {
                    BigDecimal sum = lines.stream()
                            .map(OrderLine::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return Optional.of(sum);
                }).orElse(BigDecimal.ZERO);
    }

    /**
     * adding a null check to make sure order-id is present
     */
    public BigDecimal getOrderPrice_nullCheck(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        List<OrderLine> lines = orderRepository.findByOrderId(orderId);
        return lines.stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Using streams to achieve the same as *_initial
     */
    public BigDecimal getOrderPrice_stream(Long orderId) {
        List<OrderLine> lines = orderRepository.findByOrderId(orderId);
        return lines.stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Provide an accumulator variable for the price
     * Add each line’s price to the total price
     */
    public BigDecimal getOrderPrice_initial(Long orderId) {
        List<OrderLine> lines = orderRepository.findByOrderId(orderId);
        BigDecimal price = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            price = price.add(line.getPrice());
        }
        return price;
    }

}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class OrderLine {
    Long orderId;
    Long orderItemId;
    BigDecimal price;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class OrderRepository {
    List<OrderLine> findByOrderId(Long orderId) {
        return Stream.of(
                        OrderLine.builder().orderId(1L).orderItemId(1L).price(BigDecimal.valueOf(10)).build(),
                        OrderLine.builder().orderId(1L).orderItemId(2L).price(BigDecimal.valueOf(20)).build(),
                        OrderLine.builder().orderId(1L).orderItemId(3L).price(BigDecimal.valueOf(30)).build())
                .filter(line -> line.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }
}