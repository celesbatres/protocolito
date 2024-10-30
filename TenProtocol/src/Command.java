public class Command {
    String component;
    String action;
    String value; // value - component

    public Command(String component, String action, String value){
        this.component = component;
        this.action = action;
        this.value = value;
    }

    public String getAction(){
        return this.action;
    }

    public String getValue(){
        return this.value;
    }

    public String getComponent(){
        return this.component;
    }
}
