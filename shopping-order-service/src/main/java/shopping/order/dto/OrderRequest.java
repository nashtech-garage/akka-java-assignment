/**
 *
 */
package shopping.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author loinguyenx
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OrderRequest {

    private long productId;
    private long totalAmount;
    private long quantity;

}
