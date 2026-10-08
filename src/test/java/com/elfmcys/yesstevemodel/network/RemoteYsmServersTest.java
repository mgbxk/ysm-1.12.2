package com.elfmcys.yesstevemodel.network;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class RemoteYsmServersTest {
    @Test void remembersServerRegistrationBeforeLoginCompletesWithoutNeedingDispatcherModList() {
        RemoteYsmServers peers = new RemoteYsmServers(); Object connection = new Object();
        peers.update(connection, Collections.singleton("yesstevemodel"), true);
        assertTrue(peers.contains(connection));
    }
    @Test void switchingAndOldDisconnectsCannotEnableOrClearAnotherServersState() {
        RemoteYsmServers peers = new RemoteYsmServers(); Object oldServer = new Object(), newServer = new Object();
        peers.update(oldServer, Collections.singleton("yesstevemodel"), true);
        assertFalse(peers.contains(newServer));
        peers.update(newServer, Collections.singleton("yesstevemodel"), true);
        peers.forget(oldServer); assertFalse(peers.contains(oldServer)); assertTrue(peers.contains(newServer));
    }
    @Test void unrelatedChannelsNeverEnableSyncAndUnregisterRemovesSupport() {
        RemoteYsmServers peers = new RemoteYsmServers(); Object connection = new Object();
        peers.update(connection, Collections.singleton("FML|HS"), true); assertFalse(peers.contains(connection));
        peers.update(connection, Collections.singleton("yesstevemodel"), true);
        peers.update(connection, Collections.singleton("MC|Brand"), false); assertTrue(peers.contains(connection));
        peers.update(connection, Collections.singleton("yesstevemodel"), false); assertFalse(peers.contains(connection));
    }
}
