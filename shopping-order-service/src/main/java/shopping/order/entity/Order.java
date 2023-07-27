package shopping.order.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Id
	private String id;

	@Column(name = "product_id")
	private long productId;

	@Column(name = "quantity")
	private long quantity;

	@Column(name = "order_date")
	private LocalDateTime orderDate;

	@Column(name = "status")
	private OrderStatus orderStatus;

	@Column(name = "total_amount")
	private long amount;

}
