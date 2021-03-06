/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.items.custom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;

import static com.skelril.aurora.items.custom.CustomItems.*;

public class CustomItemCenter {
    private static HashMap<CustomItems, CustomItem> items = new HashMap<>();

    private static void addItem(CustomItem item) {
        items.put(item.getItem(), item);
    }

    static {
        // Ancient Armor
        CustomEquipment ancientHelmet = new CustomEquipment(ANCIENT_HELMET, Material.CHAINMAIL_HELMET);
        ancientHelmet.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ancientHelmet.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        ancientHelmet.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        ancientHelmet.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        ancientHelmet.addEnchant(Enchantment.OXYGEN, 3);
        ancientHelmet.addEnchant(Enchantment.WATER_WORKER, 1);
        ancientHelmet.addSource(ItemSource.WISHING_WELL);
        ancientHelmet.addSource(ItemSource.MARKET);
        ancientHelmet.addUse("Set Effect: Ancient Power");
        ancientHelmet.addUse("Repaired with XP");
        addItem(ancientHelmet);

        CustomEquipment ancientChestplate = new CustomEquipment(ANCIENT_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE);
        ancientChestplate.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ancientChestplate.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        ancientChestplate.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        ancientChestplate.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        ancientChestplate.addSource(ItemSource.WISHING_WELL);
        ancientChestplate.addSource(ItemSource.MARKET);
        ancientChestplate.addUse("Set Effect: Ancient Power");
        ancientChestplate.addUse("Repaired with XP");
        addItem(ancientChestplate);

        CustomEquipment ancientLeggings = new CustomEquipment(ANCIENT_LEGGINGS, Material.CHAINMAIL_LEGGINGS);
        ancientLeggings.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ancientLeggings.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        ancientLeggings.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        ancientLeggings.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        ancientLeggings.addSource(ItemSource.WISHING_WELL);
        ancientLeggings.addSource(ItemSource.MARKET);
        ancientLeggings.addUse("Set Effect: Ancient Power");
        ancientLeggings.addUse("Repaired with XP");
        addItem(ancientLeggings);

        CustomEquipment ancientBoots = new CustomEquipment(ANCIENT_BOOTS, Material.CHAINMAIL_BOOTS);
        ancientBoots.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        ancientBoots.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        ancientBoots.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        ancientBoots.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        ancientBoots.addEnchant(Enchantment.PROTECTION_FALL, 4);
        ancientBoots.addSource(ItemSource.WISHING_WELL);
        ancientBoots.addSource(ItemSource.MARKET);
        ancientBoots.addUse("Set Effect: Ancient Power");
        ancientBoots.addUse("Repaired with XP");
        addItem(ancientBoots);

        // Elder Armor
        CustomEquipment elderCrown = new CustomEquipment(ELDER_CROWN, Material.GOLD_HELMET);
        elderCrown.addEnchant(Enchantment.DURABILITY, 3);
        elderCrown.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        elderCrown.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        elderCrown.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        elderCrown.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        elderCrown.addEnchant(Enchantment.OXYGEN, 3);
        elderCrown.addEnchant(Enchantment.WATER_WORKER, 1);
        elderCrown.addSource(ItemSource.MARKET);
        elderCrown.addSource(ItemSource.SHNUGGLES_PRIME);
        elderCrown.addUse("Set Effect: Ancient Armor");
        elderCrown.addUse("Double Health Regen");
        elderCrown.addUse("Double XP Gain");
        elderCrown.addUse("Acts as an Imbued Crystal");
        elderCrown.addUse("Acts as a Gem of Darkness");
        elderCrown.addUse("Acts as an Ancient Helmet");
        elderCrown.addUse("Repaired with XP");
        addItem(elderCrown);

        CustomEquipment elderHelmet = new CustomEquipment(ELDER_HELMET, Material.CHAINMAIL_HELMET);
        elderHelmet.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        elderHelmet.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        elderHelmet.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        elderHelmet.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        elderHelmet.addEnchant(Enchantment.OXYGEN, 3);
        elderHelmet.addEnchant(Enchantment.WATER_WORKER, 1);
        elderHelmet.addSource(ItemSource.WISHING_WELL);
        elderHelmet.addSource(ItemSource.MARKET);
        elderHelmet.addUse("Set Effect: Ancient Power");
        elderHelmet.addUse("Repaired with XP");
        addItem(elderHelmet);

        CustomEquipment elderChestplate = new CustomEquipment(ELDER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE);
        elderChestplate.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        elderChestplate.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        elderChestplate.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        elderChestplate.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        elderChestplate.addSource(ItemSource.WISHING_WELL);
        elderChestplate.addSource(ItemSource.MARKET);
        elderChestplate.addUse("Set Effect: Ancient Power");
        elderChestplate.addUse("Repaired with XP");
        addItem(elderChestplate);

        CustomEquipment elderLeggings = new CustomEquipment(ELDER_LEGGINGS, Material.CHAINMAIL_LEGGINGS);
        elderLeggings.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        elderLeggings.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        elderLeggings.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        elderLeggings.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        elderLeggings.addSource(ItemSource.WISHING_WELL);
        elderLeggings.addSource(ItemSource.MARKET);
        elderLeggings.addUse("Set Effect: Ancient Power");
        elderLeggings.addUse("Repaired with XP");
        addItem(elderLeggings);

        CustomEquipment elderBoots = new CustomEquipment(ELDER_BOOTS, Material.CHAINMAIL_BOOTS);
        elderBoots.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        elderBoots.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        elderBoots.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        elderBoots.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        elderBoots.addEnchant(Enchantment.PROTECTION_FALL, 4);
        elderBoots.addSource(ItemSource.WISHING_WELL);
        elderBoots.addSource(ItemSource.MARKET);
        elderBoots.addUse("Set Effect: Ancient Power");
        elderBoots.addUse("Repaired with XP");
        addItem(elderBoots);

        // Nectric Armor
        CustomEquipment nectricHelmet = new CustomEquipment(NECTRIC_HELMET, Material.DIAMOND_HELMET);
        nectricHelmet.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        nectricHelmet.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        nectricHelmet.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        nectricHelmet.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        nectricHelmet.addEnchant(Enchantment.OXYGEN, 3);
        nectricHelmet.addEnchant(Enchantment.WATER_WORKER, 1);
        nectricHelmet.addSource(ItemSource.MARKET);
        nectricHelmet.addSource(ItemSource.PATIENT_X);
        nectricHelmet.addUse("Set Effect: Necrotic Armor");
        addItem(nectricHelmet);

        CustomEquipment nectricChestplate = new CustomEquipment(NECTRIC_CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        nectricChestplate.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        nectricChestplate.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        nectricChestplate.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        nectricChestplate.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        nectricChestplate.addSource(ItemSource.MARKET);
        nectricChestplate.addSource(ItemSource.PATIENT_X);
        nectricChestplate.addUse("Set Effect: Necrotic Armor");
        addItem(nectricChestplate);

        CustomEquipment nectricLeggings = new CustomEquipment(NECTRIC_LEGGINGS, Material.DIAMOND_LEGGINGS);
        nectricLeggings.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        nectricLeggings.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        nectricLeggings.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        nectricLeggings.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        nectricLeggings.addSource(ItemSource.MARKET);
        nectricLeggings.addSource(ItemSource.PATIENT_X);
        nectricLeggings.addUse("Set Effect: Necrotic Armor");
        addItem(nectricLeggings);

        CustomEquipment nectricBoots = new CustomEquipment(NECTRIC_BOOTS, Material.DIAMOND_BOOTS);
        nectricBoots.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        nectricBoots.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        nectricBoots.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        nectricBoots.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        nectricBoots.addEnchant(Enchantment.PROTECTION_FALL, 4);
        nectricBoots.addSource(ItemSource.MARKET);
        nectricBoots.addSource(ItemSource.PATIENT_X);
        nectricBoots.addUse("Set Effect: Necrotic Armor");
        addItem(nectricBoots);

        // Necros Armor
        CustomEquipment necrosHelmet = new CustomEquipment(NECROS_HELMET, Material.DIAMOND_HELMET);
        necrosHelmet.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        necrosHelmet.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        necrosHelmet.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        necrosHelmet.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        necrosHelmet.addEnchant(Enchantment.OXYGEN, 3);
        necrosHelmet.addEnchant(Enchantment.WATER_WORKER, 1);
        necrosHelmet.addSource(ItemSource.MARKET);
        necrosHelmet.addSource(ItemSource.PATIENT_X);
        necrosHelmet.addUse("Set Effect: Necrotic Armor");
        necrosHelmet.addUse("Repaired with XP");
        addItem(necrosHelmet);

        CustomEquipment necrosChestplate = new CustomEquipment(NECROS_CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        necrosChestplate.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        necrosChestplate.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        necrosChestplate.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        necrosChestplate.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        necrosChestplate.addSource(ItemSource.MARKET);
        necrosChestplate.addSource(ItemSource.PATIENT_X);
        necrosChestplate.addUse("Set Effect: Necrotic Armor");
        necrosChestplate.addUse("Repaired with XP");
        addItem(necrosChestplate);

        CustomEquipment necrosLeggings = new CustomEquipment(NECROS_LEGGINGS, Material.DIAMOND_LEGGINGS);
        necrosLeggings.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        necrosLeggings.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        necrosLeggings.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        necrosLeggings.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        necrosLeggings.addSource(ItemSource.MARKET);
        necrosLeggings.addSource(ItemSource.PATIENT_X);
        necrosLeggings.addUse("Set Effect: Necrotic Armor");
        necrosLeggings.addUse("Repaired with XP");
        addItem(necrosLeggings);

        CustomEquipment necrosBoots = new CustomEquipment(NECROS_BOOTS, Material.DIAMOND_BOOTS);
        necrosBoots.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        necrosBoots.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        necrosBoots.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        necrosBoots.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        necrosBoots.addEnchant(Enchantment.PROTECTION_FALL, 4);
        necrosBoots.addSource(ItemSource.MARKET);
        necrosBoots.addSource(ItemSource.PATIENT_X);
        necrosBoots.addUse("Set Effect: Necrotic Armor");
        necrosBoots.addUse("Repaired with XP");
        addItem(necrosBoots);

        // Master Weapons
        CustomWeapon masterSword = new CustomWeapon(MASTER_SWORD, Material.DIAMOND_SWORD, 2);
        masterSword.addSource(ItemSource.SHNUGGLES_PRIME);
        masterSword.addSource(ItemSource.MARKET);
        masterSword.addUse("Repairable at any Sacrificial Pit");
        masterSword.addUse("Conditional Effects");
        addItem(masterSword);

        CustomWeapon masterBow = new CustomWeapon(MASTER_BOW, Material.BOW, 2);
        masterBow.addSource(ItemSource.SHNUGGLES_PRIME);
        masterBow.addSource(ItemSource.MARKET);
        masterBow.addUse("Repairable at any Sacrificial Pit");
        masterBow.addUse("Conditional Effects");
        addItem(masterBow);

        CustomWeapon corruptMasterSword = new CustomWeapon(CORRUPT_MASTER_SWORD, Material.DIAMOND_SWORD, 2);
        corruptMasterSword.addSource(ItemSource.SHNUGGLES_PRIME);
        corruptMasterSword.addSource(ItemSource.MARKET);
        corruptMasterSword.addUse("Conditional Effects");
        addItem(corruptMasterSword);

        CustomWeapon corruptMasterBow = new CustomWeapon(CORRUPT_MASTER_BOW, Material.BOW, 2);
        corruptMasterBow.addSource(ItemSource.SHNUGGLES_PRIME);
        corruptMasterBow.addSource(ItemSource.MARKET);
        corruptMasterBow.addUse("Conditional Effects");
        addItem(corruptMasterBow);

        // Unleashed Weapons
        CustomWeapon unleashedSword = new CustomWeapon(UNLEASHED_SWORD, Material.DIAMOND_SWORD, 2.25);
        unleashedSword.addSource(ItemSource.MARKET);
        unleashedSword.addUse("Repairable at any Sacrificial Pit, but requires 2 Imbued Crystals " +
                "for every 11% damage, or 1 Imbued Crystal if repaired inside of the Grave Yard rewards room.");
        unleashedSword.addUse("Global Effects");
        addItem(unleashedSword);

        CustomWeapon unleashedBow = new CustomWeapon(UNLEASHED_BOW, Material.BOW, 2.25);
        unleashedBow.addSource(ItemSource.MARKET);
        unleashedBow.addUse("Repairable at any Sacrificial Pit, but requires 2 Imbued Crystals " +
                "for every 11% damage, or 1 Imbued Crystal if repaired inside of the Grave Yard rewards room.");
        unleashedBow.addUse("Global Effects");
        addItem(unleashedBow);

        // Fear Weapons
        CustomWeapon fearSword = new CustomWeapon(FEAR_SWORD, Material.DIAMOND_SWORD, 2.25);
        fearSword.addSource(ItemSource.MARKET);
        fearSword.addUse("Repairable at any Sacrificial Pit, but requires 2 Gems of Darkness " +
                "for every 11% damage, or 1 Gem of Darkness if repaired inside of the Grave Yard rewards room.");
        fearSword.addUse("Global Effects");
        addItem(fearSword);

        CustomWeapon fearBow = new CustomWeapon(FEAR_BOW, Material.BOW, 2.25);
        fearBow.addSource(ItemSource.MARKET);
        fearBow.addUse("Repairable at any Sacrificial Pit, but requires 2 Gems of Darkness " +
                "for every 11% damage, or 1 Gem of Darkness if repaired inside of the Grave Yard rewards room.");
        fearBow.addUse("Global Effects");
        addItem(fearBow);

        // Shadow Items
        CustomWeapon shadowSword = new CustomWeapon(SHADOW_SWORD, Material.DIAMOND_SWORD, 5);
        fearBow.addUse("Slows your opponent with every hit.");
        addItem(shadowSword);

        CustomWeapon shadowBow = new CustomWeapon(SHADOW_BOW, Material.BOW, 5);
        fearBow.addUse("Slows your opponent with every hit.");
        addItem(shadowBow);

        // Red Items
        CustomItem redFeather = new CustomItem(RED_FEATHER, Material.FEATHER);
        redFeather.addSource(ItemSource.WILDERNESS_MOBS);
        redFeather.addSource(ItemSource.MARKET);
        redFeather.addUse("Consumes redstone to prevent up to 100% damage, " +
                "but has a cool down based on the amount of damage taken.");
        addItem(redFeather);

        // God Armor
        CustomEquipment godHelmet = new CustomEquipment(GOD_HELMET, Material.DIAMOND_HELMET);
        godHelmet.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        godHelmet.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        godHelmet.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        godHelmet.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        godHelmet.addEnchant(Enchantment.OXYGEN, 3);
        godHelmet.addEnchant(Enchantment.WATER_WORKER, 1);
        godHelmet.addSource(ItemSource.WISHING_WELL);
        godHelmet.addSource(ItemSource.MARKET);
        addItem(godHelmet);

        CustomEquipment godChestplate = new CustomEquipment(GOD_CHESTPLATE, Material.DIAMOND_CHESTPLATE);
        godChestplate.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        godChestplate.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        godChestplate.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        godChestplate.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        godChestplate.addSource(ItemSource.WISHING_WELL);
        godChestplate.addSource(ItemSource.MARKET);
        addItem(godChestplate);

        CustomEquipment godLeggings = new CustomEquipment(GOD_LEGGINGS, Material.DIAMOND_LEGGINGS);
        godLeggings.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        godLeggings.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        godLeggings.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        godLeggings.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        godLeggings.addSource(ItemSource.WISHING_WELL);
        godLeggings.addSource(ItemSource.MARKET);
        addItem(godLeggings);

        CustomEquipment godBoots = new CustomEquipment(GOD_BOOTS, Material.DIAMOND_BOOTS);
        godBoots.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        godBoots.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4);
        godBoots.addEnchant(Enchantment.PROTECTION_FIRE, 4);
        godBoots.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4);
        godBoots.addEnchant(Enchantment.PROTECTION_FALL, 4);
        godBoots.addSource(ItemSource.WISHING_WELL);
        godBoots.addSource(ItemSource.MARKET);
        addItem(godBoots);

        // God Weapons
        CustomWeapon godSword = new CustomWeapon(GOD_SWORD, Material.DIAMOND_SWORD, 1.5);
        godSword.addSource(ItemSource.WISHING_WELL);
        godSword.addSource(ItemSource.MARKET);
        addItem(godSword);

        CustomWeapon godBow = new CustomWeapon(GOD_BOW, Material.BOW, 1.5);
        godBow.addSource(ItemSource.WISHING_WELL);
        godBow.addSource(ItemSource.MARKET);
        addItem(godBow);

        // God Tools
        CustomEquipment godAxe = new CustomEquipment(GOD_AXE, Material.DIAMOND_AXE);
        godAxe.addEnchant(Enchantment.DIG_SPEED, 4);
        godAxe.addSource(ItemSource.WISHING_WELL);
        godAxe.addSource(ItemSource.MARKET);
        addItem(godAxe);

        CustomEquipment legendaryGodAxe = new CustomEquipment(LEGENDARY_GOD_AXE, Material.DIAMOND_AXE);
        legendaryGodAxe.addEnchant(Enchantment.DAMAGE_ALL, 5);
        legendaryGodAxe.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 5);
        legendaryGodAxe.addEnchant(Enchantment.DAMAGE_UNDEAD, 5);
        legendaryGodAxe.addEnchant(Enchantment.DIG_SPEED, 5);
        legendaryGodAxe.addEnchant(Enchantment.DURABILITY, 3);
        legendaryGodAxe.addSource(ItemSource.WISHING_WELL);
        legendaryGodAxe.addSource(ItemSource.MARKET);
        addItem(legendaryGodAxe);

        CustomEquipment godPickaxe = new CustomEquipment(GOD_PICKAXE, Material.DIAMOND_PICKAXE);
        godPickaxe.addEnchant(Enchantment.DIG_SPEED, 4);
        godPickaxe.addEnchant(Enchantment.SILK_TOUCH, 1);
        godPickaxe.addSource(ItemSource.WISHING_WELL);
        godPickaxe.addSource(ItemSource.MARKET);
        addItem(godPickaxe);

        CustomEquipment legendaryGodPickaxe = new CustomEquipment(LEGENDARY_GOD_PICKAXE, Material.DIAMOND_PICKAXE);
        legendaryGodPickaxe.addEnchant(Enchantment.DIG_SPEED, 5);
        legendaryGodPickaxe.addEnchant(Enchantment.DURABILITY, 3);
        legendaryGodPickaxe.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3);
        legendaryGodPickaxe.addSource(ItemSource.WISHING_WELL);
        legendaryGodPickaxe.addSource(ItemSource.MARKET);
        addItem(legendaryGodPickaxe);

        // Combat Potions
        ItemStack instantDmgPot = new Potion(PotionType.INSTANT_DAMAGE).toItemStack(1);
        CustomPotion divineCombatPotion = new CustomPotion(DIVINE_COMBAT_POTION, instantDmgPot);
        divineCombatPotion.addEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 600, 3);
        divineCombatPotion.addEffect(PotionEffectType.REGENERATION, 20 * 600, 3);
        divineCombatPotion.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 600, 3);
        divineCombatPotion.addEffect(PotionEffectType.WATER_BREATHING, 20 * 600, 3);
        divineCombatPotion.addEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 600, 3);
        divineCombatPotion.addSource(ItemSource.WISHING_WELL);
        divineCombatPotion.addSource(ItemSource.MARKET);
        addItem(divineCombatPotion);

        CustomPotion holyCombatPotion = new CustomPotion(HOLY_COMBAT_POTION, instantDmgPot);
        holyCombatPotion.addEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 45, 3);
        holyCombatPotion.addEffect(PotionEffectType.REGENERATION, 20 * 45, 3);
        holyCombatPotion.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 45, 3);
        holyCombatPotion.addEffect(PotionEffectType.WATER_BREATHING, 20 * 45, 3);
        holyCombatPotion.addEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 45, 3);
        holyCombatPotion.addSource(ItemSource.WISHING_WELL);
        holyCombatPotion.addSource(ItemSource.MARKET);
        addItem(holyCombatPotion);

        CustomPotion extremeCombatPotion = new CustomPotion(EXTREME_COMBAT_POTION, instantDmgPot);
        extremeCombatPotion.addEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 600, 2);
        extremeCombatPotion.addEffect(PotionEffectType.REGENERATION, 20 * 600, 2);
        extremeCombatPotion.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 600, 2);
        extremeCombatPotion.addEffect(PotionEffectType.WATER_BREATHING, 20 * 600, 2);
        extremeCombatPotion.addEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 600, 2);
        extremeCombatPotion.addSource(ItemSource.WISHING_WELL);
        extremeCombatPotion.addSource(ItemSource.MARKET);
        addItem(extremeCombatPotion);

        CustomPotion combatPotion = new CustomPotion(COMBAT_POTION, instantDmgPot);
        combatPotion.addEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 600, 1);
        combatPotion.addEffect(PotionEffectType.REGENERATION, 20 * 600, 1);
        combatPotion.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 600, 1);
        combatPotion.addEffect(PotionEffectType.WATER_BREATHING, 20 * 600, 1);
        combatPotion.addEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 600, 1);
        combatPotion.addSource(ItemSource.WISHING_WELL);
        combatPotion.addSource(ItemSource.MARKET);
        addItem(combatPotion);

        CustomPotion lesserCombatPotion = new CustomPotion(LESSER_COMBAT_POTION, instantDmgPot);
        lesserCombatPotion.addEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 600, 0);
        lesserCombatPotion.addEffect(PotionEffectType.REGENERATION, 20 * 600, 0);
        lesserCombatPotion.addEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 600, 0);
        lesserCombatPotion.addEffect(PotionEffectType.WATER_BREATHING, 20 * 600, 0);
        lesserCombatPotion.addEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 600, 0);
        lesserCombatPotion.addSource(ItemSource.WISHING_WELL);
        lesserCombatPotion.addSource(ItemSource.MARKET);
        addItem(lesserCombatPotion);

        // Grave Yard Gems
        CustomItem gemOfLife = new CustomItem(GEM_OF_LIFE, Material.DIAMOND);
        gemOfLife.addSource(ItemSource.MARKET);
        // gemOfLife.addUse("Preserves your inventory when you die in the Grave Yard, " +
        //        "or when you die during a thunderstorm.");
        addItem(gemOfLife);

        CustomItem gemOfDarkness = new CustomItem(GEM_OF_DARKNESS, Material.EMERALD);
        gemOfDarkness.addSource(ItemSource.MARKET);
        gemOfDarkness.addUse("Protects you from the Grave Yard's blindness effect.");
        gemOfDarkness.addUse("Used to repair Fear weapons.");
        addItem(gemOfDarkness);

        CustomItem imbuedCrystal = new CustomItem(IMBUED_CRYSTAL, Material.DIAMOND);
        imbuedCrystal.addSource(ItemSource.MARKET);
        imbuedCrystal.addUse("Compacts gold nuggets into bars.");
        imbuedCrystal.addUse("Compacts gold bars into blocks.");
        imbuedCrystal.addUse("Used to repair Unleashed Weapons.");
        addItem(imbuedCrystal);

        // Phantom Items
        CustomItem phantomGold = new CustomItem(PHANTOM_GOLD, Material.GOLD_INGOT);
        phantomGold.addSource(ItemSource.GOLD_RUSH);
        phantomGold.addUse("When sacrificed gives 50 Skrin, or 100 Skrin " +
                "if sacrificed in the Grave Yard rewards room.");
        addItem(phantomGold);

        CustomItem phantomClock = new CustomItem(PHANTOM_CLOCK, Material.WATCH);
        phantomClock.addUse("Teleports the player strait to the rewards room of the Grave Yard.");
        addItem(phantomClock);

        CustomItem phantomHymn = new CustomItem(PHANTOM_HYMN, Material.BOOK);
        phantomHymn.addSource(ItemSource.GOLD_RUSH);
        phantomHymn.addSource(ItemSource.MARKET);
        phantomHymn.addLore(ChatColor.RED + "A hymn of dark origins...");
        phantomHymn.addUse("Teleports the player through directly to the end of the Grave Yard maze.");
        phantomHymn.addUse("Teleports the player between rooms in the Freaky Four fight.");
        phantomHymn.addUse("At the cost of the item, teleports the player into Patient X's room.");
        addItem(phantomHymn);

        // Ninja Guild
        CustomItem ninjaStar = new CustomItem(NINJA_STAR, Material.NETHER_STAR);
        ninjaStar.addUse("Teleports the player to the Ninja Guild.");
        addItem(ninjaStar);

        // Flight Items
        CustomItem pixieDust = new CustomItem(PIXIE_DUST, Material.SUGAR);
        pixieDust.addSource(ItemSource.WISHING_WELL);
        pixieDust.addSource(ItemSource.GOLD_RUSH);
        pixieDust.addSource(ItemSource.MARKET);
        pixieDust.addUse("Allows the player to fly in permitted areas until " +
                "they have ran out of Pixie Dust items to consume.");
        addItem(pixieDust);

        CustomItem magicbucket = new CustomItem(MAGIC_BUCKET, Material.BUCKET);
        magicbucket.addSource(ItemSource.SHNUGGLES_PRIME);
        magicbucket.addSource(ItemSource.MARKET);
        magicbucket.addUse("Allows the player to fly indefinitely in permitted areas.");
        magicbucket.addUse("When used on a cow, it will turn into Mad Milk.");
        addItem(magicbucket);

        // Animal Bows
        CustomEquipment batBow = new CustomEquipment(BAT_BOW, Material.BOW);
        batBow.addSource(ItemSource.MARKET);
        batBow.addUse("Creates bats at the point where a fired arrow lands.");
        batBow.addUse("Creates a trail of bats following any fired arrow.");
        addItem(batBow);

        CustomEquipment chickenBow = new CustomEquipment(CHICKEN_BOW, Material.BOW);
        chickenBow.addUse("Creates chickens at the point where a fired arrow lands.");
        chickenBow.addUse("Creates a trail of chickens following any fired arrow.");
        addItem(chickenBow);

        CustomItem chickenHymn = new CustomItem(CHICKEN_HYMN, Material.BOOK);
        chickenHymn.addLore(ChatColor.BLUE + "Cluck cluck!");
        chickenHymn.addUse("Turns nearby items into chickens, and nearby chickens into cooked chicken.");
        addItem(chickenHymn);

        CustomItem godFish = new CustomItem(GOD_FISH, Material.RAW_FISH);
        godFish.addSource(ItemSource.ARROW_FISHING);
        godFish.addSource(ItemSource.MARKET);
        godFish.addUse("On consumption applies 30 seconds of the Hulk prayer.");
        addItem(godFish);

        CustomItem overSeerBow = new CustomItem(OVERSEER_BOW, Material.BOW);
        overSeerBow.addEnchant(Enchantment.ARROW_DAMAGE, 2);
        overSeerBow.addEnchant(Enchantment.ARROW_FIRE, 1);
        overSeerBow.addSource(ItemSource.WISHING_WELL);
        overSeerBow.addSource(ItemSource.MARKET);
        addItem(overSeerBow);

        CustomItem barbarianBones = new CustomItem(BARBARIAN_BONE, Material.BONE);
        barbarianBones.addSource(ItemSource.SHNUGGLES_PRIME);
        barbarianBones.addUse("Improves the drops of the Giant Boss if in a suitable quantity.");
        addItem(barbarianBones);

        CustomPotion potionOfRestitution = new CustomPotion(POTION_OF_RESTITUTION,
                new Potion(PotionType.POISON).toItemStack(1));
        potionOfRestitution.addEffect(PotionEffectType.POISON, 20 * 10, 1);
        potionOfRestitution.addSource(ItemSource.WILDERNESS_MOBS);
        potionOfRestitution.addSource(ItemSource.MARKET);
        potionOfRestitution.addUse("Returns you to your last death point if a teleport can reach the location.");
        addItem(potionOfRestitution);

        CustomItem scrollOfSummation = new CustomItem(SCROLL_OF_SUMMATION, Material.PAPER);
        scrollOfSummation.addSource(ItemSource.WILDERNESS_MOBS);
        scrollOfSummation.addSource(ItemSource.PATIENT_X);
        scrollOfSummation.addSource(ItemSource.MARKET);
        scrollOfSummation.addUse("At the cost of the item, will compact coal, iron, gold, redstone, lapis, diamonds, and emerald.");
        addItem(scrollOfSummation);

        CustomItem hymnOfSummation = new CustomItem(HYMN_OF_SUMMATION, Material.BOOK);
        hymnOfSummation.addSource(ItemSource.PATIENT_X);
        hymnOfSummation.addSource(ItemSource.MARKET);
        hymnOfSummation.addUse("Upon use, will compact coal, iron, gold, redstone, lapis, diamonds, and emerald.");
        addItem(hymnOfSummation);

        CustomItem madMilk = new CustomItem(MAD_MILK, Material.MILK_BUCKET);
        madMilk.addSource(ItemSource.MARKET);
        madMilk.addUse("If thrown into a brewing vat at the factory, a melt down will occur in which all undead creatures die.");
        madMilk.addUse("When drank, it will turn into a Magic Bucket.");
        addItem(madMilk);

        CustomItem partyBook = new CustomItem(PARTY_BOOK, Material.WRITTEN_BOOK);
        partyBook.addUse("Hit other players with the book to invite them to your party.");
        partyBook.addUse("Throw the book into the wishing well to start an instance.");
        addItem(partyBook);

        CustomItem partyScroll = new CustomItem(PARTY_SCROLL, Material.PAPER);
        partyScroll.addUse("Right click to accept a party invitation.");
        addItem(partyScroll);

        // Linear Tools
        CustomItem linearAxe = new CustomItem(LINEAR_AXE, Material.DIAMOND_AXE);
        linearAxe.addTag(ChatColor.RED, "Distance", "3");
        linearAxe.addTag(ChatColor.RED, "Max Distance", "9");
        addItem(linearAxe);

        CustomItem linearPickaxe = new CustomItem(LINEAR_PICKAXE, Material.DIAMOND_PICKAXE);
        linearPickaxe.addTag(ChatColor.RED, "Distance", "3");
        linearPickaxe.addTag(ChatColor.RED, "Max Distance", "9");
        addItem(linearPickaxe);

        CustomItem linearShovel = new CustomItem(LINEAR_SHOVEL, Material.DIAMOND_SPADE);
        linearShovel.addTag(ChatColor.RED, "Distance", "3");
        linearShovel.addTag(ChatColor.RED, "Max Distance", "9");
        addItem(linearShovel);

        // Radial Tools
        CustomItem radialAxe = new CustomItem(RADIAL_AXE, Material.DIAMOND_AXE);
        radialAxe.addTag(ChatColor.RED, "Radius", "1");
        radialAxe.addTag(ChatColor.RED, "Max Radius", "1");
        addItem(radialAxe);

        CustomItem radialPickaxe = new CustomItem(RADIAL_PICKAXE, Material.DIAMOND_PICKAXE);
        radialPickaxe.addTag(ChatColor.RED, "Radius", "1");
        radialPickaxe.addTag(ChatColor.RED, "Max Radius", "1");
        addItem(radialPickaxe);

        CustomItem radialShovel = new CustomItem(RADIAL_SHOVEL, Material.DIAMOND_SPADE);
        radialShovel.addTag(ChatColor.RED, "Radius", "1");
        radialShovel.addTag(ChatColor.RED, "Max Radius", "1");
        addItem(radialShovel);

        // Pwng Weapons
        CustomItem pwngBow = new CustomItem(PWNG_BOW, Material.BOW);
        pwngBow.addEnchant(Enchantment.ARROW_DAMAGE, 1000);
        addItem(pwngBow);

        CustomItem pwngSword = new CustomItem(PWNG_SWORD, Material.DIAMOND_SWORD);
        pwngBow.addEnchant(Enchantment.DAMAGE_ALL, 1000);
        addItem(pwngSword);

        CustomItem doomFeather = new CustomItem(DOOM_FEATHER, Material.FEATHER);
        doomFeather.addEnchant(Enchantment.KNOCKBACK, 1000);
        addItem(doomFeather);
    }

    public static CustomItem get(CustomItems item) {
        CustomItemCloneVisitor visitor = new CustomItemCloneVisitor();
        visitor.visit(items.get(item));
        return visitor.out();
    }

    public static ItemStack build(CustomItems item) {
        return items.get(item).build();
    }

    public static ItemStack build(CustomItems item, int amt) {
        ItemStack stack = items.get(item).build();
        if (amt > stack.getMaxStackSize()) throw new IllegalArgumentException();
        stack.setAmount(amt);
        return stack;
    }
}
