package shopping.order.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shopping.order.entity.OrderStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderUpdateDto {
  private Long productId;
  private Long quantity;
  private LocalDateTime orderDate;
  private OrderStatus orderStatus;
}
