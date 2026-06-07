package com.stoch.aplugdream.entity;

import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.entity.ai.AttackWantedPlayerGoal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * PoliceEntity – l'agente di Polizia della mod A Plug Dream.
 *
 * Meccaniche implementate:
 *  • Aggredisce automaticamente i player con wanted level > 0 (via AttackWantedPlayerGoal)
 *  • Scala velocità e aggressività in base al wanted level del target
 *  • Invia avvertimenti via chat al player inseguito
 *     - 1–2★: avvertimento
 *     - 3–5★: minaccia di arresto
 *  • Rifiuta di parlare (interazione) se il player è ricercato
 *  • Dialogo neutro con player "puliti"
 */
public class PoliceEntity extends PathfinderMob {

    // ──────────────────────────────────────────────────────────────────────────
    // Dialoghi
    // ──────────────────────────────────────────────────────────────────────────

    private static final String[] LOW_WANTED_WARNINGS = {
        "§9[Polizia]§r Fermati! Controllo documenti.",
        "§9[Polizia]§r Ehi tu! Dove stai andando di fretta?",
        "§9[Polizia]§r Non scappare. Devo parlarti, è importante."
    };

    private static final String[] HIGH_WANTED_WARNINGS = {
        "§c[Polizia]§r Sei in arresto! Non fare mosse stupide!",
        "§c[Polizia]§r Fermati o sparo! Ultima avvertenza!",
        "§c[Polizia]§r Troppi guai con la legge. Arrenditi subito!"
    };

    // ──────────────────────────────────────────────────────────────────────────
    // Stato interno
    // ──────────────────────────────────────────────────────────────────────────

    /** Cooldown tra un avvertimento e il successivo (in tick) */
    private int warningCooldown = 0;
    private static final int WARNING_INTERVAL = 120; // 6 secondi

    // ──────────────────────────────────────────────────────────────────────────
    // Costruttore e attributi base
    // ──────────────────────────────────────────────────────────────────────────

    public PoliceEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Goal AI
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Target: aggredisce player con wanted > 0
        this.targetSelector.addGoal(1, new AttackWantedPlayerGoal(this));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tick: avvertimenti e scala velocità
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (this.getTarget() instanceof Player targetPlayer) {
            int wantedLevel = getWantedLevel(targetPlayer);

            // Avvertimento periodico al player inseguito
            if (warningCooldown <= 0) {
                warningCooldown = WARNING_INTERVAL;
                sendWantedWarning(targetPlayer, wantedLevel);
            } else {
                warningCooldown--;
            }

            // Velocità scala con il wanted level (da 0.30 a 0.50 max)
            double speedBoost = 0.30D + (wantedLevel * 0.04D);
            setMovementSpeed(Math.min(speedBoost, 0.50D));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Interazione diretta col player
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.level().isClientSide()) return super.mobInteract(pPlayer, pHand);

        int wantedLevel = getWantedLevel(pPlayer);

        if (wantedLevel >= 3) {
            // Wanted alto: aggressivo immediato
            pPlayer.sendSystemMessage(Component.literal(
                "§c[Polizia]§r Niente chiacchiere. Sei in arresto!"
            ));
            this.setTarget(pPlayer);
        } else if (wantedLevel > 0) {
            // Wanted basso: avvertimento
            pPlayer.sendSystemMessage(Component.literal(
                "§9[Polizia]§r Stai attento. Ti stiamo tenendo d'occhio."
            ));
            this.setTarget(pPlayer);
        } else {
            // Player pulito: dialogo neutro
            pPlayer.sendSystemMessage(Component.literal(
                "§9[Polizia]§r Pattuglia di routine. Niente di speciale qui. Passa una buona giornata."
            ));
        }

        return super.mobInteract(pPlayer, pHand);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper privati
    // ──────────────────────────────────────────────────────────────────────────

    private int getWantedLevel(Player player) {
        AtomicInteger level = new AtomicInteger(0);
        player.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(w -> level.set(w.getWantedLevel()));
        return level.get();
    }

    private void sendWantedWarning(Player player, int wantedLevel) {
        if (wantedLevel >= 3) {
            int idx = (int) (Math.random() * HIGH_WANTED_WARNINGS.length);
            player.sendSystemMessage(Component.literal(HIGH_WANTED_WARNINGS[idx]));
        } else {
            int idx = (int) (Math.random() * LOW_WANTED_WARNINGS.length);
            player.sendSystemMessage(Component.literal(LOW_WANTED_WARNINGS[idx]));
        }
    }

    private void setMovementSpeed(double speed) {
        AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(speed);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Persistenza
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false; // Il poliziotto non sparisce
    }
}