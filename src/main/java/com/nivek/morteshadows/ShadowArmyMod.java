package com.nivek.morteshadows;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public final class ShadowArmyMod implements ModInitializer {
    private static final String TEAM_NAME = "masked_morte_shadows";
    private static final int MAX_ARMY_PER_PLAYER = 100;

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity player)) return;
            if (!(entity instanceof MobEntity mob)) return;
            if (mob.getCustomName() != null && mob.getCustomName().getString().startsWith("[Sombra]")) return;
            if (mob.getType() == EntityType.PLAYER) return;

            ArmyState state = getState(player.getServer());
            int total = state.getFor(player.getUuid()).values().stream().mapToInt(Integer::intValue).sum();
            if (total >= MAX_ARMY_PER_PLAYER) {
                player.sendMessage(Text.literal("§cSeu exército já atingiu o limite de 100 sombras."), true);
                return;
            }
            Identifier id = Registries.ENTITY_TYPE.getId(mob.getType());
            state.add(player.getUuid(), id.toString());
            player.sendMessage(Text.literal("§8[Sombras] §7" + id.getPath() + " foi capturado! Use §f/shadowarmy list §7para ver seu exército."), true);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal("shadowarmy")
                .then(CommandManager.literal("list").executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    Map<String, Integer> army = getState(player.getServer()).getFor(player.getUuid());
                    if (army.isEmpty()) {
                        context.getSource().sendFeedback(() -> Text.literal("§7Seu exército está vazio. Derrote mobs para capturá-los."), false);
                        return 0;
                    }
                    context.getSource().sendFeedback(() -> Text.literal("§8===== §5Exército das Sombras §8===== "), false);
                    army.forEach((id, count) -> context.getSource().sendFeedback(() -> Text.literal("§7" + id + " §8x §f" + count), false));
                    return army.size();
                }))
                .then(CommandManager.literal("summon").executes(context -> summonAll(context.getSource().getPlayerOrThrow())))
            )
        );
    }

    private static ArmyState getState(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(ArmyState.TYPE, "the_masked_morte_shadow_army");
    }

    private static int summonAll(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ArmyState state = getState(player.getServer());
        Map<String, Integer> army = state.getFor(player.getUuid());
        if (army.isEmpty()) {
            player.sendMessage(Text.literal("§7Você ainda não capturou nenhum mob."), false);
            return 0;
        }

        int summoned = 0;
        for (Map.Entry<String, Integer> entry : army.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || !Registries.ENTITY_TYPE.containsId(id)) continue;
            EntityType<?> type = Registries.ENTITY_TYPE.get(id);
            for (int i = 0; i < entry.getValue(); i++) {
                MobEntity shadow = createMob(type, world);
                if (shadow == null) continue;
                double angle = (summoned * 2.399963229728653) % (Math.PI * 2);
                double radius = 1.8 + (summoned % 4) * 0.65;
                shadow.refreshPositionAndAngles(
                    player.getX() + Math.cos(angle) * radius,
                    player.getY(),
                    player.getZ() + Math.sin(angle) * radius,
                    player.getYaw(), 0
                );
                shadow.setPersistent();
                shadow.setCustomName(Text.literal("[Sombra] " + id.getPath()));
                shadow.setCustomNameVisible(true);
                shadow.setCanPickUpLoot(false);
                if (world.spawnEntity(shadow)) {
                    state.consume(player.getUuid(), entry.getKey());
                    summoned++;
                }
            }
        }
        final int total = summoned;
        player.sendMessage(Text.literal("§8[Sombras] §f" + total + " guerreiro(s) invocado(s)."), false);
        return total;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MobEntity createMob(EntityType<?> type, ServerWorld world) {
        return (MobEntity) ((EntityType) type).create(world);
    }
}
