package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GameStartNotifier {

    private static int countdownTicks = -1; // 남은 카운트다운 틱 수
    private static Runnable onCountdownEnd = null;
    private static MinecraftServer serverInstance;
    private static boolean catcher_shown = false;

    public static void startCountdown(MinecraftServer server, Runnable callback) {
        serverInstance = server;
        countdownTicks = 7 * 20; // 7초 (1초 = 20틱)
        onCountdownEnd = callback;
        MinecraftForge.EVENT_BUS.register(GameStartNotifier.class); // 이벤트 등록
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && countdownTicks > 0) {
            // 2초 동안 술래 이름을 한번만 보여줌
            // 카운트 다운이 시작되고 2초가 지나기 전까지
            if (countdownTicks < 7 * 20) {
                // 2초가 지나고 5초가 지나기 전까지
                if (countdownTicks > 5 * 20) {
                    // 술래 이름을 보여줌
                    if (!catcher_shown) {
                        sendCatcherName(serverInstance);
                        catcher_shown = true;
                    }
                }
            }
            // 카운트 다운 시작
            // 초마다 메시지를 보여줌
            // 카운트 다운이 5초 남았을 때
            if (countdownTicks % 20 == 0 && countdownTicks <= 5 * 20) {
                int secondsLeft = countdownTicks / 20;
                sendGameStartMessage(
                    serverInstance,
                    "게임 시작까지 " + secondsLeft + "초",
                    "모두 준비하세요!"
                );
            }
            // 카운트 다운 종료
            if (countdownTicks <= 1) {
                onCountdownEnd.run();
                catcher_shown = false;
                sendGameStartMessage(serverInstance, "게임이 시작되었습니다!", "행운을 빕니다!");
                MinecraftForge.EVENT_BUS.unregister(GameStartNotifier.class); // 이벤트 등록 해제
            }
            // 주의 : 카운트 다운 틱 수를 감소시킵니다.
            // 건들지 마세요.
            countdownTicks--;
        }
    }

    public static void sendGameEndMessage(ServerPlayer player, String title, String subtitle) {
        sendTitle(player, title, subtitle);
    }

    private static void sendGameStartMessage(
        MinecraftServer server,
        String title,
        String subtitle
    ) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    private static void sendCatcherName(MinecraftServer server) {
        String hunterName = ExampleMod.hunter.getName().getString();
        for (ServerPlayer player : serverInstance.getPlayerList().getPlayers()) {
            if (player != ExampleMod.hunter) {
                sendTitle(player, "§c" + hunterName + "님이 술래입니다!", "§f조심하세요!");
            } else {
                sendTitle(player, "§a당신이 술래입니다.", "§f즐겁게 플레이하세요!");
            }
        }
    }

    private static void sendTitle(ServerPlayer player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 2 * 20);
    }

    private static void sendTitle(
        ServerPlayer player,
        String title,
        String subtitle,
        int duration
    ) {
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, duration, 10));
    }
}
