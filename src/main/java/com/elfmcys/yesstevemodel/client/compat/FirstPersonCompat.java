//package com.elfmcys.yesstevemodel.client.compat;
//
//import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
//import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
//import dev.tr7zw.firstperson.FirstPersonModelCore;
//import net.minecraft.util.math.vector.Vector3d;
//import net.minecraftforge.fml.loading.LoadingModList;
//
//public class FirstPersonCompat {
//    private static final String MOD_ID = "firstpersonmod";
//    private static boolean INSTALLED;
//
//    public static void init() {
//        // Early Loading 期间不能用 ModList
//        INSTALLED = LoadingModList.get().getModFileById(MOD_ID) != null;
//    }
//
//    public static Vector3d transformPlayerOffset(Vector3d current) {
//        return new Vector3d(current.x(), 1.5 - CustomPlayerModel.FIRST_PERSON_HEAD_POS / 16, current.z());
//    }
//
//    public static boolean isInstalled() {
//        return INSTALLED;
//    }
//
//    public static void hideHead(IBone head) {
//        head.setHidden(FirstPersonModelCore.isRenderingPlayer);
//    }
//
//    public static boolean isHeadHide() {
//        return FirstPersonModelCore.isRenderingPlayer;
//    }
//}
