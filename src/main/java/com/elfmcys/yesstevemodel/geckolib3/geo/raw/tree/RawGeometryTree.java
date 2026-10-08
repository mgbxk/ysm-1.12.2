package com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Bone;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.MinecraftGeometry;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawGeometryTree {
    public Map<String, RawBoneGroup> topLevelBones = new Object2ObjectOpenHashMap<>();
    public ModelProperties properties;

    public static RawGeometryTree parseHierarchy(RawGeoModel model) {
        RawGeometryTree hierarchy = new RawGeometryTree();
        MinecraftGeometry geometry = model.getMinecraftGeometry()[0];
        hierarchy.properties = geometry.getProperties();
        List<Bone> bones = new ObjectArrayList<>(geometry.getBones());
        Map<String, RawBoneGroup> groups = new HashMap<>();
        for (Bone bone : bones) {
            if (bone.getName() == null || groups.put(bone.getName(), new RawBoneGroup(bone)) != null) {
                throw new IllegalArgumentException("Missing or duplicate bone name: " + bone.getName());
            }
        }
        for (Bone bone : bones) {
            RawBoneGroup group = groups.get(bone.getName());
            if (!hasParent(bone)) {
                hierarchy.topLevelBones.put(bone.getName(), group);
                continue;
            }
            java.util.Set<String> visited = new java.util.HashSet<>();
            Bone ancestor = bone;
            while (hasParent(ancestor)) {
                if (!visited.add(ancestor.getName())) throw new IllegalArgumentException("Cyclic bone hierarchy: " + bone.getName());
                RawBoneGroup parent = groups.get(ancestor.getParent());
                if (parent == null) throw new IllegalArgumentException("Unknown bone parent: " + ancestor.getParent());
                ancestor = parent.selfBone;
            }
            groups.get(bone.getParent()).children.put(bone.getName(), group);
        }
        return hierarchy;
    }

    public static boolean hasParent(Bone bone) {
        return bone.getParent() != null;
    }

    public static RawBoneGroup getGroupFromHierarchy(RawGeometryTree hierarchy, String bone) {
        HashMap<String, RawBoneGroup> flatList = Maps.newHashMap();
        for (RawBoneGroup group : hierarchy.topLevelBones.values()) {
            flatList.put(group.selfBone.getName(), group);
            traverse(flatList, group);
        }
        return flatList.get(bone);
    }

    public static void traverse(HashMap<String, RawBoneGroup> flatList, RawBoneGroup group) {
        for (RawBoneGroup child : group.children.values()) {
            flatList.put(child.selfBone.getName(), child);
            traverse(flatList, child);
        }
    }
}
