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

public class App {
    // public static String receivedPacket = "";
    public static HashMap<String, Protocol> protocols = new HashMap<>();
    private static final Map<String, ConcurrentLinkedQueue<String>> clientMessages = new ConcurrentHashMap<>();
    private static final List<String> connectedClients = Collections.synchronizedList(new ArrayList<>());

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
                System.out.println("Protocol: " + tokens[1]);
                int virtualDevice = Integer.parseInt(tokens[1], 16); // ID del dispositivo virtual
                System.out.println("Virtual Device: " + virtualDevice);
                String data = protocol + "|" + tokens[2];
                synchronized (connectedClients) {
                    System.out.println("Clientes conectados:");
                    for (int i = 0; i < connectedClients.size(); i++) {
                        System.out.println("VD " + i + ": Cliente " + connectedClients.get(i));
                        if (i == virtualDevice) {
                            clientMessages.computeIfAbsent(connectedClients.get(i), k -> new ConcurrentLinkedQueue<>())
                                    .offer(data);
                            System.out.println("Mensaje almacenado para VD " + virtualDevice +
                                    " (Cliente: " + connectedClients.get(i) + ")");
                        }
                    }
                }
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
                put("SPEED", "speed");
                put("SLIDER0", "slider0");
                put("SLIDER1", "slider1");
                put("SLIDER2", "slider2");
                put("L_COLOR", "lrgb_color");
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
                put("LGREEN", "^[0-1]{1}$");
                put("HEAT", "^[0-1]{1}$");
                put("SPEED", "^(1[0-5]|[0-9])$");
                put("SLIDER0", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("SLIDER1", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("SLIDER2", "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$");
                put("L_COLOR", "^([A-Fa-f0-9]{6})$");
                put("COLOR", "^([A-Fa-f0-9]{6})$");
                put("MSG", "^[A-Za-z0-9_]+$");
            }
        };
        App.protocols.put("1", new Protocol(commandsMap, commandsRegex, " ", ":"));

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
                put("F", "msg");
            }
        };
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
    }

    static class PostHandler implements HttpHandler {
        // LinkedList<String> vds = new LinkedList<>(); // Lista de componentes
        // vds.add("FFF000000000000000000000737769746368305F6C6564313D31");
        ArrayList<String> logVD = new ArrayList<>();
        private static final LinkedList<String> vdPackets = new LinkedList<>();
        private static final LinkedList<String> tpPackets = new LinkedList<>();
        // private static final LinkedList<TenProtocol> tpPackets = new LinkedList<>();
        LinkedList<VirtualDevice> vds = new LinkedList<>();
        // LinkedList<VirtualDevice> vds = new LinkedList<>();
        String[] inputValidos = { "switch0", "switch1", "slider0", "slider1", "slider2", "pick_color" };

        // VirtualDevice vdState = new VirtualDevice(response);
        String[] commands = { "lcd", "switch0", "switch1", "fan", "lrgb", "lred", "lgreen", "heat", "speed", "slider0",
                "slider1", "slider2", "lrgb_color", "pick_color", "msg" };
        // TODO: Cabiar los value del hashmap a ReGex
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
                put("msg", "^[A-Za-z0-9_]+$");
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
                    System.out.println(line);
                    body.append(line);
                }

                String response = body.toString();

                if (vdPackets.isEmpty()) {
                    vdPackets.add("00F000000000000000000000");
                }

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
                    vdPackets.add(message);

                    String[] commands = commandLine.split(" ");
                    String action = commands[0];

                    if (action.equals("msg")) {
                        System.out.println("Es mensaje");
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
                                if (value.matches(commandsMap.get(component))) {// Tiene un valor válido para asignarle
                                    tp.commands.add(new Command(component, "msg", value));
                                }
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
                                    tp.commands.add(new Command(key, "msg", newComponent.getValue()));
                                }
                            }
                        }

                        if (!tp.commands.isEmpty()) {
                            String packet = tp.buildPacket();
                            // Agregar validación adicional
                            if (tpPackets.isEmpty() || !packet.equals(tpPackets.getLast())) {
                                tpPackets.add(packet);
                                System.out.println("TenProtocol: " + packet);
                                // sendToDriver(packet);
                            }
                        }
                    } else if (action.equals("cmd")) {
                        System.out.println("Se ejecutan comandos internamente");
                    }
                } else {
                    if (clientMessages.containsKey(clientAddress) && !clientMessages.get(clientAddress).isEmpty()) {
                        ConcurrentLinkedQueue<String> clientQueue = clientMessages.get(clientAddress);
                        System.out.println("Mensajes para cliente " + clientAddress + ":");

                        for (String mensaje : clientQueue) {

                            System.out.println("- " + mensaje);
                            String protocol = mensaje.split("\\|")[0];
                            String data = mensaje.split("\\|")[1];
                            System.out.println("Data: " + data);
                            ArrayList<Command> commands = processPacket(protocol, App.protocols.get(protocol), data);
                            vdState.eraseTextArea();
                            for (Command command : commands) {
                                System.out.println("Command: " + command.toString());
                                vdState.execute(command);
                            }
                            // clientQueue.remove();
                            // clientQueue.poll();
                            clientMessages.get(clientAddress).poll();
                        }
                        response = vdState.buildVD();
                        System.out.println("Response: " + response);
                    } else {
                        // System.out.println("No hay mensajes para el cliente " + clientAddress);
                    }

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

        String[] parts = msg.substring(0, msg.length() - 2).split(" ");

        // Debe tener al menos 2 elementos (cmd/msg y un componente:valor)
        if (parts.length < 2) {
            return false;
        }

        if (!parts[0].equals("cmd") && !parts[0].equals("msg")) {
            return false;
        }

        // Verificar que todos los elementos después del primero tengan formato
        // componente:valor
        for (int i = 2; i < parts.length; i++) {
            if (!parts[i].matches("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+")) {
                return false;
            }
        }

        return true;
    }

    // Devuelve un array de comandos para luego ejecutarlos en VD
    public static ArrayList<Command> processPacket(String protocolId, Protocol protocol, String message) {

        ArrayList<Command> commands = new ArrayList<>();
        if (protocolId.equals("1")) {
            String[] commandsP = message.split(protocol.commandDelimiter);
            for (String command : commandsP) {
                String[] parts = command.split(protocol.commandSeparator);
                String component = App.protocols.get(protocolId).commandsMap.get(parts[0]);
                System.out.println("Component: " + component);
                System.out.println("Value: " + parts[1]);
                String value = parts[1];
                commands.add(new Command(component, "msg", value));
            }
        } else if (protocolId.equals("2")) {

            while (message.length() > 0) {
                int length = Integer.parseInt(message.substring(0, 1));
                String command = message.substring(1, 2);
                int commandLength = length - 2;
                String value = message.substring(2, 2 + commandLength);
                command = protocol.commandsMap.get(command);
                commands.add(new Command(command, "msg", value));
                message = message.substring(length);
            }
        }

        return commands;
        // protocol|vd|message
    }
}