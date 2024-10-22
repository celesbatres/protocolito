
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

public class Server {

    public static void main(String[] args) throws IOException {
        // Crea el servidor en el puerto 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/post", new PostHandler());
        server.setExecutor(null); // Usa el ejecutor por defecto
        server.start();
        System.out.println("Servidor escuchando en http://localhost:8000/post");
    }

    public static void sendToDriver(String message) {
        // Abre un cliente udp y manda un mensaje al puerto del driver
        try {
            int port = 9091;
            DatagramSocket ds = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();
            byte buffer[] = null;
            buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
            ds.send(packet);
            ds.close();
        } catch (Exception e) {
            System.out.println("Alerta! Errores encontrados durante ejecución");
        }
    }

    public static void sendToVirtualDevice(String message){
        
    }

    static class PostHandler implements HttpHandler {

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
                // Generar respuesta de 24 caracteres hexadecimales
                StringBuilder hexResponse = new StringBuilder();
                for (int i = 0; i < 24; i++) {
                    hexResponse.append(Integer.toHexString((int) (Math.random() * 16)));
                }

                response = hexResponse.toString();
                response = "FFF000000000000000042804";
                // Envio al Driver
                sendToDriver(response);
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
}
