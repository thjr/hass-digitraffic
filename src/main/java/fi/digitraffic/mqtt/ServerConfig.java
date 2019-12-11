package fi.digitraffic.mqtt;

public class ServerConfig {
    public final String serverAddress;
    public final String statusTopic;

    public static final String USERNAME = "digitraffic";
    public static final String PASSWORD = "digitrafficPassword";
    public static final String CLIENT_ID = "hass-digitraffic-";

    public static final ServerConfig ROAD = new ServerConfig("wss://tie.digitraffic.fi:61619/mqtt", "tms/status");
    public static final ServerConfig MARINE = new ServerConfig("wss://meri.digitraffic.fi:61619/mqtt", "sse/status");

    public ServerConfig(final String serverAddress, final String statusTopic) {
        this.serverAddress = serverAddress;
        this.statusTopic = statusTopic;
    }
}
