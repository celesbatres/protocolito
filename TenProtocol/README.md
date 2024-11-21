## TenProtocol

Para ejecutar correctamente los archivos se debe tener en cuenta que se debe inicializar el WebServer para empezar a recibir datos del Virtual Device

# Ejecución de App.java
Debe estar en la carpeta /src y ejecutar los siguientes comandos:

- `javac -cp json-20240303.jar App.java VirtualDevice.java Component.java Command.java TenProtocol.java Protocol.java`: Compila el archivo principal y sus dependencias
- `java -cp json-20240303.jar App.java`: Ejecuta la aplicación de App.java

# Ejecución de Driver.java

Este archivo explica cómo compilar y ejecutar `Driver.java` utilizando la librería `jSerialComm`.

---

## Pasos para Ejecutar

1. **Compilar**  
   Ejecuta el siguiente comando en la terminal desde el directorio donde se encuentra `Driver.java`:

   ```bash
   javac -cp "../lib/jSerialComm-1.11.0.jar" Driver.java


	2.	Ejecutar: Una vez compilado, ejecuta el archivo con el siguiente comando:
        java -cp "../lib/jSerialComm-1.11.0.jar:." Driver


