import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

public class App {

    public static void main(String[] args) throws IOException {
        // Driver dr = new Driver();
        // String driverInfo = dr.toString();
        // System.out.println(driverInfo);
        // Crea el servidor en el puerto 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/post", new PostHandler());
        server.setExecutor(null); // Usa el ejecutor por defecto
        server.start();
        System.out.println("Servidor escuchando en http://localhost:8000/post");
        // sendToDriver("Hola desde app");

        Thread recibo = new Thread(() -> recibodedriver());
        recibo.start();

    }

    public static void recibodedriver() {
        try (DatagramSocket socket = new DatagramSocket(8001)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Driver -> App " + message);
                // Enviar el mensaje al VD..
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

    static class PostHandler implements HttpHandler {
        // LinkedList<String> vds = new LinkedList<>(); // Lista de componentes
        // vds.add("FFF000000000000000000000737769746368305F6C6564313D31");
        ArrayList<String> logVD = new ArrayList<>();
        private static final LinkedList<String> vdPackets = new LinkedList<>();
        private static final LinkedList<String> tpPackets = new LinkedList<>();
        // private static final LinkedList<TenProtocol> tpPackets = new LinkedList<>();
        LinkedList<VirtualDevice> vds = new LinkedList<>();
        // LinkedList<VirtualDevice> vds = new LinkedList<>();

        VirtualDevice vdState = new VirtualDevice("00F000000000000000000000");
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
        // Hashmap de comandos con su value y su regex
        // LinkedList<Command> commands = new LinkedList<>();// Lista de comandos
        // String[] protocols = {"00", "01", "02", "03", "04", "05", "06", "07", "08",
        // "09", "0A", "0B", "0C", "0D", "0E", "0F"};

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Agregar encabezados CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Responder a las solicitudes preflight de CORS
                exchange.sendResponseHeaders(204, -1); // Sin contenido
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

                // Respuesta
                String response = body.toString();
                System.out.println("Response: " + response);

                if (vdPackets.isEmpty()) {
                    vdPackets.add("00F000000000000000000000");
                }

                // if (tpPackets.isEmpty()) {
                //     tpPackets.add(new TenProtocol(""));
                // }
                // 32 caracteres decimales - 64 caracteres hexadecimales - 24+64 = 88 caracteres
                String message;
                if (response.length() >= 88) {
                    message = response.substring(0, 88);
                } else {
                    message = response; // O manejar el caso según tus necesidades
                    // System.out.println("Advertencia: Response menor a 56 caracteres");
                }
                String commandLineHex = message.substring(24); // Valores Validos
                System.out.println("Msg: " + commandLineHex);

                String commandLine = getMessage(commandLineHex);
                System.out.println("Command Line: " + commandLine);

                String lastPacket = vdPackets.getLast();
                // String lastTpPacket = tpPackets.getLast().buildPacket();

                if (isCommandLine(commandLine) && !message.equals(lastPacket)) {// Se revisa que tenga al final un
                                                                                // ampersand y que cumpla con el
                                                                                // formato: msg|cmd [component:value]
                                                                                // [component:value]
                    System.out.println("Es comando");
                    vdPackets.add(message);

                    String[] commands = commandLine.split(" ");
                    String action = commands[0];

                    if (action.equals("msg")) {
                        System.out.println("Es mensaje");
                        String direction = commands[1];
                        System.out.println("Direction: " + direction);

                        TenProtocol tp = new TenProtocol(direction);

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
                                    tp.commandsString.add(command);
                                    System.out.println("Command added: " + tp.commandsString);
                                }
                            }
                        }

                        VirtualDevice newVD = new VirtualDevice(message); 

                        HashMap<String, Component> newVDMap = newVD.getComponentsMap();
                        HashMap<String, Component> currentVDMap = vdState.getComponentsMap();

                        for(String key : newVDMap.keySet()){
                            Component newComponent = newVDMap.get(key);
                            Component currentComponent = currentVDMap.get(key);
                            
                            if(!newComponent.getValue().equals(currentComponent.getValue())){
                                boolean isIN = currentComponent.getRol().equals("IN");
                                if(isIN){
                                    tp.commands.add(new Command(key, "msg", newComponent.getValue()));
                                    tp.commandsString.add(key + ":" + newComponent.getValue());
                                }
                            }
                        }

                        if (tp.commandsString.size() > 0) {
                            String packet = tp.buildPacket();
                            
                            // Agregar validación adicional
                            if (tpPackets.isEmpty() || !packet.equals(tpPackets.getLast())) {
                                tpPackets.add(packet);
                                System.out.println("TenProtocol: " + packet);
                                sendToDriver(packet);
                            }
                        }
                    } else if (action.equals("cmd")) {
                        // ************************CAMBIAR EL FUNCTION DE UN
                        // COMPONENTE**************************
                        System.out.println("Se ejecutan comandos internamente");
                    }
                } else {
                    // System.out.println("Repetido");
                }

                response = "FEF000000000000000000000";
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
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].matches("[a-zA-Z0-9_]+:[a-zA-Z0-9_]+")) {
                return false;
            }
        }

        return true;
    }
}