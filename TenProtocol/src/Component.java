public class Component {
    // Clase abstracta que representa un componente
    String value;
    String rol; // IN, OUT
    String function; // 0: Switch, 1: Led, 2: Slider, 3: PickColor <- los que se pueden modificar

    public void setValue(String value){
        this.value = value;
    }

    public void setRol(String rol){
        this.rol = rol;
    }

    public void setFunction(String function){
        this.function = function;
    }

    public Component(String value){
        this.value = value;
        this.rol = "OUT";
    }

    // public boolean equals(Component component){
    //     return this.name.equals(component.name) && this.value.equals(component.value);

    //     //IDEA: Comparar los atributos de los componentes (Este va a servir si en caso se cambie el estado de un componente)
    // }

    public void applyCommand(Command command){
        if(command.action.equals("cmd")){
            this.function = command.value;
        }else if(command.action.equals("msg")){
            this.value = command.value;
        }
    }
    
}

// FUNCIONES DE ACTIONS:
// cmd: Cambiar el valor de un componente
// msg: Mensaje completo

// Component
// name -> 

// luego hacer un linkedlist de componentes (que estén en orden)-+ -> Agregar componente space