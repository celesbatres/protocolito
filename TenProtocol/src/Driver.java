
import com.fazecast.jSerialComm.*;

import java.io.*;
import java.net.*;

public class Driver {

    public Driver() {
    }

    private static UdpServer udpServer;
    public static void startUdpServer() {
        udpServer = new UdpServer();
        Thread udpThread = new Thread(udpServer);
        udpThread.start();
    }

    public String toString(){
        return "Servidor UDP del Driver iniciado en el puerto 9091";
    }
}

class UdpServer implements Runnable {

    public UdpServer() {
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(9091)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String lastReceivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Driver recibió mensaje UDP: " + lastReceivedMessage);            
                sendToUART(lastReceivedMessage);
                // Probar cuando se pueda observar lo de UART
                // enviarMensaje(lastReceivedMessage, 0);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void sendToUART(String message){
        System.out.println("Digamos que se mandó a UART este mensaje : " + message);

    }

    // **************************CODIGO B****************************
    public static void enviarMensaje(String mensaje, int destino) {
        System.out.println("App -> Driver // mensaje \"" + mensaje + "\", destino \"" + destino + "\"");

        // Definir el puerto al que está conectada la Raspberry Pi Pico COM4 segun mi pc!!!!!
        SerialPort raspberryPort = SerialPort.getCommPort("COM4"); 

        // Configurar la conexión del puerto UART
        raspberryPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        raspberryPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        // Abrir el puerto
        if (raspberryPort.openPort()) {
            System.out.println("Conexión exitosa con la pipico");
        } else {
            System.out.println("No se pudo abrir el puerto.");
            return;
        }

        try {

            // Preparar el mensaje para enviar
            String mensajeModificado = mensaje + "\n"; 

            //pa mandar
            raspberryPort.writeBytes(mensajeModificado.getBytes(), mensajeModificado.length());

            System.out.println("Driver -> Fisica: " + mensajeModificado);

            // Leer la respuesta de la Pico
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = raspberryPort.readBytes(buffer, buffer.length);
                if (bytesRead > 0) {
                    String receivedData = new String(buffer, 0, bytesRead);
                    System.out.println("Fisica -> Driver: " + receivedData);
                    break; // Sale del bucle después de recibir la respuesta
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Cerrar el puerto
            raspberryPort.closePort();
        }
    }
}
