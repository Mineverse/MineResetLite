package com.koletar.jj.mineresetlite;

import com.koletar.jj.mineresetlite.commands.MineCommands;
import com.koletar.jj.mineresetlite.commands.PluginCommands;
import com.koletar.jj.mineresetlite.commons.commands.CommandManager;
import com.koletar.jj.mineresetlite.commons.config.SerializableBlock;
import com.koletar.jj.mineresetlite.listeners.MineListener;
import com.koletar.jj.mineresetlite.types.Mine;
import com.koletar.jj.mineresetlite.types.Phrases;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * @author jjkoletar
 */
public class MineResetLitePlugin extends JavaPlugin {
  public static MineResetLitePlugin instance;
  @Getter
  public List<Mine> mines;
  private Logger logger;
  private CommandManager commandManager;
  private WorldEditPlugin worldEdit = null;
  private int saveTaskId = -1;
  private int resetTaskId = -1;

  @Override
  public void onEnable() {
    MineResetLitePlugin.instance = this;
    mines = new ArrayList<>();
    logger = getLogger();
    Bukkit.getLogger().info(
        "[MineResetLite] MRL is managed and developed by Boomclaw under the Apache 2.0 license.");
    Bukkit.getLogger().info(
        "[MineResetLite] For any issues or suggestions, use the Spigot discussion thread. Do not PM any of the developers.");

    if (!setupConfig()) {
      logger.severe("Error while loading configuration.");
      logger.severe("Plugin initlization disabled");
      return;
    }

    commandManager = new CommandManager();
    commandManager.register(MineCommands.class, new MineCommands(this));
    commandManager.register(CommandManager.class, commandManager);
    commandManager.register(PluginCommands.class, new PluginCommands(this));
    Locale locale = new Locale(Config.getLocale());
    Phrases.getInstance().initialize(locale);
    File overrides = new File(getDataFolder(), "phrases.properties");

    if (overrides.exists()) {
      Properties overridesProps = new Properties();
      try {
        overridesProps.load(new FileInputStream(overrides));
      } catch (IOException e) {
        e.printStackTrace();
      }
      Phrases.getInstance().overrides(overridesProps);
    }

    // Look for worldedit
    if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
      worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }

    ConfigurationSerialization.registerClass(Mine.class);

    // Load mines
    File[] mineFiles = new File(getDataFolder(), "mines").listFiles(new IsMineFile());
    for (File file : mineFiles) {
      logger.info("Loading mine from file '" + file.getName() + "'...");
      FileConfiguration fileConf = YamlConfiguration.loadConfiguration(file);
      try {
        Object o = fileConf.get("mine");
        if (!(o instanceof Mine)) {
          logger.severe("Mine wasn't a mine object! Something is off with serialization!");
          continue;
        }
        Mine mine = (Mine) o;
        mines.add(mine);
      } catch (Throwable t) {
        logger.severe("Unable to load mine!");
      }
    }

    Bukkit.getPluginManager().registerEvents(new MineListener(this), this);

    resetTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      public void run() {
        for (Mine mine : mines) {
          mine.cron();
        }
      }
    }, 60 * 20L, 60 * 20L);

    if (getServer().getPluginManager().getPlugin("PrisonMines") != null) {
      convertPrisonMines();
      getServer().getPluginManager()
          .disablePlugin(getServer().getPluginManager().getPlugin("PrisonMines"));
    }
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTask(resetTaskId);
    getServer().getScheduler().cancelTask(saveTaskId);
    HandlerList.unregisterAll((Plugin) this);
    logger.info("MineResetLite disabled");
  }


  public static void broadcast(String message, Mine mine) {
    if (Config.getBroadcastNearbyOnly()) {
      for (Player p : mine.getWorld().getPlayers()) {
        if (mine.isInside(p)) {
          p.sendMessage(message);
        }
      }
      if (MineResetLitePlugin.instance.getConfig().getBoolean("consoleLogMineReset", false)) {
        Bukkit.getLogger().info(message);
      }
    } else if (Config.getBroadcastInWorldOnly()) {
      for (Player p : mine.getWorld().getPlayers()) {
        p.sendMessage(message);
      }
      if (MineResetLitePlugin.instance.getConfig().getBoolean("consoleLogMineReset", false)) {
        Bukkit.getLogger().info(message);
      }
    } else {
      for (Player p : Bukkit.getServer().getOnlinePlayers()) {
        p.sendMessage(message);
      }
    }
  }

  public Material matchMaterial(String name) {
    if (name.equalsIgnoreCase("diamondore")) {
      return Material.DIAMOND_ORE;
    } else if (name.equalsIgnoreCase("diamondblock")) {
      return Material.DIAMOND_BLOCK;
    } else if (name.equalsIgnoreCase("ironore")) {
      return Material.IRON_ORE;
    } else if (name.equalsIgnoreCase("ironblock")) {
      return Material.IRON_BLOCK;
    } else if (name.equalsIgnoreCase("goldore")) {
      return Material.GOLD_ORE;
    } else if (name.equalsIgnoreCase("goldblock")) {
      return Material.GOLD_BLOCK;
    } else if (name.equalsIgnoreCase("coalore")) {
      return Material.COAL_ORE;
    } else if (name.equalsIgnoreCase("cake") || name.equalsIgnoreCase("cakeblock")) {
      return Material.CAKE_BLOCK;
    } else if (name.equalsIgnoreCase("emeraldore")) {
      return Material.EMERALD_ORE;
    } else if (name.equalsIgnoreCase("emeraldblock")) {
      return Material.EMERALD_BLOCK;
    } else if (name.equalsIgnoreCase("lapisore")) {
      return Material.LAPIS_ORE;
    } else if (name.equalsIgnoreCase("lapisblock")) {
      return Material.LAPIS_BLOCK;
    } else if (name.equalsIgnoreCase("snowblock") || name.equalsIgnoreCase("snow")) {
      return Material.SNOW_BLOCK;
    } else if (name.equalsIgnoreCase("redstoneore")) {
      return Material.REDSTONE_ORE;
    }
    return Material.matchMaterial(name);
  }

  public Mine[] matchMines(String in) {
    List<Mine> matches = new LinkedList<Mine>();
    for (Mine mine : mines) {
      if (mine.getName().toLowerCase().equals(in.toLowerCase())) {
        matches.add(mine);
      }
    }
    return matches.toArray(new Mine[matches.size()]);
  }

  /**
   * Alert the plugin that changes have been made to mines, but wait 60 seconds before we save. This
   * process saves on disk I/O by waiting until a long string of changes have finished before
   * writing to disk.
   */
  public void buffSave() {
    BukkitScheduler scheduler = getServer().getScheduler();

    if (saveTaskId != -1) {
      // Cancel old task
      scheduler.cancelTask(saveTaskId);
    }

    // Schedule save
    final MineResetLitePlugin plugin = this;

    scheduler.scheduleSyncDelayedTask(this, new Runnable() {
      public void run() {
        plugin.save();
      }
    }, 60 * 20L);
  }

  public void save() {
    for (Mine mine : mines) {
      File mineFile = getMineFile(mine);
      FileConfiguration mineConf = YamlConfiguration.loadConfiguration(mineFile);
      mineConf.set("mine", mine);
      try {
        mineConf.save(mineFile);
      } catch (IOException e) {
        logger.severe("Unable to serialize mine!");
        e.printStackTrace();
      }
    }
  }

  public File getMineFile(Mine mine) {
    return new File(new File(getDataFolder(), "mines"),
        mine.getName().replace(" ", "") + ".mine.yml");
  }

  public void eraseMine(Mine mine) {
    mines.remove(mine);
    getMineFile(mine).delete();
  }

  public Mine getMine(Player player) {
    for (Mine mine : mines) {
      if (mine.isInside(player)) {
        return mine;
      }
    }

    return null;
  }

  public boolean hasWorldEdit() {
    return worldEdit != null;
  }

  public WorldEditPlugin getWorldEdit() {
    return worldEdit;
  }

  private boolean setupConfig() {
    File pluginFolder = getDataFolder();
    if (!pluginFolder.exists() && !pluginFolder.mkdir()) {
      logger.severe("Could not make plugin folder! This won't end well...");
      return false;
    }
    File mineFolder = new File(getDataFolder(), "mines");
    if (!mineFolder.exists() && !mineFolder.mkdir()) {
      logger.severe("Could not make mine folder! Abort! Abort!");
      return false;
    }
    try {
      Config.initConfig(getDataFolder());
    } catch (IOException e) {
      logger.severe("Could not make config file!");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("mineresetlite")) {
      if (args.length == 0) {
        String[] helpArgs = new String[0];
        commandManager.callCommand("help", sender, helpArgs);
        return true;
      }
      // Spoof args array to account for the initial subcommand
      // specification
      String[] spoofedArgs = new String[args.length - 1];
      for (int i = 1; i < args.length; i++) {
        spoofedArgs[i - 1] = args[i];
      }
      commandManager.callCommand(args[0], sender, spoofedArgs);
      return true;
    }
    return false; // Fallthrough
  }

  private void convertPrisonMines() {
    for (File file : getFilesInsideFolder(
        new File(getServer().getPluginManager().getPlugin("PrisonMines").getDataFolder(),
            "mines"))) {
      FileConfiguration config = YamlConfiguration.loadConfiguration(file);

      String name = config.getString("Name");
      String[] coords = config.getString("Region").split(",");
      String world = coords[0];
      int maxX = Integer.parseInt(coords[1]);
      int maxY = Integer.parseInt(coords[2]);
      int maxZ = Integer.parseInt(coords[3]);
      int minX = Integer.parseInt(coords[4]);
      int minY = Integer.parseInt(coords[5]);
      int minZ = Integer.parseInt(coords[6]);

      Mine mine = new Mine(minX, minY, minZ, maxX, maxY, maxZ, name, Bukkit.getWorld(world));
      mine.setSilence(false);
      mine.setFillMode(false);
      mine.setResetDelay(0);

      for (String blockComposition : config.getStringList("Blocks")) {
        int id = Material.getMaterial(blockComposition.split("@")[0]).getId();
        int percentage = Integer.parseInt(blockComposition.split("@")[1].split(":")[1]);
        mine.getComposition().put(new SerializableBlock(id), Double.valueOf("0." + percentage));
      }

      Bukkit.getLogger().info("[MineResetLite] Converted " + file.getName() + ", deleting file...");
      file.delete();
    }

    Bukkit.getLogger()
        .info(
            "[MineResetLite] PrisonMines conversion complete - please remove the folder PrisonMines. Disabling plugin...");
  }

  private List<File> getFilesInsideFolder(File parentFile) {
    List<File> results = new ArrayList<File>();

    File[] files = parentFile.listFiles();

    if (parentFile.listFiles() != null) {
      for (File file : files) {
        if (file.isFile()) {
          results.add(file);
        }
      }
    }

    return results;
  }


  public void handleBlockBreak(Block block) {
    for (Mine mine : this.mines) {
      mine.breakBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }
  }



  private static class IsMineFile implements FilenameFilter
  {

    public boolean accept(File file, String s) {
      return s.contains(".mine.yml");
    }
  }

}
