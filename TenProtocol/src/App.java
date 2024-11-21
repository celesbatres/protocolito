import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.json.JSONException;

public class App {
    // public static String receivedPacket = "";
    public static HashMap<String, Protocol> protocols = new HashMap<>();
    private static final Map<String, ConcurrentLinkedQueue<String>> clientMessages = new ConcurrentHashMap<>();
    private static final List<String> connectedClients = Collections.synchronizedList(new ArrayList<>());
    private static final ArrayList<String> msg = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        // Build protocols
        buildProtocols();
        // Create web server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/post", new PostHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor escuchando en http://localhost:8000/post");

        // Receive messages from driver
        Thread recibo = new Thread(() -> recibodedriver());
        recibo.start();

    }

    public static void recibodedriver() {
        try (DatagramSocket socket = new DatagramSocket(8001)) {
            byte[] buffer = new byte[1024];
            while (true) {
                System.out.println("Esperando datos de driver...");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Driver -> App " + message);

                // Parsear el mensaje: formato "ID-protocolo-datos"
                String[] tokens = message.split("\\|");
                String protocol = tokens[0];
                System.out.println("Protocol: " + protocol);
                int virtualDevice = Integer.parseInt(tokens[1]); // ID del dispositivo virtual
                System.out.println("Virtual Device: " + virtualDevice);
                String data = protocol + "|" + tokens[2];
                msg.add(data);
                // synchronized (connectedClients) {
                // System.out.println("Clientes conectados:");
                // for (int i = 0; i < connectedClients.size(); i++) {
                // System.out.println("VD " + i + ": Cliente " + connectedClients.get(i));
                // if (i == virtualDevice) {
                // clientMessages.computeIfAbsent(connectedClients.get(i), k -> new
                // ConcurrentLinkedQueue<>())
                // .offer(data);
                // System.out.println("Mensaje almacenado para VD " + virtualDevice +
                // " (Cliente: " + connectedClients.get(i) + ")");
                // }
                // }
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendToDriver(String message) {
        try {
            int port = 9091;
            DatagramSocket ds = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();
            byte buffer[] = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            ds.send(packet);
            ds.close();
        } catch (Exception e) {
            System.out.println("Alerta! Errores encontrados durante ejecución");
            e.printStackTrace();
        }
    }

    public static List<String> getConnectedClients() {
        synchronized (connectedClients) {
            return new ArrayList<>(connectedClients);
        }
    }

    public static TenProtocol toTenProtocol(Protocol protocol, String data) {
        String[] commands = data.split(protocol.commandDelimiter);// <- Delimitador de comandos
        TenProtocol tp = new TenProtocol();
        for (String command : commands) {
            String[] parts = command.split(protocol.commandSeparator);
            String component = parts[0];
            String value = parts[1];
            if (protocol.commandsRegex.containsKey(component)) {
                if (value.matches(protocol.commandsRegex.get(component))) {
                    tp.commands.add(new Command(component, "msg", value));
                }
            }
        }
        return tp;
    }

    public static void buildProtocols() {

        // 1
        HashMap<String, String> commandsMap = new HashMap<>() {
            {
                put("LCD", "lcd");
                put("SW0", "switch0");
                put("SW1", "switch1");
                put("FAN", "fan");
                put("LRGB", "lrgb");
                put("LRED", "lred");
                put("LGRE", "lgreen");
                put("HEAT", "heat");
                put("SPD", "speed");
                put("SLIDER0", "slider0");
                put("SLIDER1", "slider1");
                put("SLIDER2", "slider2");
                put("LCOLOR", "lrgb_color");
                put("COLOR", "pick_color");
                put("MSG", "msg");
            }
        };
        HashMap<String, String> commandsRegex = new HashMap<>() {
            {
                put("LCD", "^[0-1]{1}$");
                put("SW0", "^[0-1]{1}$");
                put("SW1", "^[0-1]{1}$");
                put("FAN", "^[0-1]{1}$");
                put("LRGB", "^[0-1]{1}$");
                put("LRED", "^[0-1]{1}$");
                put("LGRE", "^[0-1]{1}$");
                put("HEAT", "^[0-1]{1}$");
                put("SPD", "^[0-9A-Fa-f]$");
                put("SLIDER0", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("SLIDER1", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("SLIDER2", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("LCOLOR", "^([A-Fa-f0-9]{6})$");
                put("COLOR", "^([A-Fa-f0-9]{6})$");
                put("MSG", "^.+$");
            }
        };
        App.protocols.put("1", new Protocol(commandsMap, commandsRegex, "(MSG:.*)|([^\\\\\\\\s]+)", ":"));

        commandsMap = new HashMap<>() {
            {
                put("0", "lcd");
                put("1", "switch0");
                put("2", "switch1");
                put("3", "fan");
                put("5", "lrgb");
                put("7", "lred");
                put("8", "lgreen");
                put("9", "heat");
                put("4", "speed");
                put("A", "slider0");
                put("B", "slider1");
                put("C", "slider2");
                put("6", "lrgb_color");
                put("D", "pick_color");
                put("EE", "msg");
            }
        };

        // check commandsregex of this protocol
        commandsRegex = new HashMap<>() {
            {
                put("0", "^[0-1]{1}$");
                put("1", "^[0-1]{1}$");
                put("2", "^[0-1]{1}$");
                put("3", "^[0-1]{1}$");
                put("4", "^[0-1]{1}$");
                put("5", "^[0-1]{1}$");
                put("6", "^[0-1]{1}$");
                put("7", "^[0-1]{1}$");
                put("8", "^(1[0-5]|[0-9])$");
                put("A", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("B", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("C", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("D", "^([A-Fa-f0-9]{6})$");
                put("E", "^([A-Fa-f0-9]{6})$");
                put("F", "^[A-Za-z0-9_]+$");
            }
        };
        App.protocols.put("2", new Protocol(commandsMap, commandsRegex, "", ""));

        commandsMap = new HashMap<>() {
            {
                put("0", "heat");
                put("1", "lred");
                put("2", "lgreen");
                put("3", "fan");
                put("4", "lrgb");
                put("5", "speed");
                put("6", "lrgb_color");
                put("7", "lcd");
                put("8", "msg");
            }
        };
        App.protocols.put("8", new Protocol(commandsMap, commandsRegex, "", ""));

        commandsMap = new HashMap<>() {
            {
                put("HET", "heat");
                put("LDR", "lred");
                put("LDG", "lgreen");
                put("FAN", "fan");
                put("RLD", "lrgb");
                put("SPD", "speed");
                put("LRGB", "lrgb_color");
                put("LCD", "lcd");
                put("MSG", "msg");
            }
        };
        App.protocols.put("11", new Protocol(commandsMap, commandsRegex, "", ""));        
    }

    static class PostHandler implements HttpHandler {
        private static final LinkedList<String> vdPackets = new LinkedList<>();
        private static final LinkedList<String> tpPackets = new LinkedList<>();
        LinkedList<VirtualDevice> vds = new LinkedList<>();
        HashMap<String, String> commandsMap = new HashMap<>() {
            {
                put("lcd", "^[0-1]{1}$");
                put("switch0", "^[0-1]{1}$");
                put("switch1", "^[0-1]{1}$");
                put("fan", "^[0-1]{1}$");
                put("lrgb", "^[0-1]{1}$");
                put("lred", "^[0-1]{1}$");
                put("lgreen", "^[0-1]{1}$");
                put("heat", "^[0-1]{1}$");
                put("speed", "^(1[0-5]|[0-9])$");
                put("slider0", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("slider1", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("slider2", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("lrgb_color", "^([A-Fa-f0-9]{6})$");
                put("pick_color", "^([A-Fa-f0-9]{6})$");
                put("msg", "^'.*'$");
            }
        };

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Agregar encabezados CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            String clientAddress = exchange.getRemoteAddress().toString();
            // connectedClients.add(clientAddress);
            if (!connectedClients.contains(clientAddress)) {
                connectedClients.add(clientAddress);
            }

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                // Lee la solicitud
                InputStream inputStream = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println(line);
                    body.append(line);
                }

                String response = body.toString();

                if (vdPackets.isEmpty()) {
                    vdPackets.add("00F000000000000000000000");
                }

                System.out.println("Ultimo VD: " + vdPackets.getLast());
                VirtualDevice vdState = new VirtualDevice(vdPackets.getLast());

                // 32 caracteres ascii = 64 caracteres hexadecimales - 24+64 = 88 caracteres
                String message;
                if (response.length() >= 88) {
                    message = response.substring(0, 88);
                } else {
                    message = response;
                }
                String commandLineHex = message.substring(24);

                String commandLine = getMessage(commandLineHex);
                // System.out.println("Command Line: " + commandLine);

                if (isCommandLine(commandLine) && !message.equals(vdState.buildVD())) {
                    vdPackets.add(message); // <= ? solo está agregando el mensaje en la linea de comandos

                    ArrayList<String> tokens = new ArrayList<>();
                    Pattern pattern = Pattern.compile("msg:'.*?'|\\S+");
                    Matcher matcher = pattern.matcher(commandLine);
                    while (matcher.find()) {
                        tokens.add(matcher.group());
                    }
                    String[] commands = tokens.toArray(new String[0]);
                    for (int i = 0; i < commands.length; i++) {
                        System.out.println("Comando: " + commands[i]);
                    }
                    String action = commands[0];

                    if (action.equals("msg")) {
                        String direction = commands[1].split(":")[0];
                        String vd = commands[1].split(":")[1];
                        String header = direction + "|" + vd;

                        TenProtocol tp = new TenProtocol(header);

                        // Commands
                        for (int i = 2; i < commands.length - 1; i++) {
                            String command = commands[i];
                            String[] parts = command.split(":");
                            String component = parts[0];
                            String value = parts[1];
                            System.out.println("Component: " + component + " Value: " + value);
                            if (commandsMap.containsKey(component)) {// Es un comando válido
                                // Hacer comparaciones
                                System.out.println("Command: " + commandsMap.get(component));
                                System.out.println();
                                // Tiene un valor válido para asignarle
                                System.out.println("Cumple");
                                tp.commands.add(new Command(component, "msg", value));

                            }
                        }

                        VirtualDevice newVD = new VirtualDevice(message);
                        // String newVDString = newVD.buildVD();
                        // System.out.println("Test new VD: " + newVDString);

                        HashMap<String, Component> newVDMap = newVD.getComponentsMap();
                        HashMap<String, Component> currentVDMap = vdState.getComponentsMap();

                        for (String key : newVDMap.keySet()) {
                            Component newComponent = newVDMap.get(key);
                            Component currentComponent = currentVDMap.get(key);

                            if (!newComponent.getValue().equals(currentComponent.getValue())) {
                                boolean isIN = currentComponent.getRol().equals("IN");
                                if (isIN) {
                                    tp.commands.add(new Command(key, action, newComponent.getValue()));
                                }
                            }
                        }

                        if (!tp.commands.isEmpty()) {
                            String packet = tp.buildPacket();
                            // Agregar validación adicional
                            if (tpPackets.isEmpty() || !packet.equals(tpPackets.getLast())) {
                                tpPackets.add(packet);
                                System.out.println("TenProtocol: " + packet);
                                if(direction.equals("100")){
                                    for (Command command : tp.commands) {
                                        System.out.println("Command+: " + command.toString());
                                        vdState.execute(command);
                                        response=vdState.buildVD();
                                    }
                                }else{
                                    sendToDriver(packet);
                                }
                            }
                        }
                    } else if (action.equals("cmd")) {
                        System.out.println("Se ejecutan comandos internamente");
                        // Recorrer el msg
                        for (int i = 1; i < commands.length - 1; i++) {
                            String command = commands[i];
                            String[] parts = command.split(":");
                            String component = parts[0];
                            String function = parts[1];
                            // System.out.println("Component: " + component + " Value: " + value);
                            if (commandsMap.containsKey(component)) {// Es un comando válido
                                // Hacer comparaciones
                                if (isIN(component) && isFunction(function)) {// Tiene un valor válido para asignarle
                                    System.out.println("Cumple y ejecuta");
                                    Command comm = new Command(component, action, function);
                                    vdState.execute(comm);
                                }
                            }
                        }
                    }
                } else {
                    if (!msg.isEmpty()) { // clientMessages.containsKey(clientAddress) &&
                                          // !clientMessages.get(clientAddress).isEmpty()

                        // ConcurrentLinkedQueue<String> clientQueue =
                        // clientMessages.get(clientAddress);
                        // System.out.println("Mensajes para cliente " + clientAddress + ":");

                        String mensaje = msg.get(0);
                        msg.remove(0);
                        System.out.println("- " + mensaje);
                        String protocol = mensaje.split("\\|")[0];
                        String data = mensaje.split("\\|")[1];
                        System.out.println("Data: " + data);
                        ArrayList<Command> commands = processPacket(protocol, App.protocols.get(protocol), data);
                        System.out.println(commands.size());
                        // vdState.eraseTextArea();
                        for (Command command : commands) {
                            System.out.println("Command+: " + command.toString());
                            vdState.execute(command);
                            System.out.println("Hi");
                        }

                        // vdPackets.add(response);
                    } else {
                        // Solo cambian componentes sin enviar o recibir
                        vdState = new VirtualDevice(response);
                    }
                }
                response = vdState.buildVD();
                System.out.println("Response: " + response);
                if (response != vdPackets.getLast()) {
                    vdPackets.add(response);
                }

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Método no permitido";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    public static String getMessage(String message) {
        StringBuilder textoConvertido = new StringBuilder();
        for (int i = 0; i < message.length(); i += 2) {
            String hex = message.substring(i, i + 2);
            int decimal = Integer.parseInt(hex, 16);
            textoConvertido.append((char) decimal);
        }
        return textoConvertido.toString();
    }

    public static boolean isCommandLine(String msg) {
        if (!msg.endsWith("&")) {
            return false;
        }

        // String input = msg; // "241:1 lred:1 lcd:1 msg:’Hola Grupo F1’";
        // ArrayList<String> tokens = new ArrayList<>();

        // Pattern pattern = Pattern.compile("msg:'.*?'|\\S+");
        // Matcher matcher = pattern.matcher(input);

        // while (matcher.find()) {
        // tokens.add(matcher.group());
        // }

        // String[] parts = tokens.toArray(new String[0]);

        // for (String part : parts) {
        // System.out.println(part);
        // }

        // // Debe tener al menos 2 elementos (cmd/msg y un componente:valor)
        // if (parts.length < 2) {
        // return false;
        // }

        // if (!parts[0].equals("cmd") && !parts[0].equals("msg")) {
        // return false;
        // }

        // // Verificar que todos los elementos después del primero tengan formato
        // // componente:valor
        // for (int i = 2; i < parts.length; i++) {
        // if (!parts[i].matches("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+")) {
        // return false;
        // }
        // }

        return true;
    }

    public static boolean isIN(String component) {
        String[] inComponents = { "switch0", "switch1", "slider0", "slider1", "slider2" };
        return Arrays.asList(inComponents).contains(component);
    }

    public static boolean isFunction(String function) {
        String[] functions = { "lcd", "fan", "lrgb", "lred", "lgreen", "heat" };
        return Arrays.asList(functions)
                .contains(function);
    }

    // Devuelve un array de comandos para luego ejecutarlos en VD
    public static ArrayList<Command> processPacket(String protocolId, Protocol protocol, String message) {

        ArrayList<Command> commands = new ArrayList<>();
        if (protocolId.equals("1")) {
            String[] commandsP = separarTexto(message);

            for (String command : commandsP) {
                String[] parts = command.split(protocol.commandSeparator);
                String component = App.protocols.get(protocolId).commandsMap.get(parts[0]);
                System.out.println("Component: " + component);
                System.out.println("Value: " + parts[1]);
                String value = parts[1];
                if (component.equals("speed")) {
                    value = decimalToHex(value);
                }else if(component.equals("slider1") || component.equals("slider2") || component.equals("slider0")){
                    System.out.println("value " + value);
                    value = toTargetLength(decimalToHex(value), 2);
                    System.out.println("value2 " + value);
                }
                if ((App.protocols.get(protocolId).commandsMap.containsKey(parts[0])
                        && value.matches(App.protocols.get(protocolId).commandsRegex.get(parts[0])))) {
                    System.out.println("Elemento");
                    commands.add(new Command(component, "msg", value));
                }
            }
        } else if (protocolId.equals("2")) {
            System.out.println("Mensaje Barrios: " + message);
            int length = 0;
            String command = "";
            String value = "";
            while (message.length() > 0) {
                if (message.startsWith("EE")) {
                    length = message.length();
                    command = "EE";
                    value = getMessage(message.substring(2));
                } else {
                    length = Integer.parseInt(message.substring(0, 1));
                    command = message.substring(1, 2);
                    int commandLength = length - 2;
                    value = message.substring(2, 2 + commandLength);
                }

                command = protocol.commandsMap.get(command);
                commands.add(new Command(command, "msg", value));
                message = message.substring(length);
            }
        } else if (protocolId.equals("8")) {
            String component = App.protocols.get(protocolId).commandsMap.get(message.substring(0, 1));
            String value = message.substring(1);
            commands.add(new Command(component, "msg", value));
        } else if (protocolId.equals("11") || protocolId.equals("B")) {
            try {
                JSONObject response = new JSONObject(message);

                int group = response.getInt("group");
                String element = response.getString("element");
                int type = response.getInt("type");
                String value = response.getString("value"); 
                String state = response.getString("state");

                if (type == 1) {
                    switch (element) {
                        case "FAN":
                            element = "SPD";
                            value = decimalToHex(value);
                            break;
                        case "RLD":
                            element = "LRGB";
                            break;
                        default:
                            break;
                    }
                }else{
                    value = state;
                }

                String component = App.protocols.get(protocolId).commandsMap.get(element);
                commands.add(new Command(component, "msg", value));
            } catch (JSONException e) {
                System.err.println("Error parsing Protocol 11 message: " + e.getMessage());
            }
        }

        return commands;
        // protocol|vd|message
    }

    public static String[] separarTexto(String input) {
        List<String> partes = new ArrayList<>();

        // Patrón: separa por espacios, pero captura todo lo que sigue después de "MSG:"
        String regex = "(MSG:.*)|([^\\s]+)";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            partes.add(matcher.group());
        }

        // Convertir la lista a un array y devolverlo
        return partes.toArray(new String[0]);
    }

    public static String decimalToHex(String decimalString) {
        try {
            // Convertir el string decimal a un entero
            int decimal = Integer.parseInt(decimalString);
            // Convertir el entero a hexadecimal
            return Integer.toHexString(decimal).toUpperCase();
        } catch (NumberFormatException e) {
            // Manejo de errores en caso de formato inválido
            throw new IllegalArgumentException("El input no es un número decimal válido: " + decimalString);
        }
    }

    public static String toTargetLength(String input, int targetLength) {
        while(input.length() < targetLength){
            input = "0" + input;
        }
        return input;
    }
}