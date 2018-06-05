package com.yoshione.fingen.screenwidget.transactions

import com.yoshione.fingen.model.Transaction

/**
 * Takes 2 sms n, n-1 and calculates balance difference
 *
 * 1. User has bank card currency A
 * 2. User pays in currency B
 * 3. Money blocked in currency B (amount A)
 * 4. Exchange rate A to B has been changed
 * 5. Blocked amount has written-offs after some time but it is different amount A with step 3
 * 6. As result one more income or outcome bank operation
 */
object RecountedMapper {

    fun map(transactions: List<Transaction>): List<DataItem> {
        val size = transactions.size
        val dataItems = mutableListOf<DataItem>()
        for (i in 0 until size - 1) {
            val previous = transactions[i + 1].amount
            val current = transactions[i].amount
            val difference = current - previous
            dataItems.add(DataItem(
                    transactions[i].accountID.toString(),
                    transactions[i].dateTime,
                    difference.toDouble(),
                    current.toDouble(),
                    transactions[i].dateTime
            ))
        }
        return dataItems
    }
}