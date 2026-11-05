package com.nivek.morteshadows;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ArmyState extends PersistentState {
    private final Map<String, Integer> captured = new HashMap<>();

    public static final Type<ArmyState> TYPE = new Type<>(
            ArmyState::new,
            ArmyState::fromNbt,
            null
    );

    public static ArmyState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ArmyState state = new ArmyState();
        NbtList list = nbt.getList("captured", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            String key = entry.getString("key");
            int count = entry.getInt("count");
            if (!key.isBlank() && count > 0) state.captured.put(key, count);
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        captured.forEach((key, count) -> {
            NbtCompound entry = new NbtCompound();
            entry.putString("key", key);
            entry.putInt("count", count);
            list.add(entry);
        });
        nbt.put("captured", list);
        return nbt;
    }

    private String key(UUID owner, String entityId) { return owner + "|" + entityId; }

    public void add(UUID owner, String entityId) {
        captured.merge(key(owner, entityId), 1, Integer::sum);
        markDirty();
    }

    public Map<String, Integer> getFor(UUID owner) {
        Map<String, Integer> result = new HashMap<>();
        String prefix = owner + "|";
        captured.forEach((key, count) -> {
            if (key.startsWith(prefix)) result.put(key.substring(prefix.length()), count);
        });
        return result;
    }

    public boolean consume(UUID owner, String entityId) {
        String key = key(owner, entityId);
        int count = captured.getOrDefault(key, 0);
        if (count <= 0) return false;
        if (count == 1) captured.remove(key); else captured.put(key, count - 1);
        markDirty();
        return true;
    }
}
