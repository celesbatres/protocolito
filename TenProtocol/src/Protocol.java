import java.util.HashMap;

public class Protocol {
    HashMap<String, String> commandsMap;
    HashMap<String, String> commandsRegex;
    String commandDelimiter;
    String commandSeparator;

    public Protocol(HashMap<String, String> commandsMap, HashMap<String, String> commandsRegex, String commandDelimiter, String commandSeparator){
        this.commandsMap = commandsMap;
        this.commandsRegex = commandsRegex;
        this.commandDelimiter = commandDelimiter;
        this.commandSeparator = commandSeparator;
    }
}
