package com.example.demo.domain;

import jakarta.validation.constraints.*;

import java.util.Objects;

public class CollectionItem {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Max(100000000)
    @Min(0)
    private Integer price;

    public Boolean sameId(Long id){
        return this.id.equals(id);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CollectionItem collectionItem = (CollectionItem) o;
        return Objects.equals(id, collectionItem.id);
    }

    public CollectionItem(Long id, String name, Integer price) {
        this.id = id;
        this.name = name == null ? null : name.strip();
        this.price = price;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }
}
