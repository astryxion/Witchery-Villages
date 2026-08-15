package com.witcherywalls.entity;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import com.witcherywalls.init.ModItems;
import com.witcherywalls.worldgen.VillageGuardSpawnQueue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = WitcheryWallsMod.MODID)
public final class VillageGuardEvents
{
    private static final Component DISABLED_MESSAGE = Component.translatable("message.witcherywalls.guards_disabled");
    private static final Component ITEM_DISABLED_MESSAGE = Component.translatable("message.witcherywalls.item_disabled");
    private static final String STEAL_TAG = "WitcheryWallsSteal";

    private VillageGuardEvents()
    {
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event)
    {
        if (WitcheryWallsConfig.spawnVillageGuards())
        {
            return;
        }

        String command = event.getParseResults().getReader().getString();
        Component message = null;
        if (isVillageGuardSummon(command))
        {
            message = DISABLED_MESSAGE;
        }
        else if (isVillageGuardGive(command))
        {
            message = ITEM_DISABLED_MESSAGE;
        }
        if (message == null)
        {
            return;
        }

        event.setCanceled(true);
        CommandSourceStack source = event.getParseResults().getContext().getSource();
        if (source.getEntity() instanceof Player player)
        {
            player.displayClientMessage(message, true);
        }
        else
        {
            source.sendFailure(message);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event)
    {
        if (VillageGuardSpawnQueue.isPlacing() || WitcheryWallsConfig.spawnVillageGuards())
        {
            return;
        }
        if (event.getSpawnType() != MobSpawnType.STRUCTURE)
        {
            return;
        }
        if (!VillageGuardSpawnQueue.isGuardVillagersGuard(event.getEntity()))
        {
            return;
        }
        event.getEntity().getPersistentData().putBoolean(STEAL_TAG, true);
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide() || event.isCanceled())
        {
            return;
        }
        if (event.getEntity() instanceof VillageGuard && !WitcheryWallsConfig.spawnVillageGuards())
        {
            event.setCanceled(true);
            return;
        }
        CompoundTag data = event.getEntity().getPersistentData();
        if (data.getBoolean(STEAL_TAG) && event.getLevel() instanceof ServerLevel serverLevel)
        {
            event.setCanceled(true);
            VillageGuardSpawnQueue.steal(serverLevel, event.getEntity().blockPosition());
        }
    }

    @SubscribeEvent
    public static void onUseSpawnEgg(PlayerInteractEvent.RightClickBlock event)
    {
        if (isDisabledSpawnEgg(event))
        {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            showDisabledMessage(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onUseSpawnEgg(PlayerInteractEvent.RightClickItem event)
    {
        if (isDisabledSpawnEgg(event))
        {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            showDisabledMessage(event.getEntity());
        }
    }

    private static void showDisabledMessage(Player player)
    {
        player.displayClientMessage(DISABLED_MESSAGE, true);
    }

    private static boolean isDisabledSpawnEgg(PlayerInteractEvent event)
    {
        return event.getItemStack().is(ModItems.VILLAGE_GUARD_SPAWN_EGG.get())
                && !WitcheryWallsConfig.spawnVillageGuards();
    }

    private static boolean isVillageGuardSummon(String command)
    {
        String[] parts = commandParts(command);
        return parts.length >= 2 && parts[0].equals("summon") && isVillageGuardEntityId(parts[1]);
    }

    private static boolean isVillageGuardGive(String command)
    {
        String[] parts = commandParts(command);
        if (parts.length < 3 || !parts[0].equals("give"))
        {
            return false;
        }
        for (int i = 2; i < parts.length; i++)
        {
            if (isVillageGuardSpawnEggId(parts[i]))
            {
                return true;
            }
        }
        return false;
    }

    private static String[] commandParts(String command)
    {
        String text = command.trim();
        if (text.startsWith("/"))
        {
            text = text.substring(1);
        }
        text = text.toLowerCase(Locale.ROOT);
        if (text.startsWith("minecraft:"))
        {
            text = text.substring("minecraft:".length());
        }
        return text.split("\\s+");
    }

    private static boolean isVillageGuardEntityId(String id)
    {
        int brace = id.indexOf('{');
        if (brace >= 0)
        {
            id = id.substring(0, brace);
        }
        return id.equals("witcherywalls:village_guard") || id.equals("village_guard");
    }

    private static boolean isVillageGuardSpawnEggId(String id)
    {
        int brace = id.indexOf('{');
        if (brace >= 0)
        {
            id = id.substring(0, brace);
        }
        return id.equals("witcherywalls:village_guard_spawn_egg") || id.equals("village_guard_spawn_egg");
    }
}
