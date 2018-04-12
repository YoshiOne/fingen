DROP TABLE IF EXISTS temp_Income_Transactions;
CREATE TEMP TABLE temp_Income_Transactions AS
SELECT _id, SrcAccount, DateTime, Amount
FROM log_Transactions
WHERE Deleted = 0 AND (Amount > 0)
UNION ALL
SELECT _id, DestAccount, DateTime, Amount*ExchangeRate*-1
FROM log_Transactions
WHERE Deleted = 0 AND (DestAccount >= 0);
CREATE INDEX [idx_temp_1] ON [temp_Income_Transactions] (SrcAccount, DateTime);

DROP TABLE IF EXISTS temp_Expense_Transactions;
CREATE TEMP TABLE temp_Expense_Transactions AS
SELECT _id, SrcAccount, DateTime, Amount
FROM log_Transactions
WHERE Deleted = 0 AND (Amount < 0);
CREATE INDEX [idx_temp_2] ON [temp_Expense_Transactions] (SrcAccount, DateTime);

DROP TABLE IF EXISTS temp_All_Transactions;
CREATE TEMP TABLE temp_All_Transactions AS
SELECT SrcAccount, _id, DateTime FROM temp_Income_Transactions UNION ALL SELECT SrcAccount, _id, DateTime FROM temp_Expense_Transactions;

DELETE FROM log_Running_Balance;
INSERT OR REPLACE INTO log_Running_Balance (AccountID, TransactionID, DateTimeRB, Income, Expense)
SELECT SrcAccount, _id, DateTime,
  IFNULL((SELECT SUM(Amount) FROM temp_Income_Transactions WHERE (SrcAccount = lt.SrcAccount) AND DateTime <= lt.DateTime), 0),
  IFNULL((SELECT SUM(Amount) FROM temp_Expense_Transactions WHERE (SrcAccount = lt.SrcAccount) AND DateTime <= lt.DateTime), 0)
FROM temp_All_Transactions lt;