/**
 *
 */
package shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shopping.order.entity.OrderStatus;

/**
 * @author loinguyenx
 *
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String      orderId;
    private OrderStatus orderStatus;
}
