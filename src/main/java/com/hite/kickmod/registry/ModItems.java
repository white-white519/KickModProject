package com.hite.kickmod.registry;

import com.hite.kickmod.KickMod;
import com.hite.kickmod.item.HammerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, KickMod.MODID);

    public static final RegistryObject<Item> HAMMER =
        ITEMS.register("hammer", () -> new HammerItem(new Item.Properties().stacksTo(1).durability(64)));
}