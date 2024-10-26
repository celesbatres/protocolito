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
import java.util.LinkedList;

public class App {

    public static void main(String[] args) throws IOException {
        Driver dr = new Driver();
        String driverInfo = dr.toString();
        System.out.println(driverInfo);
        // Crea el servidor en el puerto 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/post", new PostHandler());
        server.setExecutor(null); // Usa el ejecutor por defecto
        server.start();
        System.out.println("Servidor escuchando en http://localhost:8000/post");
        // sendToDriver("Hola desde app");
        Driver.startUdpServer();
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

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            LinkedList<String> vds = new LinkedList<>();
            vds.add("FFF000000000000000000000737769746368305F6C6564313D31");
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
                // Generar respuesta de 24 caracteres hexadecimales
                /*StringBuilder hexResponse = new StringBuilder();
                for (int i = 0; i < 24; i++) {
                    hexResponse.append(Integer.toHexString((int) (Math.random() * 16)));
                }

                response = hexResponse.toString();
                messages.add(response);*/
                // String lastMessage = vds.getLast();
                // if (lastMessage != response) {
                //     // Enviar mensaje al driver
                //     Driver driver = new Driver();
                //     driver.receiveMessageFromApp(lastMessage);
                // }
                

                

                // VirtualDevice vd = new VirtualDevice(response);
                // System.out.println("PickColor: "+vd.getPickColor());
                String response2 = getMessage(response.substring(24));
                // String response2 = "FFF000000000000000000000737769746368305F6C6564313D31";
                // System.out.println(response2);
                // response = "FFF000000000000000000000737769746368305F6C6564313D31";
                // // Envio al Driver
                // Driver driver = new Driver();
                // driver.receiveMessageFromApp();
                // udpClient udpClient = new udpClient(response2);
                System.out.println("Ultimo mensaje guardado: "+vds.getLast());
                System.out.println("Mensaje recibido: "+response);
                if(!response.equals(vds.getLast())){
                    vds.add(response);
                    System.out.println("Mensaje recibido");
                    sendToDriver(response2);
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

    public static String getMessage(String message){
        StringBuilder textoConvertido = new StringBuilder();
        for (int i = 0; i < message.length(); i += 2) {
            String hex = message.substring(i, i + 2);
            int decimal = Integer.parseInt(hex, 16);
            textoConvertido.append((char) decimal);
        }
        return textoConvertido.toString();
    }
    public void sendMessageToDriver(String message){
        // Abre un cliente udp y manda un mensaje al puerto del driver
    }  
}

class udpClient implements Runnable{
    private String packet;

    public udpClient(String packet){
        this.packet = packet;
    }

    @Override
    public void run(){
        try {
            int port = 9091;
            DatagramSocket ds = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();
            byte buffer[] = null;
            buffer = this.packet.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            ds.send(packet);
            ds.close();
        } catch (Exception e) {
            System.out.println("Alerta! Errores encontrados durante ejecución");
        }
    }
}
