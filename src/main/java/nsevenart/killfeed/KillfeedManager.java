package nsevenart.killfeed;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class KillfeedManager {

    private static final Identifier KILLFEED_PACKET_ID = new Identifier("killfeed", "killfeed_packet");

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (killedEntity instanceof LivingEntity && entity instanceof PlayerEntity) {
                onEntityKilled((PlayerEntity) entity, killedEntity, world);
            }
        });
    }

    private static void onEntityKilled(PlayerEntity killer, Entity victim, World world) {
        String killerName = killer.getEntityName();
        String victimName = victim.getDisplayName().getString();

        // Определяем, связано ли событие с текущим игроком
        boolean isRelevantToPlayer = killer.equals(world.getClosestPlayer(killer, 0)) || victim.equals(world.getClosestPlayer(killer, 0));

        // Отправляем информацию всем игрокам
        for (PlayerEntity player : world.getPlayers()) {
            if (player instanceof ServerPlayerEntity) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeString(killerName);
                buf.writeString(victimName);
                buf.writeBoolean(killer.equals(player) || victim.equals(player)); // Проверка для рамки

                ServerPlayNetworking.send((ServerPlayerEntity) player, KILLFEED_PACKET_ID, buf);
            }
        }
    }
}
