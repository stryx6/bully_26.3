package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.hackermenu.HackerMenu;
import net.kraftattacke.bully.hackermenu.HackerMenuPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.platform.InputConstants;

/** The original three layouts and textures, rendered through the 26.3 extraction API. */
public final class HackerMenuScreen extends AbstractContainerScreen<HackerMenu> {
    private final Identifier texture;
    private EditBox command;

    public HackerMenuScreen(HackerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.page == HackerMenu.Page.MAIN ? 300 : 176,
                menu.page == HackerMenu.Page.MAIN ? 173 : 166);
        texture = Identifier.fromNamespaceAndPath("hackermenu", "textures/screens/" + switch (menu.page) {
            case MAIN -> "themenugui";
            case COMMAND -> "command_gui";
            case POTION -> "potiongui";
        } + ".png");
    }

    @Override
    protected void init() {
        String previousCommand = command == null ? "" : command.getValue();
        super.init();
        switch (menu.page) {
            case MAIN -> {
                button(0, "themenugui.button_surviva", 5, 86, 61);
                button(1, "themenugui.button_surviva1", 5, 106, 61);
                button(2, "themenugui.button_surviva2", 5, 126, 61);
                for (int i = 0; i < 4; i++) button(3 + i, "themenugui.button_surviva" + (3 + i), 233, 84 + i * 20, 61);
                button(7, "themenugui.button_empty", 134, 64, 30);
                button(8, "themenugui.button_empty2", 35, 44, 25);
                button(9, "themenugui.button_empty1", 10, 44, 25);
                icon(10, "forward", 281, 4);
                icon(11, "potion_page", 4, 4);
            }
            case COMMAND -> {
                command = new EditBox(font, leftPos + 4, topPos + 28, 167, 18,
                        text("command_gui.servercommands"));
                command.setMaxLength(32767);
                command.setResponder(value -> command.setSuggestion(value.isEmpty()
                        ? text("command_gui.servercommands").getString() : null));
                command.setValue(previousCommand);
                addRenderableWidget(command);
                button(0, "command_gui.button_execute_com", 41, 46, 93);
                icon(1, "potion_page", 3, 2);
            }
            case POTION -> {
                button(0, "potiongui.button_quick_mine", 6, 34, 77);
                button(1, "potiongui.button_quick_mine1", 92, 34, 77);
                button(2, "potiongui.button_quick_mine2", 92, 61, 77);
                button(3, "potiongui.button_quick_mine3", 92, 88, 77);
                button(4, "potiongui.button_quick_mine4", 6, 61, 77);
                button(5, "potiongui.button_quick_mine5", 6, 88, 77);
                button(6, "potiongui.button_quick_mine6", 51, 124, 77);
                icon(7, "forward", 150, 7);
            }
        }
    }

    private static Component text(String key) { return Component.translatable("gui.hackermenu." + key); }

    private void send(int button) {
        ClientPlayNetworking.send(new HackerMenuPayload(menu.containerId, button,
                command == null ? "" : command.getValue()));
    }

    private void button(int id, String key, int x, int y, int width) {
        addRenderableWidget(Button.builder(text(key), button -> send(id))
                .bounds(leftPos + x, topPos + y, width, 20).build());
    }

    private void icon(int id, String name, int x, int y) {
        Identifier image = Identifier.fromNamespaceAndPath("hackermenu", "textures/screens/" + name + ".png");
        addRenderableWidget(new Button(leftPos + x, topPos + y, 16, 16,
                text(menu.page == HackerMenu.Page.MAIN
                        ? id == 10 ? "themenugui.label_next_page" : "themenugui.label_potion_page"
                        : menu.page == HackerMenu.Page.COMMAND ? "command_gui.label_back" : "potiongui.label_back"),
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
        extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        switch (menu.page) {
            case MAIN -> {
                label(graphics, "themenugui.label_next_page", 207, 7);
                label(graphics, "themenugui.label_hacker_menu_v4", 5, 162);
                label(graphics, "themenugui.label_potion_page", 32, 7);
                label(graphics, "themenugui.label_semigod", 14, 34);
            }
            case COMMAND -> {
                label(graphics, "command_gui.label_command_page", 101, 4);
                label(graphics, "command_gui.label_back", 26, 6);
            }
            case POTION -> {
                label(graphics, "potiongui.label_potion_page", 60, 151);
                label(graphics, "potiongui.label_back", 121, 11);
            }
        }
    }

    private void label(GuiGraphicsExtractor graphics, String key, int x, int y) {
        graphics.text(font, text(key), x, y, -12829636, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        // Commands are submitted only by clicking Execute, even when that button has focus.
        if (menu.page == HackerMenu.Page.COMMAND
                && (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER)) return true;
        if (command != null && command.isFocused()) return command.keyPressed(event);
        return super.keyPressed(event);
    }
}
