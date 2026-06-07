package com.stoch.aplugdream.entity;

import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.entity.ai.NpcApproachPlayerGoal;
import com.stoch.aplugdream.event.WantedLevelSpawnHandler;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.network.packet.SyncPhoneMessageS2CPacket;
import com.stoch.aplugdream.network.packet.SyncWantedLevelS2CPacket;
import com.stoch.aplugdream.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * ClientNpcEntity – il "Cliente" della mod A Plug Dream.
 *
 * Meccaniche implementate:
 *  • Passeggia nei villaggi (spawn gestito via biome modifier)
 *  • Riconosce il player (memoria per UUID → cliente abituale vs nuovo)
 *  • Si avvicina attivamente al player tramite NpcApproachPlayerGoal
 *  • Contatta il player sullo smartphone custom con messaggi diversificati
 *  • Scambia white_powder → dirty_money e aumenta il wanted level del player
 *  • Rifiuta lo scambio se il player ha wanted ≥ 4 (troppo rischioso)
 *  • Nome casuale persistente via NBT
 */
public class ClientNpcEntity extends PathfinderMob {

    // ──────────────────────────────────────────────────────────────────────────
    // Identità NPC
    // ──────────────────────────────────────────────────────────────────────────

    private String npcName = "";

    private static final String[] NPC_NAMES = {
        "Diego", "Marco", "Luca", "Tony", "Riccardo",
        "Salvatore", "Franco", "Bruno", "Enzo", "Vito"
    };

    // ──────────────────────────────────────────────────────────────────────────
    // Memoria player
    // ──────────────────────────────────────────────────────────────────────────

    /** UUID dei player che hanno già fatto almeno uno scambio con questo NPC */
    private final Set<UUID> regularCustomers = new HashSet<>();

    // ──────────────────────────────────────────────────────────────────────────
    // Sistema di richiesta periodica (timer)
    // ──────────────────────────────────────────────────────────────────────────

    private int demandCooldown = 0;
    /** ~5 minuti a 20 TPS */
    private static final int DEMAND_INTERVAL = 6000;

    // ──────────────────────────────────────────────────────────────────────────
    // Messaggi localizzati in italiano
    // ──────────────────────────────────────────────────────────────────────────

    private static final String[] FIRST_APPROACH_MESSAGES = {
        "%s: Ehi amico, ho bisogno di roba. Sai dove prendere qualcosa?",
        "%s: Psst... ho sentito che puoi procurarmi qualcosa. È vero?",
        "%s: Ho bisogno di prodotto. Puoi aiutarmi? Pago bene."
    };

    private static final String[] REGULAR_APPROACH_MESSAGES = {
        "%s: Sei tu! Ho bisogno di rifornimento, come al solito.",
        "%s: Amico mio, ho finito la scorta. Hai qualcosa per me?",
        "%s: Buon vederti di nuovo. Sei pronto con la merce?"
    };

    private static final String[] TRADE_CONFIRM_MESSAGES = {
        "%s: Perfetto. Ecco i soldi, come concordato. Sei il migliore.",
        "%s: Affare fatto. A presto, amico.",
        "%s: Ottima roba come sempre. Conto su di te."
    };

    private static final String[] DEMAND_MESSAGES = {
        "%s: Dove sei? Ho finito la scorta, muoviti.",
        "%s: Non farmi aspettare ancora, ho bisogno di prodotto.",
        "%s: Svegliati, sono in astinenza. Portami la roba!"
    };

    // ──────────────────────────────────────────────────────────────────────────
    // Costruttore e attributi
    // ──────────────────────────────────────────────────────────────────────────

    public ClientNpcEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Goal AI
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        // NpcApproachPlayerGoal ha priorità alta: il cliente si avvicina attivamente
        this.goalSelector.addGoal(2, new NpcApproachPlayerGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tick: richiesta periodica tramite telefono
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            demandCooldown++;
            if (demandCooldown >= DEMAND_INTERVAL) {
                demandCooldown = 0;
                Player nearest = this.level().getNearestPlayer(this, 64.0D);
                if (nearest instanceof ServerPlayer serverPlayer) {
                    String msg = String.format(
                        DEMAND_MESSAGES[new Random().nextInt(DEMAND_MESSAGES.length)],
                        getNpcName()
                    );
                    ModMessages.sendToPlayer(new SyncPhoneMessageS2CPacket(msg), serverPlayer);
                }
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Interazione con il player (scambio merce)
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.level().isClientSide()) return super.mobInteract(pPlayer, pHand);
        if (!(pPlayer instanceof ServerPlayer serverPlayer)) return super.mobInteract(pPlayer, pHand);

        // Controlla se il player è troppo "caldo" (wanted ≥ 4)
        if (isPlayerTooHot(serverPlayer)) {
            pPlayer.sendSystemMessage(Component.literal(
                "§c[" + getNpcName() + "]§r Stai scherzando?! Sei circondato dalla polizia! Vattene!"
            ));
            return InteractionResult.FAIL;
        }

        ItemStack held = pPlayer.getItemInHand(pHand);

        if (held.is(ModItems.WHITE_POWDER.get())) {
            return handleTrade(serverPlayer, held);
        } else {
            // Nessun prodotto: dialogo diverso per clienti nuovi vs abituali
            String response = regularCustomers.contains(pPlayer.getUUID())
                ? "§e[" + getNpcName() + "]§r Dai, sai cosa mi serve. Portami la roba!"
                : "§e[" + getNpcName() + "]§r Ho bisogno di prodotto... ce l'hai?";
            pPlayer.sendSystemMessage(Component.literal(response));
            return InteractionResult.SUCCESS;
        }
    }

    /** Gestisce lo scambio effettivo white_powder → dirty_money */
    private InteractionResult handleTrade(ServerPlayer serverPlayer, ItemStack held) {
        int count   = held.getCount();
        int payment = count * 3; // 1 white_powder = 3 dirty_money

        held.shrink(count);
        serverPlayer.addItem(new ItemStack(ModItems.DIRTY_MONEY.get(), payment));

        // Segna il player come cliente abituale
        regularCustomers.add(serverPlayer.getUUID());

        // Aumenta il wanted level e spawna eventuale polizia
        serverPlayer.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(wanted -> {
            int oldLevel = wanted.getWantedLevel();
            wanted.addWantedLevel(1);
            int newLevel = wanted.getWantedLevel();
            ModMessages.sendToPlayer(new SyncWantedLevelS2CPacket(newLevel), serverPlayer);

            // Spawna polizia se il wanted è salito
            if (newLevel > oldLevel) {
                WantedLevelSpawnHandler.onWantedLevelChanged(serverPlayer, oldLevel, newLevel);
            }
        });

        // Conferma via smartphone
        String tradeMsg = String.format(
            TRADE_CONFIRM_MESSAGES[new Random().nextInt(TRADE_CONFIRM_MESSAGES.length)],
            getNpcName()
        );
        ModMessages.sendToPlayer(new SyncPhoneMessageS2CPacket(tradeMsg), serverPlayer);

        // Reset del timer di richiesta dopo uno scambio riuscito
        demandCooldown = 0;
        return InteractionResult.SUCCESS;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Metodi pubblici usati da NpcApproachPlayerGoal
    // ──────────────────────────────────────────────────────────────────────────

    /** Messaggio di primo contatto (nuovo cliente) */
    public String getApproachMessage() {
        return String.format(
            FIRST_APPROACH_MESSAGES[new Random().nextInt(FIRST_APPROACH_MESSAGES.length)],
            getNpcName()
        );
    }

    /** Messaggio per cliente già conosciuto */
    public String getRegularApproachMessage() {
        return String.format(
            REGULAR_APPROACH_MESSAGES[new Random().nextInt(REGULAR_APPROACH_MESSAGES.length)],
            getNpcName()
        );
    }

    /** Controlla se questo player ha già tradato con l'NPC */
    public boolean isRegularCustomer(UUID playerUUID) {
        return regularCustomers.contains(playerUUID);
    }

    /** Ritorna il nome dell'NPC (casuale, persistente via NBT) */
    public String getNpcName() {
        if (npcName.isEmpty()) {
            npcName = NPC_NAMES[new Random().nextInt(NPC_NAMES.length)];
        }
        return npcName;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper privato
    // ──────────────────────────────────────────────────────────────────────────

    private boolean isPlayerTooHot(Player player) {
        boolean[] hot = {false};
        player.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(w -> {
            if (w.getWantedLevel() >= 4) hot[0] = true;
        });
        return hot[0];
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NBT: persistenza su disco
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("NpcName", getNpcName());
        pCompound.putInt("DemandCooldown", demandCooldown);

        ListTag customerList = new ListTag();
        for (UUID uuid : regularCustomers) {
            customerList.add(StringTag.valueOf(uuid.toString()));
        }
        pCompound.put("RegularCustomers", customerList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("NpcName")) {
            npcName = pCompound.getString("NpcName");
        }
        if (pCompound.contains("DemandCooldown")) {
            demandCooldown = pCompound.getInt("DemandCooldown");
        }
        if (pCompound.contains("RegularCustomers")) {
            ListTag list = pCompound.getList("RegularCustomers", Tag.TAG_STRING);
            regularCustomers.clear();
            for (int i = 0; i < list.size(); i++) {
                try {
                    regularCustomers.add(UUID.fromString(list.getString(i)));
                } catch (IllegalArgumentException ignored) { /* UUID corrotto, skip */ }
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false; // Non despawna mai (cliente fedele!)
    }
}