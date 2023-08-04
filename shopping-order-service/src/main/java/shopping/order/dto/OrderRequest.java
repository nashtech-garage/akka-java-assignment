/**
 *
 */
package shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author loinguyenx
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private String orderId;
    private long productId;
    private long totalAmount;
    private long quantity;
    private LocalDateTime orderDate;
}
