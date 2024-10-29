import java.util.*;

public class TenProtocol {
    ArrayList<Command> commands = new ArrayList<>();
    ArrayList<String> commandsString = new ArrayList<>();
    String data;
    String header;
    // 

    public TenProtocol(String direction){
        this.header = direction;
        this.data = "";
        this.commandsString = new ArrayList<>();
    }

    public String buildPacket(){
        // Packet = header + "-" + data //ip:vd
        // return header + "-" + data;
        for(String command : commandsString){
            this.data += command + " ";
        }
        return this.header + "-" + this.data;
    }

    public boolean hasCommands(){
        return commandsString.size() > 0;
    }


    //****************************************IMPORTANTE********************************************************* */
    // El ten protocol solamente sera la DATA que se envia en los paquetes, aparte son los headers que se agregan en el driver

    // Aquí es donde se interpretan los paquetes de los otros protocolos y se convierte en VD
    // , también se puede interpretar el VD y Formar un Paquete
    //y convertirlo en otros protocolos dependiendo de
    
    /*
     * 
     * 1. Recibir un VD y convertirlo en un paquete de TenProtocol > El ten protocol solamente sera la DATA que se envia en los paquetes, aparte son los headers que se agregan en el driver
     * 2. Recibir un paquete de Otro Protocolo y convertirlo en un VD
     */

    //  Cuando se esté creando un VD, se debe colocar la configuración actual de los componentes - getLastVD() -> En App.java

    // A

}
