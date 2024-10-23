public class VirtualDevice {
    private String vd; // All
    private String message;
    private String control;
    private String speed;
    private String space;
    private String slider0;
    private String slider1;
    private String slider2;
    private String ledRgb;
    private String pickColor;

    // Getters
    public String getVd() {
        return vd;
    }

    public String getMessage() {
        return message;
    }

    public String getControl() {
        return control;
    }

    public String getSpeed() {
        return speed;
    }

    public String getSpace() {
        return space;
    }

    public String getSlider0() {
        return slider0;
    }

    public String getSlider1() {
        return slider1;
    }

    public String getSlider2() {
        return slider2;
    }

    public String getLedRgb() {
        return ledRgb;
    }

    public String getPickColor() {
        return pickColor;
    }

    // Setters
    public void setVd(String vd) {
        this.vd = vd;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public void setSlider0(String slider0) {
        this.slider0 = slider0;
    }

    public void setSlider1(String slider1) {
        this.slider1 = slider1;
    }

    public void setSlider2(String slider2) {
        this.slider2 = slider2;
    }

    public void setLedRgb(String ledRgb) {
        this.ledRgb = ledRgb;
    }

    public void setPickColor(String pickColor) {
        this.pickColor = pickColor;
    }

    public VirtualDevice(String vd) {
        this.vd = vd;
        this.message = vd.substring(24);
        // Convertir el mensaje hexadecimal a texto
        StringBuilder textoConvertido = new StringBuilder();
        for (int i = 0; i < message.length(); i += 2) {
            String hex = message.substring(i, i + 2);
            int decimal = Integer.parseInt(hex, 16);
            textoConvertido.append((char) decimal);
        }
        this.message = textoConvertido.toString();
        this.control = vd.substring(0, 2);
        this.speed = vd.substring(2, 3);
        this.space = vd.substring(3, 4);
        this.slider0 = vd.substring(4,6);
        this.slider1 = vd.substring(6, 8);
        this.slider2 = vd.substring(8, 10);
        this.ledRgb = vd.substring(11, 17);
        this.pickColor = vd.substring(18, 24);
    }
}
