package com.witcherywalls.client;

import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game config from Mods → Witchery Walls → Config.
 * Values write into the Forge common config and save on Done.
 */
public final class WitcheryWallsConfigScreen extends Screen
{
    private static final int FIELD_WIDTH = 80;
    private static final int BLACKLIST_WIDTH = 280;

    private final Screen parent;
    private EditBox wallChanceBox;
    private EditBox keepChanceBox;
    private EditBox towerChanceBox;
    private EditBox dedupRadiusBox;
    private EditBox blacklistBox;
    private CycleButton<Boolean> guardsButton;
    private Component statusMessage = CommonComponents.EMPTY;

    public WitcheryWallsConfigScreen(Screen parent)
    {
        super(Component.translatable("config.witcherywalls.title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int centerX = this.width / 2;

        this.wallChanceBox = intBox(centerX + 20, 40, WitcheryWallsConfig.WALL_SPAWN_CHANCE.get(),
                "config.witcherywalls.wall_spawn_chance.tooltip");
        this.keepChanceBox = intBox(centerX + 20, 64, WitcheryWallsConfig.KEEP_SPAWN_CHANCE.get(),
                "config.witcherywalls.keep_spawn_chance.tooltip");
        this.towerChanceBox = intBox(centerX + 20, 88, WitcheryWallsConfig.WATCH_TOWER_SPAWN_CHANCE.get(),
                "config.witcherywalls.watch_tower_spawn_chance.tooltip");
        this.dedupRadiusBox = intBox(centerX + 20, 112, WitcheryWallsConfig.villageWallDedupRadius(),
                "config.witcherywalls.dedup_radius.tooltip");

        this.blacklistBox = new EditBox(this.font, centerX - BLACKLIST_WIDTH / 2, 156, BLACKLIST_WIDTH, 20,
                Component.translatable("config.witcherywalls.structure_blacklist"));
        this.blacklistBox.setMaxLength(1024);
        this.blacklistBox.setValue(String.join(", ", WitcheryWallsConfig.structureBlacklistEntries()));
        this.blacklistBox.setHint(Component.translatable("config.witcherywalls.structure_blacklist.hint"));
        this.blacklistBox.setTooltip(Tooltip.create(Component.translatable("config.witcherywalls.structure_blacklist.tooltip")));
        addRenderableWidget(this.blacklistBox);

        this.guardsButton = CycleButton.onOffBuilder(WitcheryWallsConfig.SPAWN_VILLAGE_GUARDS.get())
                .withTooltip(value -> Tooltip.create(Component.translatable("config.witcherywalls.spawn_guards.tooltip")))
                .create(centerX - 150, 186, 300, 20,
                        Component.translatable("config.witcherywalls.spawn_guards"),
                        (button, value) -> {
                        });
        addRenderableWidget(this.guardsButton);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            if (applyChanges())
            {
                this.minecraft.setScreen(this.parent);
            }
        }).bounds(centerX - 155, this.height - 28, 150, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent))
                .bounds(centerX + 5, this.height - 28, 150, 20).build());
    }

    private EditBox intBox(int x, int y, int value, String tooltipKey)
    {
        EditBox box = new EditBox(this.font, x, y, FIELD_WIDTH, 20, Component.empty());
        box.setMaxLength(4);
        box.setValue(Integer.toString(value));
        box.setFilter(text -> text.isEmpty() || text.matches("\\d{1,4}"));
        box.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        addRenderableWidget(box);
        return box;
    }

    private boolean applyChanges()
    {
        Integer wallChance = parseIntInRange(this.wallChanceBox.getValue(), 0, 100);
        Integer keepChance = parseIntInRange(this.keepChanceBox.getValue(), 0, 100);
        Integer towerChance = parseIntInRange(this.towerChanceBox.getValue(), 0, 100);
        Integer dedupRadius = parseIntInRange(this.dedupRadiusBox.getValue(), 40, 512);

        if (wallChance == null || keepChance == null || towerChance == null || dedupRadius == null)
        {
            this.statusMessage = Component.translatable("config.witcherywalls.invalid_number");
            return false;
        }

        List<String> blacklist = parseBlacklist(this.blacklistBox.getValue());
        if (blacklist == null)
        {
            this.statusMessage = Component.translatable("config.witcherywalls.invalid_structure_id");
            return false;
        }

        WitcheryWallsConfig.WALL_SPAWN_CHANCE.set(wallChance);
        WitcheryWallsConfig.KEEP_SPAWN_CHANCE.set(keepChance);
        WitcheryWallsConfig.WATCH_TOWER_SPAWN_CHANCE.set(towerChance);
        WitcheryWallsConfig.VILLAGE_WALL_DEDUP_RADIUS.set(dedupRadius);
        WitcheryWallsConfig.SPAWN_VILLAGE_GUARDS.set(this.guardsButton.getValue());
        WitcheryWallsConfig.VILLAGE_GUARDS_DEFAULT_APPLIED.set(true);
        WitcheryWallsConfig.setStructureBlacklist(blacklist);
        WitcheryWallsConfig.save();
        return true;
    }

    private static Integer parseIntInRange(String text, int min, int max)
    {
        try
        {
            int value = Integer.parseInt(text.trim());
            if (value < min || value > max)
            {
                return null;
            }
            return value;
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    private static List<String> parseBlacklist(String raw)
    {
        List<String> entries = new ArrayList<>();
        if (raw == null || raw.isBlank())
        {
            return entries;
        }

        for (String part : raw.split("[,;\\s]+"))
        {
            String trimmed = part.trim();
            if (trimmed.isEmpty())
            {
                continue;
            }
            if (!ResourceLocation.isValidResourceLocation(trimmed))
            {
                return null;
            }
            entries.add(trimmed);
        }
        return entries;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int centerX = this.width / 2;
        int labelColor = 0xE0E0E0;
        graphics.drawString(this.font, Component.translatable("config.witcherywalls.wall_spawn_chance"),
                centerX - 160, 46, labelColor);
        graphics.drawString(this.font, Component.translatable("config.witcherywalls.keep_spawn_chance"),
                centerX - 160, 70, labelColor);
        graphics.drawString(this.font, Component.translatable("config.witcherywalls.watch_tower_spawn_chance"),
                centerX - 160, 94, labelColor);
        graphics.drawString(this.font, Component.translatable("config.witcherywalls.dedup_radius"),
                centerX - 160, 118, labelColor);
        graphics.drawCenteredString(this.font, Component.translatable("config.witcherywalls.structure_blacklist"),
                centerX, 140, labelColor);

        if (this.statusMessage != CommonComponents.EMPTY)
        {
            graphics.drawCenteredString(this.font, this.statusMessage, this.width / 2, this.height - 45, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
    }
}
