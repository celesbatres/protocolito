import java.util.*;

public class VirtualDevice {
    public static final String protocol = "A"; //TenProtocol
    private HashMap<String, Component> componentsMap;
    // private ArrayList<Command> command s; // if - commands is empty (Los que existen en el )

    // private String protocolFrom = ""; // Si protocolTo == protocol: Significa que es un VD de recepcion y decide si enviarlo a driver o responder a VD -> Desde APP
    // private String protocolTo = "";
    private String action = ""; // cmd, msg
    private String vd; // VD completo
    private String message; // Ultimos bits después de 24 en formato de texto
    // private String 
    

    // vd.setInfo() -> En dependencia del mensaje, despues del bit 24 - editamos

    //Para cambiar 

    public VirtualDevice(String vd) {
        this.componentsMap = new HashMap<>();
        this.componentsMap.put("control", new Component(vd.substring(0, 2)));
        this.componentsMap.put("speed", new Component(vd.substring(2, 3)));
        this.componentsMap.put("space", new Component(vd.substring(3, 4)));
        this.componentsMap.put("slider0", new Component(vd.substring(4,6)));
        this.componentsMap.put("slider1", new Component(vd.substring(6, 8)));
        this.componentsMap.put("slider2", new Component(vd.substring(8, 10)));
        this.componentsMap.put("space", new Component(vd.substring(10, 11)));
        this.componentsMap.put("ledRgb", new Component(vd.substring(11, 17)));
        this.componentsMap.put("space", new Component(vd.substring(17, 18)));
        this.componentsMap.put("pickColor", new Component(vd.substring(18, 24)));

        // this.components = new ArrayList<>();
        // this.components.add(new Component("control", vd.substring(0, 2)));
        // this.components.add(new Component("speed", vd.substring(2, 3)));
        // this.components.add(new Component("space", vd.substring(3, 4)));
        // this.components.add(new Component("slider0", vd.substring(4,6)));
        // this.components.add(new Component("slider1", vd.substring(6, 8)));
        // this.components.add(new Component("slider2", vd.substring(8, 10)));
        // this.components.add(new Component("space", vd.substring(10, 11)));
        // this.components.add(new Component("ledRgb", vd.substring(11, 17)));
        // this.components.add(new Component("space", vd.substring(17, 18)));
        // this.components.add(new Component("pickColor", vd.substring(18, 24)));

        this.vd = vd;
        String msg = vd.substring(24);
        // Convertir el mensaje hexadecimal a texto
        StringBuilder textoConvertido = new StringBuilder();
        for (int i = 0; i < msg.length(); i += 2) {
            String hex = msg.substring(i, i + 2);
            int decimal = Integer.parseInt(hex, 16);
            textoConvertido.append((char) decimal);
        }
        this.message = textoConvertido.toString();
        // String[] params = this.message.split(" ");
        // this.action = params[0];
        // this.protocolTo = params[1];    
    }

    //- Se almacenan y cuando se mande el siguiente request del VD estará mandando lo que recibió
    //
    public void action(){
        // this.message 
        String[] params = this.message.split(" ");
        this.action = params[0];
        // this.protocolFrom = params[1];
        // this.protocolTo = params[2];
        this.action = params[3];
        String[] commands = params[4].split(",");
        // -- Depedendiendo del action entonces se hacen cambios en los componentes 
        for(String command : commands){
            if(this.action.equals("cmd")){
                String[] commandParams = command.split(":");
                String componentName = commandParams[0];
                String componentFunction = commandParams[1];
                // Cambiar el valor del componente
            }else{
                // -- Si es msg entonces se manda el mensaje completo
                String[] msgParams = command.split(":");
                String componentName = msgParams[0];
                String componentValue = msgParams[1];
                // Cambiar el valor del componente
            }
        }
    }

    public VirtualDevice(String control, String speed, String slider0, String slider1, String slider2, String ledRgb, String pickColor, String message){
        this.componentsMap = new HashMap<>();
        this.componentsMap.put("control", new Component(control));
        this.componentsMap.put("speed", new Component(speed));
        this.componentsMap.put("slider0", new Component(slider0));
        this.componentsMap.put("slider1", new Component(slider1));
        this.componentsMap.put("slider2", new Component(slider2));
        this.componentsMap.put("ledRgb", new Component(ledRgb));
        this.componentsMap.put("pickColor", new Component(pickColor));
        this.componentsMap.put("message", new Component(message));
        this.componentsMap.put("space", new Component(" "));
        this.message = message;
        // this.vd = buildVD();
    }


    // Devuelve el VD completo en dependencia de los atributos
    // public String buildVD(){
    //     String vd = this.control + this.speed + this.space + this.slider0 + this.slider1 + this.slider2 + this.space + this.ledRgb + this.space + this.pickColor + this.message;
    //     return vd;
    // }

    public boolean equals(VirtualDevice vd){
        return this.vd.equals(vd.vd);
    }

    public String getAction(){
        return this.action;
    }
    

    // Getters
    public String getControl() {
        return this.componentsMap.get("control").value;
    }

    public String getSpeed() {
        return this.componentsMap.get("speed").value;
    }

    public String getSlider0() {
        return this.componentsMap.get("slider0").value;
    }

    public String getSlider1() {
        return this.componentsMap.get("slider1").value;
    }

    public String getSlider2() {
        return this.componentsMap.get("slider2").value;
    }

    public String getLedRgb() {
        return this.componentsMap.get("ledRgb").value;
    }

    public String getPickColor() {
        return this.componentsMap.get("pickColor").value;
    }

    public String getMessage() {
        return this.componentsMap.get("message").value;
    }

    // Setters
    public void setControl(String control) {
        this.componentsMap.get("control").setValue(control);
    }

    public void setSpeed(String speed) {
        this.componentsMap.get("speed").setValue(speed);
    }

    public void setSlider0(String slider0) {
        this.componentsMap.get("slider0").setValue(slider0);
    }

    public void setSlider1(String slider1) {
        this.componentsMap.get("slider1").setValue(slider1);
    }

    public void setSlider2(String slider2) {
        this.componentsMap.get("slider2").setValue(slider2);
    }

    public void setLedRgb(String ledRgb) {
        this.componentsMap.get("ledRgb").setValue(ledRgb);
    }

    public void setPickColor(String pickColor) {
        this.componentsMap.get("pickColor").setValue(pickColor);
    }

    public void setMessage(String message) {
        this.componentsMap.get("message").setValue(message);
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        VirtualDevice other = (VirtualDevice) obj;
        
        return getControl().equals(other.getControl()) &&
               getSpeed().equals(other.getSpeed()) &&
               getSlider0().equals(other.getSlider0()) &&
               getSlider1().equals(other.getSlider1()) &&
               getSlider2().equals(other.getSlider2()) &&
               getLedRgb().equals(other.getLedRgb()) &&
               getPickColor().equals(other.getPickColor()) &&
               getMessage().equals(other.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getControl(), getSpeed(), getSlider0(), getSlider1(), 
                          getSlider2(), getLedRgb(), getPickColor(), getMessage());
    }
}
