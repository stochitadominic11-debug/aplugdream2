package com.stoch.aplugdream.entity.ai;

import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.entity.ClientNpcEntity;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.network.packet.SyncPhoneMessageS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI Goal: il ClientNpc si avvicina attivamente al player più vicino
 * e gli invia un messaggio sullo smartphone, simulando un contatto.
 *
 * - Non si avvicina a player con wanted ≥ 4 stelle (troppo pericoloso)
 * - Usa messaggi diversi per clienti nuovi vs clienti abituali
 * - Ha un cooldown per non spammare messaggi
 */
public class NpcApproachPlayerGoal extends Goal {

    private final ClientNpcEntity npc;
    private Player targetPlayer;

    /** Quante tick aspettare prima di poter riattivarsi dopo aver contattato un player */
    private int goalCooldown = 0;
    private boolean hasMessaged = false;

    private static final int GOAL_COOLDOWN_TICKS = 400; // 20 secondi
    private static final double SPOT_RANGE      = 18.0D;  // raggio in cui l'NPC "vede" un player
    private static final double MESSAGE_RANGE   = 4.5D;   // distanza sotto cui invia il messaggio

    public NpcApproachPlayerGoal(ClientNpcEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        if (goalCooldown > 0) {
            goalCooldown--;
            return false;
        }

        Player nearest = npc.level().getNearestPlayer(npc, SPOT_RANGE);
        if (nearest == null || !nearest.isAlive()) return false;

        // Non avvicinarsi a player già "caldi" con la polizia
        if (isPlayerTooHot(nearest)) return false;

        this.targetPlayer = nearest;
        this.hasMessaged  = false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPlayer == null || !targetPlayer.isAlive()) return false;
        if (hasMessaged) return false; // già contattato, smettila di seguire
        return npc.distanceToSqr(targetPlayer) <= (SPOT_RANGE * SPOT_RANGE * 1.5);
    }

    @Override
    public void start() {
        npc.getNavigation().moveTo(targetPlayer, 1.0D);
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;
        npc.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);

        double distSq = npc.distanceToSqr(targetPlayer);

        if (distSq < MESSAGE_RANGE * MESSAGE_RANGE) {
            // Abbastanza vicino: invia messaggio sullo smartphone
            if (!npc.level().isClientSide() && targetPlayer instanceof ServerPlayer sp && !hasMessaged) {
                String msg = npc.isRegularCustomer(sp.getUUID())
                        ? npc.getRegularApproachMessage()
                        : npc.getApproachMessage();
                ModMessages.sendToPlayer(new SyncPhoneMessageS2CPacket(msg), sp);
                hasMessaged   = true;
                goalCooldown  = GOAL_COOLDOWN_TICKS;
            }
        } else {
            // Non ancora abbastanza vicino: continua ad avvicinarsi
            npc.getNavigation().moveTo(targetPlayer, 1.0D);
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
        npc.getNavigation().stop();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────────

    /** Ritorna true se il player ha troppi stelline (≥4) — l'NPC evita il rischio */
    private boolean isPlayerTooHot(Player player) {
        boolean[] hot = {false};
        player.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(w -> {
            if (w.getWantedLevel() >= 4) hot[0] = true;
        });
        return hot[0];
    }
}