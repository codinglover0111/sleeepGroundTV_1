package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GameRemainTime {
    private static int remainingTicks = -1; // 남은 시간 (틱 단위)
    private static MinecraftServer serverInstance;

    // 타이머 시작
    public static void startTimer(MinecraftServer server, int seconds) {
        serverInstance = server;
        remainingTicks = seconds * 20; // 초를 틱 단위로 변환
        MinecraftForge.EVENT_BUS.register(GameRemainTime.class); // 이벤트 등록
    }

    // 매 틱마다 실행 (1초마다 액션바 업데이트)
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            if (remainingTicks % 20 == 0) { // 1초마다 갱신
                int minutes = (remainingTicks / 20) / 60;
                int seconds = (remainingTicks / 20) % 60;
                sendActionBarToAll(serverInstance, "§e남은시간: §f" + minutes + "분 " + seconds + "초");
            }

            remainingTicks--;

            if (remainingTicks == 0) {
                sendActionBarToAll(serverInstance, "§c시간 종료!");
                MinecraftForge.EVENT_BUS.unregister(GameRemainTime.class); // 이벤트 해제
            }
        }
    }

    // 모든 플레이어에게 액션바 전송
    private static void sendActionBarToAll(MinecraftServer server, String message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendActionBar(player, message);
        }
    }

    // 특정 플레이어에게 액션바 전송
    public static void sendActionBar(ServerPlayer player, String message) {
        player.connection.send(new ClientboundSetActionBarTextPacket(Component.literal(message)));
    }

    public static void clearActionBarToAll(MinecraftServer server) {
        MinecraftForge.EVENT_BUS.unregister(GameRemainTime.class); // 이벤트 해제
        sendActionBarToAll(server, "§e 게임종료!");
    }
}
