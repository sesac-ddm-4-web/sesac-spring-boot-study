package com.example.sesac_spring_boot_study.domain;

public class CartItem {
    private Long rowId;
    private Long productId;
    private String name;
    private Long price;
    private Integer stock;
    private int quantity;

    public CartItem() {}

    public CartItem(Long rowId, Long productId, String name, Long price, Integer stock, int quantity){
        this.rowId=rowId;
        this.productId=productId;
        this.name=name;
        this.price=price;
        this.stock=stock;
        this.quantity=quantity;
    }

    public Long getRowId() {
        return rowId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public int getQuantity() {
        return quantity;
    }

    public long total(){
        return price*quantity;
    }


    public void setRowId(Long rowId) {
        this.rowId = rowId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
