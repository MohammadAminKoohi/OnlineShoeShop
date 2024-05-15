import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Commands {

    REGISTER("register:(?<id>\\S+):(?<name>\\S+):(?<money>\\S+)"),
    LOGIN("login:(?<id>\\S+)"),
    GET_PRICE("get price:(?<shoeName>\\S+)"),
    GET_QUANTITY("get quantity:(?<shoeName>\\S+)"),
    GET_MONEY("get money"),
    CHARGE("charge:(?<amount>\\S+)"),
    PURCHASE("purchase:(?<shoeName>\\S+):(?<amount>\\S+)"),
    LOGOUT("logout");

    private String regex;
    private Commands(String regex){
        this.regex = regex;
    }
    public Matcher getMatcher(String input){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        matcher.matches();
        return matcher;
    }
}
