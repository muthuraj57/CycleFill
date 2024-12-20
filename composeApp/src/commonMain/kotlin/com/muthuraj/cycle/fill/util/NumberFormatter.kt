/* $Id$ */
package com.muthuraj.cycle.fill.util

/**
 * Created by Muthuraj on 20/12/24.
 */
fun formatToIndianRupee(amount: Long): String {
    val amountStr = amount.toString()
    val n = amountStr.length

    if (n <= 3) return "₹$amountStr"

    val lastThree = amountStr.takeLast(3)
    val remaining = amountStr.dropLast(3)

    val formattedRemaining = remaining.reversed().chunked(2).joinToString(",").reversed()

    return "₹$formattedRemaining,$lastThree"
}