/**
 *
 */
package shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author loinguyenx
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    private long productId;
    private long totalAmount;
    private long quantity;

}
