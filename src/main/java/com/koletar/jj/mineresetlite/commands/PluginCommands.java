package com.koletar.jj.mineresetlite.commands;

import static com.koletar.jj.mineresetlite.types.Phrases.phrase;

import com.koletar.jj.mineresetlite.commons.commands.Command;
import com.koletar.jj.mineresetlite.MineResetLitePlugin;
import org.bukkit.command.CommandSender;

/**
 * @author jjkoletar
 */
public class PluginCommands {

  private MineResetLitePlugin plugin;

  public PluginCommands(MineResetLitePlugin plugin) {
    this.plugin = plugin;
  }

  @Command(aliases = {
      "about"}, description = "List version and project information about MRL", permissions = {}, help = {
      "Show version information about this installation of MRL, in addition",
      "to the authors of the plugin."}, min = 0, max = 0, onlyPlayers = false)
  public void about(CommandSender sender, String[] args) {
    sender.sendMessage(phrase("aboutTitle"));
    sender.sendMessage(phrase("aboutAuthors"));
    sender.sendMessage(phrase("aboutVersion", plugin.getDescription().getVersion()));
  }
}
