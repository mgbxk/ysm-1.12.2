package com.elfmcys.yesstevemodel.network;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** REGISTER arrives before the connection-completed event in Forge 1.12. */
public final class RemoteYsmServers {
    private final Map<Object, Boolean> servers = Collections.synchronizedMap(new WeakHashMap<>());
    public void update(Object connection, Set<String> channels, boolean registering) {
        if (channels.contains("yesstevemodel")) {
            if (registering) servers.put(connection, true); else servers.remove(connection);
        }
    }
    public boolean contains(Object connection) { return Boolean.TRUE.equals(servers.get(connection)); }
    public void forget(Object connection) { servers.remove(connection); }
}
