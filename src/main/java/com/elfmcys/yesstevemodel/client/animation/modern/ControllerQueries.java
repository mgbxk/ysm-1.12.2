package com.elfmcys.yesstevemodel.client.animation.modern;

import com.elfmcys.yesstevemodel.client.animation.condition.AbstractConditionItem;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalHold;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalSwing;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalUse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumHand;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** String arguments are resolved before handing numeric expressions to Molang. */
public final class ControllerQueries {
    private static final Pattern CALL = Pattern.compile("ctrl\\.(hold|swing|use|armor|ride)\\(\\s*'([^']*)'\\s*,\\s*'([^']*)'\\s*\\)");
    private static final Pattern ITEM_NAMES = Pattern.compile("(?:query|q)\\.is_item_name_any\\(\\s*'([^']*)'\\s*((?:,\\s*'[^']*'\\s*)+)\\)");
    private static final Pattern ITEM_ID = Pattern.compile("'([^']*)'");
    private final Map<String, AbstractConditionItem> items = new HashMap<>();

    public String resolve(String expression, EntityPlayer player, Consumer<String> warning) {
        Matcher names = ITEM_NAMES.matcher(expression);
        StringBuffer itemsResult = new StringBuffer();
        while (names.find()) {
            boolean match = false;
            String slot = names.group(1);
            if (player != null && (slot.equals("mainhand") || slot.equals("offhand"))) {
                net.minecraft.item.ItemStack stack = player.getHeldItem(slot.equals("mainhand") ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
                    Matcher ids = ITEM_ID.matcher(names.group(2));
                    while (ids.find()) if (stack.getItem().getRegistryName().toString().equals(ids.group(1))) match = true;
                }
            }
            names.appendReplacement(itemsResult, match ? "1" : "0");
        }
        names.appendTail(itemsResult);
        expression = itemsResult.toString();
        Matcher matcher = CALL.matcher(expression);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            boolean match = player != null && test(matcher.group(1), matcher.group(2), matcher.group(3), player, warning);
            matcher.appendReplacement(result, match ? "1" : "0");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private boolean test(String operation, String slot, String filter, EntityPlayer player, Consumer<String> warning) {
        if (filter.startsWith("#")) { warning.accept("Minecraft 1.12 has no modern item/entity tag registry: " + filter); return false; }
        if (operation.equals("ride")) {
            if (!filter.startsWith("$")) return false;
            if (slot.equals("vehicle")) return matchesEntity(player.getRidingEntity(), filter.substring(1));
            if (slot.equals("passenger")) for (Entity passenger : player.getPassengers()) if (matchesEntity(passenger, filter.substring(1))) return true;
            return false;
        }
        if (operation.equals("armor")) {
            EntityEquipmentSlot equipment = ConditionArmor.getType(slot);
            if (equipment == null || equipment.getSlotType() != EntityEquipmentSlot.Type.ARMOR) return false;
            ConditionArmor condition = new ConditionArmor(); condition.addTest(slot + filter);
            return !condition.doTest(player, equipment).isEmpty();
        }
        if (!slot.equals("mainhand") && !slot.equals("offhand")) return false;
        EnumHand hand = slot.equals("mainhand") ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        if (operation.equals("swing") && (!player.isSwingInProgress || player.swingingHand != hand)) return false;
        if (operation.equals("use") && (!player.isHandActive() || player.getActiveHand() != hand)) return false;
        if (operation.equals("hold") && ((player.isSwingInProgress && player.swingingHand == hand) || (player.isHandActive() && player.getActiveHand() == hand))) return false;
        if (filter.equals(":empty")) return player.getHeldItem(hand).isEmpty();
        String key = operation + "/" + slot + "/" + filter;
        AbstractConditionItem condition = items.computeIfAbsent(key, ignored -> {
            AbstractConditionItem value;
            String prefix;
            if (operation.equals("hold")) { value = new ConditionalHold(hand); prefix = "hold_" + slot; }
            else if (operation.equals("use")) { value = new ConditionalUse(hand); prefix = "use_" + slot; }
            else { value = new ConditionalSwing(hand); prefix = hand == EnumHand.MAIN_HAND ? "swing" : "swing_offhand"; }
            value.addTest(prefix + filter);
            return value;
        });
        return !player.getHeldItem(hand).isEmpty() && !condition.doTest(player, hand).isEmpty();
    }

    private static boolean matchesEntity(Entity entity, String id) {
        return entity != null && EntityList.getKey(entity) != null && EntityList.getKey(entity).toString().equals(id);
    }
}
