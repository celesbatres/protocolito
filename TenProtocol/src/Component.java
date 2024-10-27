public class Component {
    // Clase abstracta que representa un componente
    String type;
    String value;
    String state; // IN, OUT
    String function; // Cual componente va a cambiar
    Component component;

    public Component(String type, String value){
        this.type = type;
        this.value = value;
        this.state = "IN";
        this.function = "";
    }

    public boolean equals(Component component){
        return this.type.equals(component.type) && this.value.equals(component.value);

        //IDEA: Comparar los atributos de los componentes (Este va a servir si en caso se cambie el estado de un componente)
    }
}
