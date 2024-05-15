# OnlineShoeShop
Sut advanced programming problem 2 of Network and multithreading assignment.
A client-server shoe store simulation implemented using sockets and multithreading, allowing multiple clients to perform operations such as registering, logging in, getting shoe prices and quantities, charging accounts, and making purchases.

## Commands

- **Register**: `register:id:name:money` - Register a new client with ID, name, and initial balance.
- **Login**: `login:id` - Log in with the specified ID.
- **Logout**: `logout` - Log out the current client.
- **Price Inquiry**: `get price:shoe_name` - Retrieve the price of a shoe.
- **Stock Check**: `get quantity:shoe_name` - Check the remaining stock of a shoe.
- **Account Balance**: `get money` - Display the current client's balance.
- **Account Charging**: `charge:money` - Add funds to the client's account.
- **Purchase**: `purchase:shoe_name:quantity` - Buy a quantity of a shoe.
