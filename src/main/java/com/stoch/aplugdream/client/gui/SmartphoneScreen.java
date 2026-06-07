package com.stoch.aplugdream.client.gui;

import com.stoch.aplugdream.capability.PlayerBankProvider;
import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.client.ClientPhoneData;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.network.packet.DepositMoneyC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SmartphoneScreen extends Screen {
    private int balance = 0;
    private int wantedLevel = 0;

    // Tabs
    private enum Tab { HOME, BANK, MESSAGES }
    private Tab currentTab = Tab.HOME;

    // Colors
    private static final int BG_COLOR = 0xDD1A1A2E;
    private static final int HEADER_COLOR = 0xFF16213E;
    private static final int ACCENT_COLOR = 0xFF0F3460;
    private static final int TEXT_PRIMARY = 0xE0E0E0;
    private static final int TEXT_ACCENT = 0x00D4FF;
    private static final int WANTED_COLOR = 0xFF4444;
    private static final int MONEY_GREEN = 0x00FF88;

    public SmartphoneScreen() {
        super(Component.translatable("gui.aplugdream.smartphone"));
    }

    @Override
    protected void init() {
        super.init();
        refreshData();

        int phoneW = 180;
        int phoneH = 260;
        int px = (width - phoneW) / 2;
        int py = (height - phoneH) / 2;

        // Tab buttons at bottom of phone
        int tabY = py + phoneH - 28;
        int tabW = 56;

        this.addRenderableWidget(Button.builder(Component.literal("\u2302 Home"), button -> {
            currentTab = Tab.HOME;
            rebuildWidgets();
        }).bounds(px + 4, tabY, tabW, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("$ Bank"), button -> {
            currentTab = Tab.BANK;
            rebuildWidgets();
        }).bounds(px + 62, tabY, tabW, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("\u2709 Msgs"), button -> {
            currentTab = Tab.MESSAGES;
            ClientPhoneData.markAllRead();
            rebuildWidgets();
        }).bounds(px + 120, tabY, tabW, 20).build());

        // Bank tab: deposit button
        if (currentTab == Tab.BANK) {
            this.addRenderableWidget(Button.builder(Component.literal("Deposit All Clean Money"), button -> {
                ModMessages.sendToServer(new DepositMoneyC2SPacket());
                refreshData();
            }).bounds(px + 10, py + 130, phoneW - 20, 20).build());
        }
    }

    private void refreshData() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(bank -> {
                balance = bank.getBalance();
            });
            minecraft.player.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(wanted -> {
                wantedLevel = wanted.getWantedLevel();
            });
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        refreshData();

        int phoneW = 180;
        int phoneH = 260;
        int px = (width - phoneW) / 2;
        int py = (height - phoneH) / 2;

        // Phone body (dark background)
        g.fill(px - 2, py - 2, px + phoneW + 2, py + phoneH + 2, 0xFF333333);
        g.fill(px, py, px + phoneW, py + phoneH, BG_COLOR);

        // Header bar
        g.fill(px, py, px + phoneW, py + 22, HEADER_COLOR);
        g.drawCenteredString(this.font, "\u00A7bA Plug Dream", px + phoneW / 2, py + 7, TEXT_PRIMARY);

        // Wanted stars in header
        if (wantedLevel > 0) {
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < wantedLevel; i++) stars.append("\u2605");
            for (int i = wantedLevel; i < 5; i++) stars.append("\u2606");
            g.drawString(this.font, "\u00A7c" + stars, px + 4, py + 7, WANTED_COLOR);
        }

        // Content area
        int contentY = py + 28;

        switch (currentTab) {
            case HOME -> renderHome(g, px, contentY, phoneW);
            case BANK -> renderBank(g, px, contentY, phoneW);
            case MESSAGES -> renderMessages(g, px, contentY, phoneW);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderHome(GuiGraphics g, int px, int startY, int phoneW) {
        g.drawCenteredString(this.font, "\u00A7l\u00A7bSMARTPHONE", px + phoneW / 2, startY + 5, TEXT_PRIMARY);
        g.drawCenteredString(this.font, "\u00A7fWelcome, Plug.", px + phoneW / 2, startY + 25, TEXT_PRIMARY);

        // Balance summary
        g.drawString(this.font, "\u00A7aBank: \u00A7f$" + balance, px + 10, startY + 50, MONEY_GREEN);

        // Wanted level summary
        String wantedText = wantedLevel == 0 ? "\u00A72Clean" : "\u00A7c" + wantedLevel + " Star" + (wantedLevel > 1 ? "s" : "");
        g.drawString(this.font, "\u00A7eWanted: " + wantedText, px + 10, startY + 65, TEXT_PRIMARY);

        // Unread messages
        int unread = ClientPhoneData.getUnreadCount();
        if (unread > 0) {
            g.drawString(this.font, "\u00A7c\u2709 " + unread + " new message" + (unread > 1 ? "s" : ""), px + 10, startY + 85, TEXT_PRIMARY);
        } else {
            g.drawString(this.font, "\u00A77No new messages", px + 10, startY + 85, TEXT_PRIMARY);
        }

        // Status
        g.drawString(this.font, "\u00A78---", px + 10, startY + 105, TEXT_PRIMARY);
        g.drawString(this.font, "\u00A77Stay low. Stay smart.", px + 10, startY + 120, TEXT_PRIMARY);
    }

    private void renderBank(GuiGraphics g, int px, int startY, int phoneW) {
        g.drawCenteredString(this.font, "\u00A7l\u00A7a$ BANK APP", px + phoneW / 2, startY + 5, TEXT_PRIMARY);

        // Balance display
        g.fill(px + 10, startY + 25, px + phoneW - 10, startY + 70, ACCENT_COLOR);
        g.drawCenteredString(this.font, "\u00A7fCurrent Balance", px + phoneW / 2, startY + 30, TEXT_PRIMARY);
        g.drawCenteredString(this.font, "\u00A7a\u00A7l$" + balance, px + phoneW / 2, startY + 48, MONEY_GREEN);

        g.drawString(this.font, "\u00A77Deposit your Clean Money", px + 10, startY + 80, TEXT_PRIMARY);
        g.drawString(this.font, "\u00A77to your bank account.", px + 10, startY + 92, TEXT_PRIMARY);
    }

    private void renderMessages(GuiGraphics g, int px, int startY, int phoneW) {
        g.drawCenteredString(this.font, "\u00A7l\u00A7e\u2709 MESSAGES", px + phoneW / 2, startY + 5, TEXT_PRIMARY);

        List<String> messages = ClientPhoneData.getMessages();
        if (messages.isEmpty()) {
            g.drawCenteredString(this.font, "\u00A77No messages yet.", px + phoneW / 2, startY + 40, TEXT_PRIMARY);
            g.drawCenteredString(this.font, "\u00A78Wait for clients to", px + phoneW / 2, startY + 55, TEXT_PRIMARY);
            g.drawCenteredString(this.font, "\u00A78contact you...", px + phoneW / 2, startY + 65, TEXT_PRIMARY);
        } else {
            int y = startY + 22;
            int maxDisplay = Math.min(messages.size(), 8);
            for (int i = 0; i < maxDisplay; i++) {
                String msg = messages.get(i);
                // Truncate if too long
                if (msg.length() > 28) msg = msg.substring(0, 25) + "...";
                g.fill(px + 8, y - 1, px + phoneW - 8, y + 11, 0x44FFFFFF);
                g.drawString(this.font, "\u00A7f" + msg, px + 12, y + 1, TEXT_PRIMARY);
                y += 15;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
