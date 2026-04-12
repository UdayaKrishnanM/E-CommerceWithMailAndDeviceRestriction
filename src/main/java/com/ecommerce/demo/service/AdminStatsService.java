package com.ecommerce.demo.service;

import com.ecommerce.demo.dto.AdminStatsDTO;
import com.ecommerce.demo.model.Order;
import com.ecommerce.demo.model.OrderItem;
import com.ecommerce.demo.model.Product;
import com.ecommerce.demo.model.ProductSize;
import com.ecommerce.demo.repository.OrderRepository;
import com.ecommerce.demo.repository.ProductRepository;
import com.ecommerce.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminStatsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int LOW_STOCK_THRESHOLD = 5;

    public AdminStatsDTO getDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        double totalRevenue = allOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Top 5 products by quantity sold
        Map<String, Long> soldMap = new LinkedHashMap<>();
        for (Order order : allOrders) {
            for (OrderItem item : order.getOrderItems()) {
                String name = item.getProduct().getName();
                soldMap.merge(name, (long) item.getQuantity(), Long::sum);
            }
        }
        List<AdminStatsDTO.TopProductDTO> topProducts = soldMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new AdminStatsDTO.TopProductDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // Low stock items
        List<AdminStatsDTO.LowStockDTO> lowStockItems = new ArrayList<>();
        for (Product product : allProducts) {
            for (ProductSize size : product.getSizes()) {
                if (size.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                    lowStockItems.add(new AdminStatsDTO.LowStockDTO(
                            product.getName(),
                            size.getSize().toString(),
                            size.getStockQuantity()
                    ));
                }
            }
        }

        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalRevenue(totalRevenue);
        stats.setTotalOrders(allOrders.size());
        stats.setTotalUsers(userRepository.count());
        stats.setTotalProducts(allProducts.size());
        stats.setTopProducts(topProducts);
        stats.setLowStockItems(lowStockItems);
        return stats;
    }
}
