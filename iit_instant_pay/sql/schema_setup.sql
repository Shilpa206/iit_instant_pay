-- Accounts table 
DROP TABLE IF EXISTS IP_Payees; 
DROP TABLE IF EXISTS IP_Transactions;
DROP TABLE IF EXISTS IP_Accounts; 

CREATE TABLE IP_Accounts (
    AccountId int NOT NULL AUTO_INCREMENT,
    Name varchar(255) NOT NULL,
    Email varchar(255) NOT NULL,
    Password varchar(1000) NOT NULL,
    Balance float NOT NULL,
    PRIMARY KEY (AccountId),
    UNIQUE (Email),
    CHECK (Balance>0)
) AUTO_INCREMENT=10000;

-- Transactions 
CREATE TABLE IP_Transactions (
    TransactionId int NOT NULL AUTO_INCREMENT,
    FromAccountId int NOT NULL,
    ToAccountId int NOT NULL,
    TransactionTime datetime NOT NULL,
    Amount float NOT NULL,
    Remarks varchar(16000) NOT NULL,
    PRIMARY KEY (TransactionId),
    FOREIGN KEY (FromAccountId) REFERENCES IP_Accounts(AccountId),
    FOREIGN KEY (ToAccountId) REFERENCES IP_Accounts(AccountId)
) AUTO_INCREMENT=5000;

-- Payees 
CREATE TABLE IP_Payees (
    PayeeId int NOT NULL AUTO_INCREMENT,
    PayerAccountId int NOT NULL,
    PayeeAccountId int NOT NULL,
    Enabled boolean NOT NULL DEFAULT TRUE,
    PRIMARY KEY (PayeeId),
    FOREIGN KEY (PayerAccountId) REFERENCES IP_Accounts(AccountId),
    FOREIGN KEY (PayeeAccountId) REFERENCES IP_Accounts(AccountId),
    UNIQUE(PayerAccountId, PayeeAccountId)
) AUTO_INCREMENT=3000;


-- Stored Procesure 
DROP PROCEDURE IF EXISTS IP_Transfer; 

/*
Stored procedure to execute a money transfer.
Gives back a return code (-1 for failure and 0 for success), and a message. 
*/
DELIMITER $$
CREATE PROCEDURE IP_Transfer
(
    IN FromAccountId INT,
    IN ToAccountId INT, 
    IN Amount FLOAT, 
    IN Remarks VARCHAR(16000)
)
this_proc:BEGIN
	DECLARE currentPayerBalance INT;
    DECLARE currentPayeeBalance INT;
    DECLARE newPayerBalance INT;
    DECLARE newPayeeBalance INT;
    
    -- In case of an exception, rollback transaction
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
		BEGIN
			ROLLBACK;
            RESIGNAL;
		END;
	COMMIT;
    
    SET autocommit = OFF;  
    
    IF NOT EXISTS (SELECT 1 FROM IP_Accounts WHERE AccountId = FromAccountId)
    THEN 
		SELECT -1 ReturnCode, 'From account does not exist' AS Message;
        LEAVE this_proc;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM IP_Accounts WHERE AccountId = ToAccountId)
    THEN
		SELECT -1 ReturnCode, 'To account does not exist' AS Message;
        LEAVE this_proc;
    END IF;
    
    IF (FromAccountId = ToAccountId)
    THEN
		SELECT -1 ReturnCode, 'You cannot send money to yourself.' AS Message;
        LEAVE this_proc;
    END IF;
    
    SET currentPayerBalance = (SELECT Balance FROM IP_Accounts WHERE AccountId = FromAccountId);
    SET currentPayeeBalance = (SELECT Balance FROM IP_Accounts WHERE AccountId = ToAccountId);
    
    SET newPayerBalance = currentPayerBalance - Amount;
    SET newPayeeBalance = currentPayeeBalance + Amount;
    
    IF (newPayerBalance < 0)
    THEN 
		SELECT -1 ReturnCode, CONCAT('Not enough balance to transfer: $', Amount, '. Current Balance: $', currentPayerBalance) AS Message;
        LEAVE this_proc;
    END IF;
    
    /*
    Starting transaction.
    In order to make the data transfer atomic and consistent, we want to either run all DML statements below or none.
    Running some of them and skipping others (because of errors) will result in bad / corrupt data. 
    */
    START TRANSACTION;
    
    -- Deduct amout from payer account 
    UPDATE IP_Accounts SET Balance = newPayerBalance WHERE AccountId = FromAccountId;

	-- Deposit amout to payee account 
    UPDATE IP_Accounts SET Balance = newPayeeBalance WHERE AccountId = ToAccountId;
    
    -- Insert transcation 
    INSERT INTO IP_Transactions(FromAccountId, ToAccountId, Amount, TransactionTime, Remarks)
	VALUES (FromAccountId, ToAccountId, Amount, NOW(), Remarks);

	COMMIT;
    SELECT 0 ReturnCode, 'Transaction Successful!' AS Message;
END$$
DELIMITER ;


-- Start up data 
INSERT INTO IP_Accounts(AccountId, Name, Email, Password, Balance)
VALUES 
    (999999, 'Administrator', 'admin', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 999999.0);




-- Commit
COMMIT;

