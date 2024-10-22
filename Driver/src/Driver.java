
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

    // Cuando se desee recibir del VD, entonces se abre un udp server que recibe el mensaje, este mensaje se mandará por UART

    // -- 

    // Otro caso: Cuando se desea mandar al VD lo que se recibió del UART: -- Se abre el cliente y se manda por medio del cliente un request para que se le mande un response:
    // Lo del response se manda al VD

    // Mientras no existan cambios en los logs se sigue mandando el mismo: Siempre se manda el mismo response cuando se envia por medio del VD, 

    // 

    // UDP SERVER
    private volatile boolean isRunning = true;  // Controla cuándo el servidor está en ejecución

    // Método para activar el servidor UDP y recibir el mensaje del cliente
    public String startUdpServer() {
        String receivedMessage = null;
        try (DatagramSocket socket = new DatagramSocket(9876)) {
            byte[] receiveData = new byte[1024];
            System.out.println("Servidor UDP activado, esperando paquetes...");

            while (isRunning) {  // El servidor se ejecuta mientras isRunning sea true
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);  // Espera el paquete del cliente

                // Obtener el mensaje del cliente
                receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Mensaje recibido: " + receivedMessage);

                // Responder al cliente
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String response = "Respuesta del servidor";
                byte[] sendData = response.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                socket.send(sendPacket);

                // Opcional: Puedes salir del bucle después de recibir un mensaje
                break;  // Si deseas que el servidor cierre después de un solo mensaje, usa esta línea
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedMessage;  // Retorna el mensaje recibido
    }

    // Método para usar el servidor UDP solo cuando sea necesario
    public String useUdpServer(boolean activar) {
        if (activar) {
            System.out.println("Iniciando servidor UDP...");
            return startUdpServer();  // Inicia el servidor UDP y retorna el mensaje recibido
        } else {
            System.out.println("Servidor UDP no está activo.");
            return null;
        }
    }

    // Método para detener el servidor UDP
    public void stopUdpServer() {
        isRunning = false;  // Cambia el estado para detener el servidor
        System.out.println("Servidor UDP apagado.");
    }


/*
    public static void main(String[] args) throws InterruptedException {
        Driver driver = new Driver();

        // Ejecutar el servidor UDP en un hilo separado para poder detenerlo más tarde
        Thread serverThread = new Thread(() -> {
            String mensaje = driver.useUdpServer(true);  // Activa el servidor UDP y captura el mensaje recibido
            System.out.println("Mensaje procesado: " + mensaje);
        });

        serverThread.start();

        // Simulación de que el servidor corre por un tiempo, luego se apaga
        Thread.sleep(10000);  // Mantén el servidor activo durante 10 segundos (ajusta el tiempo según tu necesidad)
        driver.stopUdpServer();  // Apaga el servidor UDP
        serverThread.join();  // Espera que el hilo termine
    }*/
}
