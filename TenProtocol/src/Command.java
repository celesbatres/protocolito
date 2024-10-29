public class Command {
    String component;
    String action;
    String value; // ya sea value de comp o su fun

    public Command(String component, String action, String value){
        this.component = component;
        this.action = action;
        this.value = value;
    }

    public Component getComponent(){
        Component component = new Component("");
        if(this.action.equals("cmd")){
            // component.set = this.action;
            return component;
        }
        return null;
    }
}
