package com.mes.core.testing

import com.mes.core.domain.Cart
import com.mes.core.domain.CartLine
import com.mes.core.domain.Product
import com.mes.core.domain.ProductCategory
import com.mes.core.domain.RentalPeriod
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.util.UUID

object TestData {
    fun testProduct(
        id: String = UUID.randomUUID().toString(),
        name: String = "Portable Ventilator",
        category: ProductCategory = ProductCategory.LIFE_SUPPORT,
        dailyRate: Long = 50000,
        merchantName: String = "MedTech Supplies"
    ) = Product(
        id = id,
        name = name,
        description = "Professional-grade portable ventilator for ICU and emergency use",
        category = category,
        merchantId = UUID.randomUUID().toString(),
        merchantName = merchantName,
        merchantIsVerified = true,
        dailyRateTzs = dailyRate,
        weeklyRateTzs = dailyRate * 6,
        monthlyRateTzs = dailyRate * 25,
        imageUrls = listOf("https://via.placeholder.com/400"),
        specs = mapOf(
            "Model" to "VentPro 3000",
            "Manufacturer" to "MedTech Corp",
            "Weight" to "5.2 kg",
            "Power" to "AC 100-240V"
        ),
        isFeatured = true,
        isAvailable = true
    )

    fun testRentalPeriod(): RentalPeriod {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return RentalPeriod(
            startDate = today,
            endDate = today.plus(7, DateTimeUnit.DAY)
        )
    }

    fun testCartLine(
        productId: String = UUID.randomUUID().toString(),
        dailyRate: Long = 50000
    ): CartLine {
        val period = testRentalPeriod()
        return CartLine(
            id = UUID.randomUUID().toString(),
            productId = productId,
            merchantId = UUID.randomUUID().toString(),
            merchantName = "MedTech Supplies",
            productName = "Portable Ventilator",
            thumbnailUrl = "https://via.placeholder.com/100",
            dailyRateTzs = dailyRate,
            rentalPeriod = period,
            quantity = 1,
            addedAt = Clock.System.now()
        )
    }

    fun testCart(): Cart = Cart(lines = listOf(testCartLine()))
}
