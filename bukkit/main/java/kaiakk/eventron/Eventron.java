package kaiakk.eventron;

import kaiakk.multimedia.classes.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Eventron extends JavaPlugin implements Listener {
    
    private final Map<UUID, Long> lastActivity = new HashMap<>();

    @Override
    public void onEnable() {
        ConsoleLog.init(this);
        ConfigHelp.init(this);
        
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("dragon.enabled", true);
        defaults.put("wither.enabled", true);
        defaults.put("wither.world", "world");
        defaults.put("wither.x", 0);
        defaults.put("wither.y", 50);
        defaults.put("wither.z", 0);
        defaults.put("wither.radius", 1000);
        defaults.put("item-clear.enabled", true);
        defaults.put("auto-heal.enabled", true);
        defaults.put("remove-banned-items.enabled", true);
        defaults.put("auto-save-worlds.enabled", true);
        defaults.put("remove-annoying-mobs.enabled", true);
        defaults.put("afk-kick.enabled", true);
        defaults.put("auto-reset-dimensions.enabled", true);
        defaults.put("random-events.enabled", true);
        defaults.put("auto-feed.enabled", true);
        defaults.put("prevent-void-death.enabled", true);
        ConfigHelp.ensureDefaults(defaults);
        
        Bukkit.getPluginManager().registerEvents(this, this);
        
        ConsoleLog.info("Eventron enabled, get ready for randomness...");
        
        if (ConfigHelp.getBoolean("dragon.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getEnvironment() == World.Environment.THE_END) {
                        DragonBattle battle = world.getEnderDragonBattle();
                        if (battle != null) {
                            EnderDragon dragon = battle.getEnderDragon();
                            if (dragon == null || dragon.isDead()) {
                                battle.initiateRespawn();
                            }
                        }
                    }
                }
            }, 0L, 20L);
        }
        
        if (ConfigHelp.getBoolean("wither.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                String worldName = ConfigHelp.getString("wither.world", "world");
                World world = Bukkit.getWorld(worldName);
                
                if (world != null) {
                    int centerX = ConfigHelp.getInt("wither.x", 0);
                    int centerY = ConfigHelp.getInt("wither.y", 100);
                    int centerZ = ConfigHelp.getInt("wither.z", 0);
                    int radius = ConfigHelp.getInt("wither.radius", 50);
                    
                    Location center = new Location(world, centerX, centerY, centerZ);
                    
                    boolean witherExists = world.getEntities().stream()
                            .filter(e -> e instanceof Wither)
                            .anyMatch(e -> e.getLocation().distance(center) <= radius);
                    
                    if (!witherExists) {
                        world.spawnEntity(center, EntityType.WITHER);
                    }
                }
            }, 0L, 100L);
        }
        
        if (ConfigHelp.getBoolean("item-clear.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                CommandExecuter.runConsoleSilently("kill @e[type=item]");
                CommandExecuter.runConsoleSilently("kill @e[type=experience_orb]");
                CommandExecuter.runConsoleSilently("kill @e[type=arrow]");
                CommandExecuter.runConsoleSilently("kill @e[type=spectral_arrow]");
                CommandExecuter.runConsoleSilently("kill @e[type=tipped_arrow]");
                CommandExecuter.runConsoleSilently("kill @e[type=snowball]");
                CommandExecuter.runConsoleSilently("kill @e[type=trident]");
                CommandExecuter.runConsoleSilently("kill @e[type=falling_block]");
            }, 0L, 6000L);
        }
        
        if (ConfigHelp.getBoolean("auto-heal.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double currentHealth = player.getHealth();
                    double maxHealth = player.getMaxHealth();
                    if (currentHealth < maxHealth) {
                        player.setHealth(Math.min(currentHealth + 1.0, maxHealth));
                    }
                }
            }, 0L, 600L);
        }
        
        if (ConfigHelp.getBoolean("remove-banned-items.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp() || 
                        player.hasPermission("op") || 
                        player.hasPermission("axior.mod") || 
                        player.hasPermission("axior.admin") || 
                        player.hasPermission("axior.owner")) {
                        continue;
                    }
                    
                    // Remove banned items using clear command
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " bedrock");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " barrier");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " command_block");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " chain_command_block");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " repeating_command_block");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " debug_stick");
                    CommandExecuter.runConsoleSilently("clear " + player.getName() + " jigsaw");
                }
            }, 0L, 200L);
        }
        
        if (ConfigHelp.getBoolean("auto-save-worlds.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (World world : Bukkit.getWorlds()) {
                    world.save();
                }
            }, 36000L, 36000L);
        }
        
        if (ConfigHelp.getBoolean("remove-annoying-mobs.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                CommandExecuter.runConsoleSilently("kill @e[type=phantom]");
                CommandExecuter.runConsoleSilently("kill @e[type=bat]");
                CommandExecuter.runConsoleSilently("kill @e[type=silverfish]");
            }, 0L, 6000L);
        }
        
        if (ConfigHelp.getBoolean("afk-kick.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                long now = System.currentTimeMillis();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("op") || 
                        p.hasPermission("axior.mod") || 
                        p.hasPermission("axior.admin") || 
                        p.hasPermission("axior.owner") ||
                        p.isOp()) {
                        continue;
                    }
                    
                    long lastAct = lastActivity.getOrDefault(p.getUniqueId(), now);
                    if (now - lastAct > 1800000) {
                        p.kickPlayer("Kicked for being AFK (30 minutes)");
                        lastActivity.remove(p.getUniqueId());
                    }
                }
            }, 0L, 1200L);
        }
        
        if (ConfigHelp.getBoolean("auto-reset-dimensions.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                World nether = Bukkit.getWorld("world_nether");
                World end = Bukkit.getWorld("world_the_end");
                Location overworldSpawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                
                if (nether != null) {
                    for (Player p : nether.getPlayers()) {
                        p.teleport(overworldSpawn);
                    }
                    
                    String netherName = nether.getName();
                    Bukkit.unloadWorld(nether, false);
                    
                    SchedulerHelper.runAsync(this, () -> {
                        java.io.File worldFolder = new java.io.File(Bukkit.getWorldContainer(), netherName);
                        deleteFolder(worldFolder);
                        
                        SchedulerHelper.run(this, () -> {
                            Bukkit.createWorld(new org.bukkit.WorldCreator(netherName)
                                .environment(World.Environment.NETHER));
                        });
                    });
                }
                
                if (end != null) {
                    for (Player p : end.getPlayers()) {
                        p.teleport(overworldSpawn);
                    }
                    
                    String endName = end.getName();
                    Bukkit.unloadWorld(end, false);
                    
                    SchedulerHelper.runAsync(this, () -> {
                        java.io.File worldFolder = new java.io.File(Bukkit.getWorldContainer(), endName);
                        deleteFolder(worldFolder);
                        
                        SchedulerHelper.run(this, () -> {
                            Bukkit.createWorld(new org.bukkit.WorldCreator(endName)
                                .environment(World.Environment.THE_END));
                        });
                    });
                }
            }, 51840000L, 51840000L);
        }
        
        if (ConfigHelp.getBoolean("random-events.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                World world = Bukkit.getWorld("world");
                if (world != null && !Bukkit.getOnlinePlayers().isEmpty()) {
                    int x = Mathematics.randomInt(-5000, 5000);
                    int z = Mathematics.randomInt(-5000, 5000);
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    
                    EntityType[] types = EntityType.values();
                    EntityType randomType = types[Mathematics.randomInt(0, types.length - 1)];
                    
                    CommandExecuter.runConsoleSilently(
                        "summon " + randomType.name().toLowerCase() + " " + x + " " + y + " " + z
                    );
                }
            }, 18000L, 18000L);
            
            SchedulerHelper.runTimer(this, () -> {
                java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
                if (!players.isEmpty()) {
                    Player randomPlayer = players.get(Mathematics.randomInt(0, players.size() - 1));
                    VisualCreator.sendTitle(randomPlayer, "&eLIGHTNING!", "&7You've been struck!", 10, 40, 10);
                    CommandExecuter.runConsoleSilently(
                        "execute at " + randomPlayer.getName() + " run summon lightning_bolt"
                    );
                }
            }, 36000L, 36000L);
            
            SchedulerHelper.runTimer(this, () -> {
                java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
                if (!players.isEmpty()) {
                    Player randomPlayer = players.get(Mathematics.randomInt(0, players.size() - 1));
                    VisualCreator.sendTitle(randomPlayer, "&bLucky!", "&7You received a diamond!", 10, 50, 10);
                    CommandExecuter.runConsoleSilently(
                        "give " + randomPlayer.getName() + " diamond 1"
                    );
                }
            }, 54000L, 54000L);
            
            SchedulerHelper.runTimer(this, () -> {
                java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
                if (!players.isEmpty()) {
                    Player randomPlayer = players.get(Mathematics.randomInt(0, players.size() - 1));
                    VisualCreator.sendTitle(randomPlayer, "&cSURPRISE!", "&7Run!", 5, 30, 10);
                    Location loc = randomPlayer.getLocation();
                    CommandExecuter.runConsoleSilently(
                        "setblock " + loc.getBlockX() + " " + loc.getBlockY() + " " + 
                        loc.getBlockZ() + " tnt"
                    );
                }
            }, 72000L, 72000L);
            
            SchedulerHelper.runTimer(this, () -> {
                java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.size() >= 2) {
                    Player teleporter = players.get(Mathematics.randomInt(0, players.size() - 1));
                    players.remove(teleporter);
                    Player target = players.get(Mathematics.randomInt(0, players.size() - 1));
                    
                    VisualCreator.sendTitle(teleporter, "&dTeleported!", "&7You've been moved!", 10, 40, 10);
                    teleporter.teleport(target.getLocation());
                }
            }, 432000L, 432000L);
            
            SchedulerHelper.runTimer(this, () -> {
                java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
                if (!players.isEmpty()) {
                    String[] hostileMobs = {"zombie", "skeleton", "creeper", "spider", "enderman",
                        "witch", "vindicator", "pillager", "ravager", "blaze", "ghast"};
                    String randomMob = hostileMobs[Mathematics.randomInt(0, hostileMobs.length - 1)];
                    int swarmSize = Mathematics.randomInt(5, 20);
                    
                    Player randomPlayer = players.get(Mathematics.randomInt(0, players.size() - 1));
                    VisualCreator.sendTitle(randomPlayer, "&4MOB SWARM!", "&c" + swarmSize + " " + randomMob + "s incoming!", 10, 60, 10);
                    Location loc = randomPlayer.getLocation();
                    for (int i = 0; i < swarmSize; i++) {
                        int offsetX = Mathematics.randomInt(-10, 10);
                        int offsetZ = Mathematics.randomInt(-10, 10);
                        CommandExecuter.runConsoleSilently(
                            "execute at " + randomPlayer.getName() + 
                            " run summon " + randomMob + " ~" + offsetX + " ~ ~" + offsetZ
                        );
                    }
                }
            }, 216000L, 216000L);
        }
        
        if (ConfigHelp.getBoolean("auto-feed.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getFoodLevel() < 20) {
                        p.setFoodLevel(Math.min(p.getFoodLevel() + 1, 20));
                    }
                }
            }, 0L, 18000L);
        }
        
        if (ConfigHelp.getBoolean("prevent-void-death.enabled", true)) {
            SchedulerHelper.runTimer(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().getY() < -70) {
                        Location safe = p.getWorld().getSpawnLocation();
                        p.teleport(safe);
                        p.setHealth(p.getMaxHealth());
                        p.setFoodLevel(20);
                    }
                }
            }, 0L, 5L);
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        lastActivity.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        lastActivity.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void onDisable() {
        ConsoleLog.info("Eventron disabling...");
    }
    
    private void deleteFolder(java.io.File folder) {
        if (folder.exists()) {
            java.io.File[] files = folder.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }
}
