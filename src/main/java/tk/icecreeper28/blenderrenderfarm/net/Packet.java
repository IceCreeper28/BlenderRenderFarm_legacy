package tk.icecreeper28.blenderrenderfarm.net;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class Packet implements Serializable {
    private HashMap<String, String> headers;
    private HashMap<String, Object> payload;

    private UUID packetID;
    private long createdAt;

    public Packet(String contentName, String contentType) {
        this.headers = new HashMap<>();
        this.payload = new HashMap<>();

        this.packetID = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();

        this.headers.put("Content", contentName);
        this.headers.put("Content-Type", contentType);
    }

    public Packet addHeader(String name, String value) {
        if (!headerExists(name)) {
            headers.put(name, value);
        } else {
            headers.replace(name, value);
        }
        return this;
    }

    public Packet addContent(String name, Object value) {
        if (!contentExists(name)) {
            payload.put(name, value);
        } else {
            payload.replace(name, value);
        }
        return this;
    }

    public boolean headerExists(String name) {
        return headers.containsKey(name);
    }

    public boolean contentExists(String name) {
        return payload.containsKey(name);
    }

    public String getHeader(String name) {
        if (headerExists(name)) {
            return headers.get(name);
        }
        return null;
    }

    public Object getContent(String name) {
        if (contentExists(name)) {
            return payload.get(name);
        }
        return null;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

    public UUID getPacketID() {
        return packetID;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
