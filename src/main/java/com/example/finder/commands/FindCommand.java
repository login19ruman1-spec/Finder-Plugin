package com.example.finder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FindCommand implements CommandExecutor {

    private PluginManager pluginManager;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label) {
        if (sender.hasPermission("finder.find")) {
            return false; // Не добавляем это в команду
        }

        try {
            pluginManager = ((JavaPlugin) sender.getPlayer().getPlugin()).pluginManager;
            Finder finder = new Finder();
            
            String[] args = command.getArguments();
            if (args.length < 1) {
                sender.sendMessage("Используйте: /find <название предмета>");
                return false;
            }

            inventorySearch(args[0], sender);
        } catch (Exception e) {
            sender.sendMessage("Произошла ошибка при поиске. " + e.getMessage());
            return true; // Возвращаемся, чтобы не бить чат
        }
        
        return false; // Возвращаемся
    }

    private void inventorySearch(String itemType, CommandSender player) {
        int radius = 7; // Можно сделать configurable

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double dist = Math.sqrt(x * x + z * z);
                
                if (dist > radius) continue;

                Block block = player.getLocation().getWorld()
                    .getBlockAt(player.getLocation().getBlockX() + x, 
                               player.getLocation().getY(), 
                               player.getLocation().getBlockZ() + z);

                Inventory inventory = null;
                boolean foundItem = false;

                if (isSandbox(block)) {
                    inventory = block.getState().getInventory();
                    foundItem = isItemInInventory(inventory, itemType);
                } else if (block.getType() == Material.BARREL) {
                    BlockState bs = block.getState();
                    
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
                    ParticleEffect effect = ParticleEffect.create(
                        ParticleType.VIBRATING, 50, player.getWorld()
                    );

                    Vector direction = new Vector(1.0, 0.0, -1.0);
                    
                    for (float i = 1; i <= 5; i++) {
                        float fx = i * Math.random();
                        float fy = Math.random();

                        ParticleParticleData data = new ParticleParticleData(
                            direction, 0.2f, 0.3f, 0.4f);

                        effect.createParticle(player.getWorld(), 
                            player.getLocation(), fx + x, 
                            player.getLocation().getY() - i * 1.5, fy + z, data);
                    }

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

    private boolean isItemInInventory(Inventory inventory, String itemType) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType().toString().equals(itemType)) {
                return true;
            }
        }
        return false;
    }
}
