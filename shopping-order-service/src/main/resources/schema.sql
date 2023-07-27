--DROP TABLE IF EXISTS public.orders;

CREATE TABLE IF NOT EXISTS public.orders (
  id varchar(255) NOT NULL,
  product_id BIGINT NOT NULL,
  quantity BIGINT NOT NULL,
  order_date DATE, 
  status varchar(255),
  total_amount BIGINT NOT NULL,
  PRIMARY KEY(id)
);