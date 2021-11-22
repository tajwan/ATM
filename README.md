#ATM simulator

## Endpoints

There are just two endpoints. 

Get: localhost:8080/atm/check-balance
Post: localhost:8080/atm/withdraw-money that takes integer as a payload

Both endpoints need two headers: pin and account-number. If pin and/or account number are incorrect exception is thrown.
Other cases when exception is thrown: 
- ATM does not have many to
- User does not have sufficient funds
- ATM cannot execute operation
- User wants to withdraw amount below 5

Get method returns BalanceDetails object with information about account balance, overdraft and total available funds.
Post method returns OperationsDetails object with information how much money was withdrawn and a map where keys
are notes and values are amount of notes that were withdrawn