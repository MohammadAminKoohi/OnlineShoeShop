import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreServer extends Thread {
    private static Map<String, Integer> inventory = new HashMap<>();
    private static Map<String, Customer> customers = new HashMap<>();
    private Socket socket;
    private Customer currentCustomer;
    private ServerSocket serverSocket = null;

    public StoreServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            currentCustomer = null;
            String input;
            while (true) {
                input = dataInputStream.readUTF();
                if (Commands.REGISTER.getMatcher(input).matches()) {
                    register(Commands.REGISTER.getMatcher(input), dataOutputStream);
                }
                else if(Commands.LOGIN.getMatcher(input).matches()){
                    login(Commands.LOGIN.getMatcher(input), dataOutputStream);
                }
                else if(Commands.LOGOUT.getMatcher(input).matches()){
                    logout(dataOutputStream);
                }
                else if(Commands.GET_PRICE.getMatcher(input).matches()){
                    getPrice(Commands.GET_PRICE.getMatcher(input).group("shoeName"), dataOutputStream);
                }
                else if(Commands.GET_QUANTITY.getMatcher(input).matches()){
                    getQuantity(Commands.GET_QUANTITY.getMatcher(input).group("shoeName"), dataOutputStream);
                }
                else if(Commands.GET_MONEY.getMatcher(input).matches()){
                    getCustomerMoney(dataOutputStream);
                }
                else if(Commands.CHARGE.getMatcher(input).matches()){
                    chargeCustomer(Commands.CHARGE.getMatcher(input).group("amount"), dataOutputStream);
                }
                else if(Commands.PURCHASE.getMatcher(input).matches()){
                    purchaseProduct(Commands.PURCHASE.getMatcher(input).group("shoeName"),Integer.parseInt(Commands.PURCHASE.getMatcher(input).group("amount")), dataOutputStream);
                }
                else{
                    dataOutputStream.writeUTF("invalid command");

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void logout(DataOutputStream dataOutputStream) throws IOException {
        if(currentCustomer == null){
            dataOutputStream.writeUTF("login first!");
        }
        else{
            currentCustomer = null;
            dataOutputStream.writeUTF("logged out successfully");
        }
    }
    public void login(Matcher matcher,DataOutputStream dataOutputStream) throws IOException{
        String id = matcher.group("id");
        if(isValidId(id)){
            currentCustomer = customers.get(id);
            dataOutputStream.writeUTF("logged in successfully");
        }
        else{
            dataOutputStream.writeUTF("no customer with this id");
        }
    }
    public void register(Matcher matcher, DataOutputStream dataOutputStream) throws IOException {
        String id = matcher.group("id");
        String name = matcher.group("name");
        String money = matcher.group("money");

        if (isValidId(id)) {
            dataOutputStream.writeUTF("a client with this id already exists!");
            return;
        }
        if (!isValidMoney(money)) {
            dataOutputStream.writeUTF("invalid money");
            return;
        }
        Customer customer = new Customer(name, id, Integer.parseInt(money));
        customers.put(id, customer);
        dataOutputStream.writeUTF("registered successfully");
    }

    private boolean isValidId(String id) {
        for (Map.Entry<String, Customer> entry : customers.entrySet()) {
            if (entry.getValue().getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidName(String name) {
        for (Map.Entry<String, Customer> entry : customers.entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidMoney(String moneyStr) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(moneyStr);
        return matcher.matches();
    }

    private boolean isValidProductName(String productName) {
        return inventory.containsKey(productName);
    }

    private boolean isValidQuantity(String quantityStr) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(quantityStr);
        return matcher.matches();
    }

    private void chargeCustomer(String chargeAmount, DataOutputStream dataOutputStream) throws IOException {
        int amount = Integer.parseInt(chargeAmount);
        if(currentCustomer == null){
            dataOutputStream.writeUTF("login first!");
        }
        else{
            currentCustomer.setMoney(currentCustomer.getMoney() + amount);
            dataOutputStream.writeUTF("charged successfully");
        }
    }

    private int getPrice(String productName, DataOutputStream dataOutputStream) throws IOException{
        if(isValidProductName(productName)){
            if(productName.equals("shoe1")){
                dataOutputStream.writeUTF("price of shoe1 is 1");
                return 1;
            }
            else if(productName.equals("shoe2")){
                dataOutputStream.writeUTF("price of shoe2 is 2");
                return 2;
            }
            else if(productName.equals("shoe3")){
                dataOutputStream.writeUTF("price of shoe3 is 3");
                return 3;
            }
            else{
                dataOutputStream.writeUTF("this item is not for sale!");
                return -1;
            }
        }
        else{
            dataOutputStream.writeUTF("invalid product name");
            return -1;
        }
    }

    private int getQuantity(String productName, DataOutputStream dataOutputStream) throws IOException{
        if(isValidProductName(productName)){
            dataOutputStream.writeUTF("quantity of "+productName +" is "+String.valueOf(inventory.get(productName)));
            return inventory.get(productName);
        }
        else{
            dataOutputStream.writeUTF("invalid product name");
            return -1;
        }
    }

    private void purchaseProduct(String productName, int quantity, DataOutputStream dataOutputStream) throws IOException {
        if(currentCustomer == null){
            dataOutputStream.writeUTF("login first!");
        }
        else if(!isValidProductName(productName)){
            dataOutputStream.writeUTF("invalid product name");
        }
        else if(!isValidQuantity(String.valueOf(quantity))){
            dataOutputStream.writeUTF("invalid quantity");
        }
        else if(inventory.get(productName) < quantity){
            dataOutputStream.writeUTF("not enough in stock");
        }
        else{
            int price = getPrice(productName, dataOutputStream);
            if(price == -1){
                return;
            }
            if(currentCustomer.getMoney() < price * quantity){
                dataOutputStream.writeUTF("not enough money");
            }
            else{
                currentCustomer.setMoney(currentCustomer.getMoney() - price * quantity);
                inventory.put(productName, inventory.get(productName) - quantity);
                dataOutputStream.writeUTF("purchased successfully");
            }
        }
    }

    private void getCustomerMoney(DataOutputStream dataOutputStream) throws IOException {
        if(currentCustomer == null){
            dataOutputStream.writeUTF("login first!");
        }
        else{
            dataOutputStream.writeUTF(String.valueOf(currentCustomer.getMoney()));
        }
    }

    public static void main(String[] args) {
        inventory.put("shoe1", 5);
        inventory.put("shoe2", 5);
        inventory.put("shoe3", 5);
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted");
                StoreServer storeServer = new StoreServer(socket);
                storeServer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class Customer {
    private String name;
    private String id;
    private int money;

    public Customer(String name, String id, int money) {
        this.name = name;
        this.id = id;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}