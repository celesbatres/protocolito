import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TenProtocol {
    ArrayList<Command> commands = new ArrayList<>();
    String data;
    String header;

    public TenProtocol(String direction){
        this.header = direction;
        this.data = "";
    }


    public TenProtocol(){
        this.data = "";
        this.header = "";
    }

    // Build the packet to send to the driver
    public String buildPacket(){
        // Packet = header + "-" + data //ip:vd
        // return header + "-" + data;
        for(Command command : commands){
            // String commandString = command.toString();
            this.data += command.toString() + " ";
        }
        return this.header + "|" + this.data;
    }

    public boolean hasCommands(){
        return commands.size() > 0;
    }

    // public TenProtocol(String protocol, String data){
        // Armar un arraylist de comandos en formato string dependiendo del protocolo(por cada grupo) > 
        // Mejor en Command
        // Definir separator de comandos
           
        // this.header = protocol;
        // this.data = data;
        // if(protocol.equals("A")){
        //     // Separar los comandos por espacios
        //     String[] commands = data.split(" ");
        //     for(String command : commands){
        //         String[] commandParts = command.split(":");
        //         String component = commandParts[0];
        //         String value = commandParts[1];
        //         Command command = new Command(value);
        //         this.commandsString.add(command.toString());
        //     }
        // }
        // Command command = new Command(protocol, data);
    // }

    public static void interpretTenProtocol(String packet){
        String input = packet; // "241:1 lred:1 lcd:1 msg:’Hola Grupo F1’";
        ArrayList<String> tokens = new ArrayList<>();

        Pattern pattern = Pattern.compile("msg:’.*?’|\\S+");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        for (String token : tokens) {
            System.out.println(token);
            String component = token.split(":")[0];
            String value = token.split(":")[1];
            //Traducción de Protocolo
        }
    }

    public static void interpretOtherProtocol(String packet){
        String[] commands = packet.split("-");
        for(String command : commands){
            String[] commandParts = command.split(":");
            String component = commandParts[0];
            String value = commandParts[1];
            //Traducción de Protocolo
        }
        if(packet.startsWith("F1")){
            // F1 Protocol

        }
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
