import com.fazecast.jSerialComm.*;

import java.io.IOException;

/*import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;*/
//import java.nio.charset.StandardCharsets;

import java.net.*;

public class Driver {

    private static final int DriverPort = 5100;
    private static final int AplicacionPort = 5000;
    private static SerialPort puertoUtilizar;


    public static void main(String[] args) {
        // Inicializar el puerto serial una vez
        abrirPuerto();

        Thread receiverThread = new Thread(() -> recibeDatos());
        Thread senderThread = new Thread(() -> enviarMensaje());

        receiverThread.start();
        senderThread.start();
    }

    public static void abrirPuerto() {
        puertoUtilizar = SerialPort.getCommPort("COM6");
        puertoUtilizar.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        puertoUtilizar.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (puertoUtilizar.openPort()) {
            System.out.println("Si se hizo la conexion");
        } else {
            System.out.println("No se abrió el puerto");
        }
    }

    public static void recibeDatos() {
        try (DatagramSocket udpSocket = new DatagramSocket(9091)) {
            byte[] receiveBuffer = new byte[1024];
            
            System.out.println("Servidor UDP iniciado");

            while (true) {
                String message = convertirMensajeUDP(udpSocket, receiveBuffer);
                if (message != null) {
                    System.out.println("Datos de app a driver " + message);
                    sendToUART(message, 0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //convertir el mensaje recibido a string
    private static String convertirMensajeUDP(DatagramSocket udpSocket, byte[] buffer) {
        try {
            DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(udpPacket);
            String mensaje_nuevo = new String(udpPacket.getData(), 0, udpPacket.getLength());
            return mensaje_nuevo;
        } catch (IOException e) {
            System.err.println("Error al recibir mensaje UDP: " + e.getMessage());
            return null;
        }
    }

    public static void enviarMensaje() {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress appAddress = InetAddress.getByName("localhost");

            while (true) {
                String mensaje_recibido = recibirMensajeUART();
                if (mensaje_recibido != null && !mensaje_recibido.isEmpty()) {
                    System.out.println("Mensaje enviado a driver " + mensaje_recibido);

                    enviarUDP(udpSocket, mensaje_recibido, appAddress);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en la transmisión UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //convertir mensaje a string recibido del uart
    private static String recibirMensajeUART() {
        byte[] BufferUART = new byte[1024];
        try {
            while (true) {
                int bytesRead = puertoUtilizar.readBytes(BufferUART, BufferUART.length);
                if (bytesRead > 0) {
                    String datos_recibidos = new String(BufferUART, 0, bytesRead);
                    return datos_recibidos;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer datos de UART: " + e.getMessage());
        }
        return null; 
    }

    private static void enviarUDP(DatagramSocket udpSocket, String message, InetAddress address) {
        try {
            byte[] udpBuffer = message.getBytes();
            DatagramPacket udpPacket = new DatagramPacket(udpBuffer, udpBuffer.length, address, 8001);
            udpSocket.send(udpPacket);
            System.out.println("Driver -> App: Mensaje enviado a la aplicación.");
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje UDP: " + e.getMessage());
        }
    }

    public static void sendToUART(String message, int destino) {
        String datos_enviar = message + "\n";
        if (puertoUtilizar.isOpen()) {
            puertoUtilizar.writeBytes(datos_enviar.getBytes(), datos_enviar.length());
            System.out.println("Se envio de Driver a física");
        }else{
            System.err.println("Puerto no abierto");
        }
    }
}