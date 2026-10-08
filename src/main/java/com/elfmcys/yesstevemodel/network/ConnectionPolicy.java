package com.elfmcys.yesstevemodel.network;

/** A server without YSM is allowed; synchronized peers must share a build. */
public final class ConnectionPolicy {
    private ConnectionPolicy() {}
    public static boolean accepts(String remoteVersion, boolean remoteIsServer, String localVersion) {
        return remoteVersion == null ? remoteIsServer : localVersion.equals(remoteVersion);
    }
}
