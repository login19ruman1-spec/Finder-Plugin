package com.example.finder;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.particle.ParticleEffect;
import org.bukkit.plugin.java.JavaPlugin;

public class Finder extends JavaPlugin {

    private static final int DEFAULT_RADIUS = 7;
    
    @Override
    public void onEnable() {
        saveDefaultConfig(); // Загружаем default config.yml
        System.out.println("Плагин Finder включен!");
    }

    @Override
    public void onDisable() {
        System.out.println("Плагин Finder отключен.");
    }

    private boolean isItemInInventory(Inventory inventory, String itemType) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType().toString().equals(itemType)) {
                return true;
            }
        }
        return false;
    }

    public void findItems(String itemType, Player player) {
        Location playerPos = player.getLocation();
        int radius = DEFAULT_RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double dist = Math.sqrt(x * x + z * z);
                
                if (dist > radius) continue; // Оптимизация

                Block block = player.getWorld().getBlockAt(
                    playerPos.getBlockX() + x, 
                    playerPos.getY(), 
                    playerPos.getBlockZ() + z
                );

                if (block.isAir()) continue;

                Inventory inventory = null;
                boolean foundItem = false;

                // Проверка сундуков
                if (isSandbox(block)) {
                    inventory = block.getState().getInventory();
                    foundItem = isItemInInventory(inventory, itemType);
                } 
                
                // Проверка бочек
                else if (block.getType() == Material.BARREL) {
                    BlockState bs = block.getState();
                    Inventory inv = null;

                    for (int i = 0; i < inventory.size(); i++) {
                        ItemStack item = inventory.getItem(i);
                        String itemTypeString = item != null ? 
                            item.getType().toString() : "";

                        if (itemTypeString.equals(itemType)) {
                            foundItem = true;
                            break;
                        }
                    }
                }

                if (foundItem) {
                    // Частицы для выделения предмета
                    ParticleEffect effect = ParticleEffect.create(
                        ParticleType.VIBRATING, 
                        50, 
                        player.getWorld()
                    );

                    Vector direction = new Vector(1.0, 0.0, -1.0);
                    for (float i = 1; i <= 5; i++) {
                        float fx = i * Math.random();
                        float fy = Math.random();
                        
                        ParticleParticleData data = new ParticleParticleData(
                            direction, 0.2f, 0.3f, 0.4f
                        );

                        effect.createParticle(player.getWorld(), 
                            playerPos, fx + x, playerPos.getY() - i * 1.5, fy + z, data);
                    }

                    // Показать сообщение игроку
                    String message = "Найдено: " + item.getType().getName();
                    player.sendMessage(message);
                }
            }
        }
    }

    private boolean isSandbox(Block block) {
        return block.getType() == Material.CHEST && 
               block.getState() != null;
    }
}
