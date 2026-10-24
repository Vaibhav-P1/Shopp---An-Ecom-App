package com.example.shopp.domain

import com.example.shopp.model.ProductModel

/** Percentage off the list price, rounded to the nearest whole number; null when there is no real discount. */
val ProductModel.discountPercent: Int?
    get() {
        val list = Money.parsePaise(price) ?: return null
        val selling = Money.parsePaise(actualPrice) ?: return null
        if (list <= 0 || selling >= list) return null
        return (((list - selling) * 100 + list / 2) / list).toInt()
    }
