package fi.digitraffic.mqtt;

public class ServerConfig {
    public final String serverAddress;
    public final String statusTopic;

    public final boolean needUsername;

    public static final String USERNAME = "digitraffic";
    public static final String PASSWORD = "digitrafficPassword";
    public static final String CLIENT_ID = "hass-digitraffic-";

    public static final ServerConfig ROAD = new ServerConfig("wss://tie.digitraffic.fi:61619/mqtt", "tms/status", true);
    public static final ServerConfig MARINE = new ServerConfig("wss://meri.digitraffic.fi:61619/mqtt", "sse/status", true);
    public static final ServerConfig RAIL = new ServerConfig("wss://rata.digitraffic.fi:443/mqtt", null, false);

    public ServerConfig(final String serverAddress, final String statusTopic, final boolean needUsername) {
        this.serverAddress = serverAddress;
        this.statusTopic = statusTopic;
        this.needUsername = needUsername;
    }
}
