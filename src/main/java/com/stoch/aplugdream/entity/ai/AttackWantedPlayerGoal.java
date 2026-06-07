package com.stoch.aplugdream.entity.ai;

import com.stoch.aplugdream.capability.PlayerWantedProvider;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AttackWantedPlayerGoal – fa sì che il PoliceEntity insegua i player con wanted > 0.
 *
 * Il raggio di ricerca si espande con il livello Wanted:
 *   1★ → 48 blocchi
 *   3★ → 64 blocchi
 *   5★ → 80 blocchi
 */
public class AttackWantedPlayerGoal extends TargetGoal {

    private Player targetPlayer;

    private static final double BASE_RANGE = 48.0D;

    public AttackWantedPlayerGoal(PathfinderMob mob) {
        super(mob, false, true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public boolean canUse() {
        // Il raggio di pattuglia dipende dal wanted del player più vicino
        double searchRange = BASE_RANGE;
        AABB searchArea = this.mob.getBoundingBox().inflate(searchRange);
        List<Player> players = this.mob.level().getEntitiesOfClass(Player.class, searchArea);

        double closestDist = Double.MAX_VALUE;
        Player closest     = null;

        for (Player player : players) {
            AtomicInteger wantedLevel = new AtomicInteger(0);
            player.getCapability(PlayerWantedProvider.PLAYER_WANTED)
                  .ifPresent(w -> wantedLevel.set(w.getWantedLevel()));

            if (wantedLevel.get() > 0) {
                // Espandiamo il raggio in base al wanted
                double expandedRange = BASE_RANGE + (wantedLevel.get() * 8.0D);
                double dist = this.mob.distanceToSqr(player);
                if (dist <= expandedRange * expandedRange && dist < closestDist) {
                    closestDist = dist;
                    closest     = player;
                }
            }
        }

        if (closest != null) {
            this.targetPlayer = closest;
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetPlayer);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPlayer == null || !targetPlayer.isAlive()) return false;

        AtomicBoolean stillWanted = new AtomicBoolean(false);
        targetPlayer.getCapability(PlayerWantedProvider.PLAYER_WANTED)
                    .ifPresent(w -> { if (w.getWantedLevel() > 0) stillWanted.set(true); });

        return stillWanted.get() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        targetPlayer = null;
        super.stop();
    }
}