package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.utilitymenu.UtilityMenu;
import net.kraftattacke.bully.utilitymenu.UtilityMenuPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.platform.InputConstants;

/** The original three layouts and textures, rendered through the 26.3 extraction API. */
public final class UtilityMenuScreen extends AbstractContainerScreen<UtilityMenu> {
    private EditBox command;
    private CommandSuggestions commandSuggestions;

    public UtilityMenuScreen(UtilityMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, switch (menu.page) {
            case MAIN -> 190;
            case COMMAND -> 166;
            case POTION -> 116;
        });
    }

    @Override
    protected void init() {
        String previousCommand = command == null ? "" : command.getValue();
        command = null;
        commandSuggestions = null;
        super.init();
        switch (menu.page) {
            case MAIN -> {
                button(7, "themenugui.button_empty", 61, 78, 54);
                arrow(11, "<", 4, 6);   // left -> effect page
                arrow(10, ">", 156, 6); // right -> command page
            }
            case POTION -> {
                button(0, "potiongui.button_quick_mine", 8, 22, 78);   // Quick Mine (haste)
                button(1, "potiongui.button_quick_mine1", 90, 22, 78); // Inf Saturation
                button(2, "potiongui.button_quick_mine2", 8, 44, 78);  // Night Vision
                button(3, "potiongui.button_quick_mine3", 90, 44, 78); // Invisibility
                button(4, "potiongui.button_quick_mine4", 8, 66, 78);  // Insta Kill (strength)
                button(5, "potiongui.button_quick_mine5", 90, 66, 78); // Poseidon (water effects)
                button(8, "themenugui.label_semigod", 8, 88, 78);      // Semi-God
                button(6, "potiongui.button_quick_mine6", 90, 88, 78); // Turn all off
                arrow(7, ">", 152, 4); // right -> back to main page
            }
            case COMMAND -> {
                command = new EditBox(font, leftPos + 4, topPos + 28, 167, 18,
                        text("command_gui.servercommands"));
                command.setMaxLength(32767);
                command.setHint(text("command_gui.servercommands"));
                addRenderableWidget(command);
                // Vanilla chat's command completion: suggestion popup plus Tab to auto-fill.
                commandSuggestions = new CommandSuggestions(minecraft, this, command, font, true, false, 1, 10, false, -805306368);
                command.setResponder(value -> commandSuggestions.updateCommandInfo());
                command.setValue(previousCommand);
                commandSuggestions.setAllowSuggestions(true);
                commandSuggestions.updateCommandInfo();
                button(0, "command_gui.button_execute_com", 41, 52, 93);
                arrow(1, "<", 4, 4);
            }
        }
    }

    private static Component text(String key) { return Component.translatable("gui.utilitymenu." + key); }

    private void send(int button) {
        ClientPlayNetworking.send(new UtilityMenuPayload(menu.containerId, button,
                command == null ? "" : command.getValue()));
    }

    private void button(int id, String key, int x, int y, int width) {
        addRenderableWidget(Button.builder(text(key), button -> send(id))
                .bounds(leftPos + x, topPos + y, width, 20).build());
    }

    private void arrow(int id, String glyph, int x, int y) {
        addRenderableWidget(Button.builder(Component.literal(glyph), button -> send(id))
                .bounds(leftPos + x, topPos + y, 16, 16).build());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        panel(graphics);
        slotBackgrounds(graphics);
        super.extractContents(graphics, mouseX, mouseY, delta);
        if (commandSuggestions != null) {
            commandSuggestions.extractRenderState(graphics, mouseX, mouseY);
        }
        extractTooltip(graphics, mouseX, mouseY);
    }

    /** A plain vanilla-grey bevelled panel, so the menu is cut to its content instead of a wide custom texture. */
    private void panel(GuiGraphicsExtractor graphics) {
        int x = leftPos, y = topPos, w = imageWidth, h = imageHeight;
        graphics.fill(x, y, x + w, y + h, 0xFF000000);                 // outer border
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6); // base grey
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFFFFFFFF);     // top highlight
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFFFFFFFF);     // left highlight
        graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF555555); // right shadow
        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF555555); // bottom shadow
    }

    /** Inset vanilla-style slot behind every slot: grey centre, dark top/left edge, light bottom/right edge. */
    private void slotBackgrounds(GuiGraphicsExtractor graphics) {
        for (var slot : menu.slots) {
            int x = leftPos + slot.x - 1, y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, 0xFF373737);         // dark: leaves the top + left border
            graphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF); // light: leaves the bottom + right border
            graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B); // grey centre
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (commandSuggestions != null && commandSuggestions.mouseScrolled(scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (commandSuggestions != null && commandSuggestions.mouseClicked(event)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // "Bully Menu" title: centred, flat grey, no shadow (same look as before, just moved to the middle).
        Component title = text("themenugui.label_utility_menu_v4");
        graphics.text(font, title, (imageWidth - font.width(title)) / 2, 6, -12829636, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Tab completion, suggestion navigation and hiding the popup take priority.
        if (commandSuggestions != null && commandSuggestions.keyPressed(event)) {
            return true;
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        // Submit the command on Enter (normal or numpad) as well as the Execute button.
        if (menu.page == UtilityMenu.Page.COMMAND
                && (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER)) {
            send(0);
            return true;
        }
        if (command != null && command.isFocused()) return command.keyPressed(event);
        return super.keyPressed(event);
    }
}
