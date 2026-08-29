package com.witcherywalls.entity;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = WitcheryWallsMod.MODID)
public final class VillageGuardEvents
{
    private static final ITextComponent DISABLED_MESSAGE = new TextComponentTranslation("message.witcherywalls.guards_disabled");
    private static final ITextComponent ITEM_DISABLED_MESSAGE = new TextComponentTranslation("message.witcherywalls.item_disabled");
    private static final ResourceLocation GUARD_ID = new ResourceLocation(WitcheryWallsMod.MODID, "village_guard");

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

        ICommand command = event.getCommand();
        String[] args = event.getParameters();
        ITextComponent message = null;
        if (isCommand(command, "summon") && args.length >= 1 && isVillageGuardEntityId(args[0]))
        {
            message = DISABLED_MESSAGE;
        }
        else if (isCommand(command, "give") && isVillageGuardSpawnEggGive(args))
        {
            message = ITEM_DISABLED_MESSAGE;
        }
        if (message == null)
        {
            return;
        }

        event.setCanceled(true);
        ICommandSender sender = event.getSender();
        if (sender instanceof EntityPlayer)
        {
            ((EntityPlayer) sender).sendStatusMessage(message, true);
        }
        else
        {
            sender.sendMessage(message);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event)
    {
        if (event.getWorld().isRemote || event.isCanceled())
        {
            return;
        }
        if (event.getEntity() instanceof EntityVillageGuard && !WitcheryWallsConfig.spawnVillageGuards())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseSpawnEgg(PlayerInteractEvent.RightClickBlock event)
    {
        if (isDisabledSpawnEgg(event.getItemStack()))
        {
            event.setCanceled(true);
            showDisabledMessage(event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public static void onUseSpawnEgg(PlayerInteractEvent.RightClickItem event)
    {
        if (isDisabledSpawnEgg(event.getItemStack()))
        {
            event.setCanceled(true);
            showDisabledMessage(event.getEntityPlayer());
        }
    }

    private static void showDisabledMessage(EntityPlayer player)
    {
        if (player != null)
        {
            player.sendStatusMessage(DISABLED_MESSAGE, true);
        }
    }

    private static boolean isDisabledSpawnEgg(ItemStack stack)
    {
        if (!WitcheryWallsConfig.spawnVillageGuards() && !stack.isEmpty() && stack.getItem() instanceof ItemMonsterPlacer)
        {
            ResourceLocation id = ItemMonsterPlacer.getNamedIdFrom(stack);
            return id != null && GUARD_ID.equals(id);
        }
        return false;
    }

    private static boolean isCommand(ICommand command, String name)
    {
        if (name.equals(command.getName().toLowerCase(Locale.ROOT)))
        {
            return true;
        }
        for (String alias : command.getAliases())
        {
            if (name.equals(alias.toLowerCase(Locale.ROOT)))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isVillageGuardSpawnEggGive(String[] args)
    {
        String joined = String.join(" ", args).toLowerCase(Locale.ROOT);
        boolean spawnEgg = joined.contains("spawn_egg");
        boolean guard = joined.contains("witcherywalls:village_guard") || joined.contains("village_guard");
        return spawnEgg && guard;
    }

    private static boolean isVillageGuardEntityId(String id)
    {
        id = stripNbt(id).toLowerCase(Locale.ROOT);
        return "witcherywalls:village_guard".equals(id) || "village_guard".equals(id);
    }

    private static String stripNbt(String id)
    {
        int brace = id.indexOf('{');
        return brace >= 0 ? id.substring(0, brace) : id;
    }
}
