package com.stoch.aplugdream.event;

import com.stoch.aplugdream.entity.PoliceEntity;
import com.stoch.aplugdream.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Gestisce la comparsa di agenti di Polizia quando il livello Wanted del player aumenta.
 *
 * Chiamato staticamente da qualsiasi punto in cui il wanted level viene modificato
 * (es: ClientNpcEntity.mobInteract dopo uno scambio).
 *
 * Scala di spawn:
 *   1★ → 1 poliziotto
 *   2★ → 2 poliziotti
 *   3★ → 3 poliziotti
 *   4★ → 5 poliziotti
 *   5★ → 8 poliziotti
 *
 * I poliziotti spawwnano a 15–30 blocchi di distanza dal player.
 */
public class WantedLevelSpawnHandler {

    private static final Random RANDOM = new Random();

    // ──────────────────────────────────────────────────────────────────────────
    // Entry point pubblico
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Da chiamare ogni volta che il wanted level del player aumenta.
     *
     * @param player   Il ServerPlayer interessato
     * @param oldLevel Il livello precedente (0-5)
     * @param newLevel Il nuovo livello (0-5)
     */
    public static void onWantedLevelChanged(ServerPlayer player, int oldLevel, int newLevel) {
        // Spawn solo se il livello è salito e il player è effettivamente ricercato
        if (newLevel <= 0 || newLevel <= oldLevel) return;

        ServerLevel level = player.serverLevel();
        int count = getPoliceCountForLevel(newLevel);

        for (int i = 0; i < count; i++) {
            spawnPoliceNearPlayer(player, level);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Logica interna
    // ──────────────────────────────────────────────────────────────────────────

    /** Quanti poliziotti spawnare per ogni stella */
    private static int getPoliceCountForLevel(int wantedLevel) {
        return switch (wantedLevel) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 5;
            case 5 -> 8;
            default -> 0;
        };
    }

    /**
     * Tenta di spawnare un singolo PoliceEntity a distanza casuale dal player.
     * Prova fino a 10 posizioni per trovare un punto al suolo valido.
     */
    private static void spawnPoliceNearPlayer(ServerPlayer player, ServerLevel level) {
        Vec3 playerPos = player.position();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle    = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = 15 + RANDOM.nextDouble() * 15; // 15–30 blocchi

            double spawnX = playerPos.x + Math.cos(angle) * distance;
            double spawnZ = playerPos.z + Math.sin(angle) * distance;

            // Trova il blocco di terreno nella posizione calcolata
            BlockPos candidate = new BlockPos((int) spawnX, 64, (int) spawnZ);
            BlockPos groundPos = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);

            // Verifica che ci siano almeno due blocchi liberi per l'entità
            if (level.isEmptyBlock(groundPos) && level.isEmptyBlock(groundPos.above())) {
                PoliceEntity police = ModEntityTypes.POLICE.get().create(level);
                if (police != null) {
                    police.moveTo(
                            groundPos.getX() + 0.5,
                            groundPos.getY(),
                            groundPos.getZ() + 0.5,
                            RANDOM.nextFloat() * 360f,
                            0f
                    );
                    police.finalizeSpawn(
                            level,
                            level.getCurrentDifficultyAt(groundPos),
                            MobSpawnType.EVENT,
                            null,
                            null
                    );
                    level.addFreshEntity(police);
                    break; // Spawn riuscito, prossima unità
                }
            }
        }
    }
}