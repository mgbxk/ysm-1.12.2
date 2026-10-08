package com.elfmcys.yesstevemodel.network;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.Tags;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionPolicyTest {
    @Test void acceptsVanillaAndModdedServersWithoutYsmButRequiresClientsOnYsmServers() {
        assertTrue(ConnectionPolicy.accepts(null, true, "build"));
        assertFalse(ConnectionPolicy.accepts(null, false, "build"));
    }
    @Test void synchronizedPeersRequireMatchingBuilds() {
        assertTrue(ConnectionPolicy.accepts("build", true, "build"));
        assertTrue(ConnectionPolicy.accepts("build", false, "build"));
        assertFalse(ConnectionPolicy.accepts("old", true, "build"));
        assertFalse(ConnectionPolicy.accepts("old", false, "build"));
    }
    @Test void forgeFindsTheHandlerOnTheActualModEntrypoint() throws Exception {
        assertTrue(YesSteveModel.class.getMethod("checkRemoteMods", Map.class, Side.class).isAnnotationPresent(NetworkCheckHandler.class));
        YesSteveModel mod = new YesSteveModel();
        assertTrue(mod.checkRemoteMods(Collections.emptyMap(), Side.SERVER));
        assertFalse(mod.checkRemoteMods(Collections.emptyMap(), Side.CLIENT));
        assertTrue(mod.checkRemoteMods(Collections.singletonMap(YesSteveModel.MOD_ID, Tags.VERSION), Side.CLIENT));
    }
}
