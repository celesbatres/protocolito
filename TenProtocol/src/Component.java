public class Component {
    String value;
    String rol; // IN, OUT
    String function; 
    
    public Component(String value){
        this.value = value;
        this.rol = "OUT";
    } 

    public String getValue(){
        return this.value;
    }

    public String getRol(){
        return this.rol;
    }

    public String getFunction(){
        return this.function;
    }

    public void setValue(String value){
        this.value = value;
    }

    public void setRol(String rol){
        this.rol = rol;
    }

    public void setFunction(String function){
        this.function = function;
    }   
}