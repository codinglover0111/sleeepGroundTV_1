package com.example.examplemod;

import com.example.examplemod.ExampleMod;
import com.mojang.logging.LogUtils;


import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;


@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class GameManager {
    // 게임 진행 시간
    private static final int GAME_DURATION_TICKS = 20 * 300;

    private static int gameTimer = 0;

    private static boolean gameActive = false;

    private static UUID hunterUUID;
   
    public static final List<UUID> survivors = new ArrayList<>();


    public static void startGame(List<ServerPlayer> players) {
        if (players.size() < 2) {
            // 플레이어가 2명 미만일 경우 게임 시작 불가
            for (ServerPlayer player : players) {
                player.sendSystemMessage(Component.literal("게임을 시작하려면 최소 2명의 플레이어가 필요합니다."));
            }
            ExampleMod.LOGGER.info("게임 시작 실패: 플레이어 수 부족");
            return;
        }

        // 무작위로 술래 선정
        Random rand = new Random();
        int hunterIndex = rand.nextInt(players.size());
        ServerPlayer hunter = players.get(hunterIndex);
        hunterUUID = hunter.getUUID();
        ExampleMod.hunter = hunter;

        // 나머지 플레이어들을 생존자로 설정
        for (ServerPlayer player : players) {
            if (!player.getUUID().equals(hunterUUID)) {
                survivors.add(player.getUUID());
            }
        }

        // 게임 시작 알림
        for (ServerPlayer player : players) {
            player.sendSystemMessage(Component.literal("술래잡기가 시작되었습니다! 술래: " + hunter.getName().getString()));
        }
        ExampleMod.LOGGER.info("게임 시작: 술래 - " + hunter.getName().getString());

        // 게임 타이머 초기화
        gameActive = true;
        gameTimer = GAME_DURATION_TICKS;


    }

        // 게임 종료
        public static void endGame(String reason) {
            gameActive = false;
            hunterUUID = null;
            survivors.clear();
            gameTimer = 0;

            // 게임 종료 메시지
            ExampleMod.LOGGER.info("게임 종료: " + reason);
        }

    // 게임 진행 체크 (서버 틱마다 실행)
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!gameActive)
            return;

        gameTimer--;
        if (gameTimer <= 0) {
            endGame("시간 초과! 생존자 승리!");
        }
    }
    
        // 플레이어 잡기 기능 (술래가 생존자를 잡았을 때)
    public static void catchSurvivor(ServerPlayer catcher, ServerPlayer target) {
        if (!gameActive || !catcher.getUUID().equals(hunterUUID)) return;
        if (!survivors.contains(target.getUUID())) return;

        survivors.remove(target.getUUID());
        target.sendSystemMessage(Component.literal("당신은 술래에게 잡혔습니다!"));

        if (survivors.isEmpty()) {
            endGame("모든 생존자가 잡혔습니다! "+ catcher.getName().getString() + " 승리!");
        }
    }

}
