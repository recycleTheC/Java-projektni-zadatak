/*
 * Copyright (c) 2023. Mario Kopjar, Zagreb University of Applied Sciences
 */

package hr.java.projekt.model.articles;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ArticleStock {
    private Article article;
    private BigDecimal stock;
    private BigDecimal averagePurchasePrice;

    public ArticleStock(Article article, BigDecimal stock) {
        this.article = article;
        this.stock = stock.setScale(4, RoundingMode.HALF_UP);
    }

    public ArticleStock(Article article, BigDecimal stock, BigDecimal averagePurchasePrice) {
        this(article, stock);
        this.averagePurchasePrice = averagePurchasePrice.setScale(2, RoundingMode.HALF_UP);
    }

    public Article getArticle() {
        return article;
    }

    public BigDecimal getStock() {
        return stock;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }
}
