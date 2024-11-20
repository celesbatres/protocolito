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
        puertoUtilizar = SerialPort.getCommPort("COM5");
        puertoUtilizar.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        puertoUtilizar.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (puertoUtilizar.openPort()) {
            System.out.println("Si se hizo la conexion");
        } else {
            System.out.println("No se abrió el puerto");
        }
    }

    //proceso que recibe de app por udp para enviar a uart
    //recibo de aplicacion
    public static void recibeDatos() {
        try (DatagramSocket udpSocket = new DatagramSocket(9091)) {
            byte[] receiveBuffer = new byte[1024];
            
            System.out.println("Servidor UDP iniciado ");

            while (true) {
                String message = convertirMensajeUDP(udpSocket, receiveBuffer);
                System.out.println("mensaje " + message);
                if (message != null) {
                    //System.out.println("Datos de app a driver " + message);
                    sendToUART(message);
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
            //System.out.println("mensaje nuevo" + mensaje_nuevo);
            return mensaje_nuevo;
        } catch (IOException e) {
            System.err.println("Error al recibir mensaje UDP: " + e.getMessage());
            return null;
        }
    }

<<<<<<< HEAD
    //de lo que envio a uart
=======
    //de lo que envio
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
    public static void sendToUART(String message) {
        String[] datos_enviar = message.split("\\|");
        
        String grupo_enviar = datos_enviar[0];
        String num_vd = datos_enviar[1];
        String data_enviar = datos_enviar[2];
        String nuevo_data = data_enviar + "\n";
        String final_enviar = "";
        int cant = data_enviar.length() + 1;
        String len = cant + "";
        if(cant > 15){
            final_enviar = "640" + grupo_enviar + "0" + num_vd + Integer.toHexString(cant);
        }else{
            final_enviar = "640" + grupo_enviar + "0" + num_vd + "0" + Integer.toHexString(cant);
        }

        if (puertoUtilizar.isOpen()) {
            //puertoUtilizar.writeBytes(datos_enviar.getBytes(), datos_enviar.length());
            
            byte[] header = new byte[4];

            // El valor hexadecimal se convierte a entero en base 16
            //int hexValue = 0x64020106;
            
            //System.out.println("final " + final_enviar + " hex " + prueba);
            //System.out.println(final_enviar);
            int hexValue = 0;
            for(int i = 0; i < final_enviar.length(); i+=2){
                int hexFor = Integer.parseInt("" + final_enviar.charAt(i) + final_enviar.charAt(i+1), 16);
                header[i/2] = (byte) hexFor;
            }

            // Asignamos cada byte del valor hexadecimal al arreglo haciendo un deplazamiento de bits
            /*header[0] = (byte) ((hexValue >> 24) & 0xFF); // obtiene los primeros 8 bits 
            header[1] = (byte) ((hexValue >> 16) & 0xFF); // obtiene los siguientes 8 bits
            header[2] = (byte) ((hexValue >> 8) & 0xFF);  // obtiene los siguientes 8 bits
            header[3] = (byte) (hexValue & 0xFF);        */ // obtiene los ultimos 8 bits
            
            puertoUtilizar.writeBytes(header, header.length);
            puertoUtilizar.writeBytes(nuevo_data.getBytes(), Integer.parseInt(len));

            System.out.println("Se envio de Driver a física: " + header[0] + header[1] + header[2] + header[3]);
            System.out.println("Se envio de Driver a física: " + nuevo_data + "j");
        }else{
            System.err.println("Puerto no abierto");
        }
    }




    //driver a app
    public static void enviarMensaje() {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress appAddress = InetAddress.getByName("localhost");
            
            while (true) {
                //mensaje completo que se recibe del grupo 
                String mensaje_recibido = recibirMensajeUART();
                //substring del grupo para hacer un if y luego mandar todos los datos a metodo, que se forme el header y lo recibido 
                String no_grupo = mensaje_recibido.substring(0,2);
                String no_vd = "";
                int vd_temporal = 0;
                String mensaje_e = "";
<<<<<<< HEAD
                String mensaje_temp = "";
                
                String header_dapp = "";
                System.out.println("grupo num " + no_grupo);
=======
                String header_dapp = "";
                System.out.println("grupo num " + no_grupo);
                //cambiar para ver del 01 o 02 etc
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                if(no_grupo.equals("01")){
                    no_vd = mensaje_recibido.substring(6, 8);
                    vd_temporal = Integer.parseInt(no_vd) - 1;
                    mensaje_e = mensaje_recibido.substring(12, mensaje_recibido.length());
<<<<<<< HEAD
                    if(mensaje_recibido.contains("msg")){
                        mensaje_temp = "'" + mensaje_e + "'";
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_temp);
                    }else{
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
                    }
                    //si no sirve este if de arriba, usa el header_dapp quemado de la siguiente linea.
                    //header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
=======
                    header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                //grupo 2
                }else if(no_grupo.equals("02")){
                    no_vd = mensaje_recibido.substring(8, 10);
                    vd_temporal = Integer.parseInt(no_vd) - 1;
                    mensaje_e = mensaje_recibido.substring(16, mensaje_recibido.length());
<<<<<<< HEAD
                    if(mensaje_recibido.contains("msg")){
                        mensaje_temp = "'" + mensaje_e + "'";
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_temp);
                    }else{
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
                    }
                    //si no sirve este if de arriba, usa el header_dapp quemado de la siguiente linea.
                    //header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
=======
                    header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                //grupo 8
                }else if(no_grupo.equals("08")){
                    no_vd = mensaje_recibido.substring(4, 6);
                    vd_temporal = Integer.parseInt(no_vd) - 1;
                    mensaje_e = mensaje_recibido.substring(10, mensaje_recibido.length());
<<<<<<< HEAD
                    if(mensaje_recibido.contains("msg")){
                        mensaje_temp = "'" + mensaje_e + "'";
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_temp);
                    }else{
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
                    }
                    //si no sirve este if de arriba, usa el header_dapp quemado de la siguiente linea.
                    //header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
=======
                    header_dapp = HeaderVirtualD(no_grupo, no_vd, mensaje_e);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                //grupo 11
                }else if(no_grupo.equals("11")){
                    no_vd = mensaje_recibido.substring(10, 12);
                    vd_temporal = Integer.parseInt(no_vd) - 1;
                    mensaje_e = mensaje_recibido.substring(16, mensaje_recibido.length());
<<<<<<< HEAD
                    if(mensaje_recibido.contains("msg")){
                        mensaje_temp = "'" + mensaje_e + "'";
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_temp);
                    }else{
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
                    }
                    //si no sirve este if de arriba, usa el header_dapp quemado de la siguiente linea.
                    //header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
=======
                    header_dapp = HeaderVirtualD(no_grupo, no_vd, mensaje_e);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                }
                //grupo 12
                else if(no_grupo.equals("12")){
                    /*no_vd = mensaje_recibido.substring(10, 12);
                    vd_temporal = Integer.parseInt(no_vd) - 1;*/
                    mensaje_e = mensaje_recibido.substring(14, mensaje_recibido.length());
<<<<<<< HEAD
                    if(mensaje_recibido.contains("msg")){
                        mensaje_temp = "'" + mensaje_e + "'";
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_temp);
                    }else{
                        header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
                    }
                    //si no sirve este if de arriba, usa el header_dapp quemado de la siguiente linea.
                    //header_dapp = HeaderVirtualD(no_grupo, vd_temporal + "", mensaje_e);
=======
                    header_dapp = HeaderVirtualD(no_grupo, no_vd, mensaje_e);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
                }

                //en un string que se va a mandar. 
                if (header_dapp != null && !header_dapp.isEmpty()) {
                    enviarUDP(udpSocket, header_dapp, appAddress);
                    System.out.println("Mensaje enviado de driver a app " + header_dapp);
                }
            }
        } catch (Exception e) {
            System.err.println("Error en la transmisión UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //define el header segun lo recibido 
    private static String HeaderVirtualD(String id_protocol, String no_vd, String mensaje){
        String header_vd = id_protocol.substring(1,2) + "|" + no_vd + "|" + mensaje;
        return header_vd;
    }

    //convertir mensaje a string recibido del uart
    private static String recibirMensajeUART() {
        String datos_recibidos = "";
        byte[] BufferUART = new byte[1024];
        int bytesRead = 0;
        while (true) {
            bytesRead = puertoUtilizar.readBytes(BufferUART, BufferUART.length);
            if (bytesRead > 0) {
                //datos_recibidos = new String(BufferUART, 0, bytesRead);
                //System.out.println("FISICA -> Driver: " + datos_recibidos);     
                break; // Sale del bucle después de recibir la respuesta
            }
        }

        if(BufferUART[0] == 1){
            datos_recibidos = Grupo1(BufferUART,bytesRead);
        }else if(BufferUART[0] == 2){
            datos_recibidos = Grupo2(BufferUART,bytesRead);
        }else if(BufferUART[0] == 8){
            datos_recibidos = Grupo8(BufferUART,bytesRead);
        }else if(BufferUART[0] == 11){
            datos_recibidos = Grupo11(BufferUART,bytesRead);
        }else if(BufferUART[0] == 12){
            datos_recibidos = Grupo12(BufferUART,bytesRead);
        }

        return datos_recibidos;
        
    }

    private static String Grupo1(byte[] mensajeRecibido, int cantBytes){
        StringBuilder h2 = new StringBuilder();
        for(int i = 0; i < 6; i++){
            h2.append(String.format("%02X", mensajeRecibido[i]));
        }

        String header_h2 = h2.toString();
        //mensajeRecibido[5] es la posición del length
<<<<<<< HEAD
        String mensaje = new String(mensajeRecibido, 6, cantBytes).substring(0, (mensajeRecibido[4] - 6));
=======
        String mensaje = new String(mensajeRecibido, 6, cantBytes).substring(0, mensajeRecibido[4]);
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
        String final_enviado = header_h2 + mensaje;

        return final_enviado;
    }

    private static String Grupo2(byte[] mensajeRecibido, int cantBytes){
        System.out.println("grupo 2 ");
        StringBuilder h2 = new StringBuilder();
        for(int i = 0; i < 8; i++){
            h2.append(String.format("%02X", mensajeRecibido[i]));
            System.out.println("mensaje recibido posicion " + mensajeRecibido[i]);
        }

        String header_h2 = h2.toString();

        String mensaje = new String(mensajeRecibido, 8, cantBytes).substring(0, mensajeRecibido[7]);
        String final_enviado = header_h2 + mensaje;

        return final_enviado;
    }

    private static String Grupo8(byte[] mensajeRecibido, int cantBytes){
        StringBuilder h2 = new StringBuilder();
        for(int i = 0; i < 7; i++){
            h2.append(String.format("%02X", mensajeRecibido[i]));
        }

        String header_h2 = h2.toString();

        String mensaje = new String(mensajeRecibido, 7, cantBytes).substring(0, mensajeRecibido[4]);
        String final_enviado = header_h2 + mensaje;

        return final_enviado;
    }

    private static String Grupo11(byte[] mensajeRecibido, int cantBytes){
        StringBuilder h2 = new StringBuilder();
        for(int i = 0; i < 8; i++){
            h2.append(String.format("%02X", mensajeRecibido[i]));
        }

        String header_h2 = h2.toString();

        String mensaje = new String(mensajeRecibido, 8, cantBytes).substring(0, mensajeRecibido[3]);
        String final_enviado = header_h2 + mensaje;

        return final_enviado;
    }

    private static String Grupo12(byte[] mensajeRecibido, int cantBytes){
        StringBuilder h2 = new StringBuilder();
        for(int i = 0; i < 7; i++){
            h2.append(String.format("%02X", mensajeRecibido[i]));
        }

        String header_h2 = h2.toString();

        String mensaje = new String(mensajeRecibido, 8, cantBytes).substring(0, mensajeRecibido[5]);
        String final_enviado = header_h2 + mensaje;

        return final_enviado;
    }

    private static void enviarUDP(DatagramSocket udpSocket, String message, InetAddress address) {
        try {
            byte[] udpBuffer = message.getBytes();
            DatagramPacket udpPacket = new DatagramPacket(udpBuffer, udpBuffer.length, address, 8001);
            udpSocket.send(udpPacket);
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje UDP: " + e.getMessage());
        }
    } 
<<<<<<< HEAD
}
=======
}
>>>>>>> 8618f057989b42df4cd8d35083d95591f71b3786
