
import com.fazecast.jSerialComm.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Driver {

    public static void main(String[] args) throws SocketException {
        // Como recibe el driver un mensaje de app -- Con un udp server corriendo
        String protocol = "UDP";
        String server = "127.0.0.1";
        int port = 9091;
        DatagramSocket socket = new DatagramSocket(port);
        byte[] input = new byte[65535];
        DatagramPacket receivePacket = null;

        try {
            receivePacket = new DatagramPacket(input, input.length);
            socket.receive(receivePacket);
            InetAddress c_address = receivePacket.getAddress();
            int port_c = receivePacket.getPort();

            String mess = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("< " + c_address + " app dice: " + mess);

            /*
                String resS = realizaOperacion(mess);
                System.out.println("> " + receivePacket.getAddress() + " server " + "[" + LocalDate.now() + " " + LocalTime.now() + "] " + protocol + ": " + resS);
                byte[] sendData = resS.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, c_address, port_c);
                socket.send(sendPacket);
             */
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Mandar mensaje hacia la pico por medio de UART

        SerialPort comPort = SerialPort.getCommPort("COM4");
        comPort.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        // comPort.openPort();
        // Diferencia entre READ_BLOCKING y READ_SEMI_BLOCKING ...
        // comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        if (comPort.openPort()) {
            System.out.println("Puerto abierto exitosamente.");

            try {
                // Obtener el OutputStream del puerto
                OutputStream salida = comPort.getOutputStream();
                InputStream entrada = comPort.getInputStream();
                // Mensaje que quieres enviar
                String mensaje = "Hola, tarjeta!\n";
                
                // Enviar el mensaje
                salida.write(mensaje.getBytes());
                salida.flush();

                System.out.println("Mensaje enviado: " + mensaje);

                // Esperar un poco para asegurar que el mensaje se envíe completamente
                Thread.sleep(1000);

                byte[] bufferLectura = new byte[1024];
                int bytesLeidos = entrada.read(bufferLectura);
                if (bytesLeidos > 0) {
                    String respuesta = new String(bufferLectura, 0, bytesLeidos);
                    System.out.println("Respuesta recibida: " + respuesta);
                } else {
                    System.out.println("No se recibió respuesta.");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Cerrar el puerto cuando hayas terminado
                comPort.closePort();
                System.out.println("Puerto cerrado.");
            }
        } else {
            System.out.println("No se pudo abrir el puerto.");
        }
    }

    public static void sendMessage(PrintWriter output, String message) {
        output.println(message);
        output.flush();
    }

    public static void receiveMessageFromApp() {
        //A UDP Server always running
    }

    public static void receiveMessageFromUART() {

    }
}
