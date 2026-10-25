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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.platform.InputConstants;

/** The original three layouts and textures, rendered through the 26.3 extraction API. */
public final class UtilityMenuScreen extends AbstractContainerScreen<UtilityMenu> {
    private final Identifier texture;
    private EditBox command;
    private CommandSuggestions commandSuggestions;

    public UtilityMenuScreen(UtilityMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.page == UtilityMenu.Page.MAIN ? 300 : 176,
                menu.page == UtilityMenu.Page.MAIN ? 173 : 166);
        texture = Identifier.fromNamespaceAndPath("utilitymenu", "textures/screens/" + switch (menu.page) {
            case MAIN -> "themenugui";
            case COMMAND -> "command_gui";
        } + ".png");
    }

    @Override
    protected void init() {
        String previousCommand = command == null ? "" : command.getValue();
        command = null;
        commandSuggestions = null;
        super.init();
        switch (menu.page) {
            case MAIN -> {
                button(7, "themenugui.button_empty", 134, 64, 30);
                icon(10, "forward", 281, 4);
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
                button(0, "command_gui.button_execute_com", 41, 46, 93);
                icon(1, "potion_page", 3, 2);
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

    private void icon(int id, String name, int x, int y) {
        Identifier image = Identifier.fromNamespaceAndPath("utilitymenu", "textures/screens/" + name + ".png");
        addRenderableWidget(new Button(leftPos + x, topPos + y, 16, 16,
                text(menu.page == UtilityMenu.Page.MAIN ? "themenugui.label_next_page" : "command_gui.label_back"),
                button -> send(id), supplier -> supplier.get()) {
            @Override
            protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, image, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        });
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0,
                imageWidth, imageHeight, imageWidth, imageHeight);
        super.extractContents(graphics, mouseX, mouseY, delta);
        if (commandSuggestions != null) {
            commandSuggestions.extractRenderState(graphics, mouseX, mouseY);
        }
        extractTooltip(graphics, mouseX, mouseY);
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
        switch (menu.page) {
            case MAIN -> {
                label(graphics, "themenugui.label_next_page", 207, 7);
                label(graphics, "themenugui.label_utility_menu_v4", 5, 162);
            }
            case COMMAND -> {
                label(graphics, "command_gui.label_command_page", 101, 4);
                label(graphics, "command_gui.label_back", 26, 6);
            }
        }
    }

    private void label(GuiGraphicsExtractor graphics, String key, int x, int y) {
        graphics.text(font, text(key), x, y, -12829636, false);
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
        // Commands are submitted only by clicking Execute, even when that button has focus.
        if (menu.page == UtilityMenu.Page.COMMAND
                && (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER)) return true;
        if (command != null && command.isFocused()) return command.keyPressed(event);
        return super.keyPressed(event);
    }
}
