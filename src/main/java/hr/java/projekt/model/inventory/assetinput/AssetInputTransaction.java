/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.inventory.assetinput;

import hr.java.projekt.model.Entity;
import hr.java.projekt.model.articles.Article;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AssetInputTransaction extends Entity implements Serializable {
    private Article article;
    private BigDecimal quantity, discount, price;

    public AssetInputTransaction(Long id, Article article, BigDecimal quantity, BigDecimal discount, BigDecimal price) {
        super(id);
        this.article = article;
        this.quantity = quantity.setScale(4, RoundingMode.HALF_UP);
        this.discount = discount.setScale(4, RoundingMode.HALF_UP);
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public AssetInputTransaction(Article article, BigDecimal quantity, BigDecimal discount, BigDecimal price) {
        this(null, article, quantity, discount, price);
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getPrice() {
        return price;
    }
    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.ONE.subtract(discount.divide(new BigDecimal("100.0000")))).multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount.setScale(4, RoundingMode.HALF_UP);
    }
}
