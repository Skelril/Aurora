package com.skelril.aurora.jail;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.CommandBookUtil;
import com.sk89q.commandbook.util.PlayerUtil;
import com.sk89q.minecraft.util.commands.*;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Author: Turtle9598
 */
@ComponentInformation(friendlyName = "Jail", desc = "Jail System")
@Depend(plugins = {"WorldEdit"}, components = {AdminComponent.class})
public class JailComponent extends BukkitComponent implements Listener, Runnable {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    @InjectComponent
    private AdminComponent adminComponent;

    private InmateDatabase inmates;
    private JailCellDatabase jailCells;
    private LocalConfiguration config;
    private Map<Player, JailCell> cells = new HashMap<>();

    @Override
    public void enable() {

        //super.enable();
        config = configure(new LocalConfiguration());

        // Setup the inmates database
        File jailDirectory = new File(inst.getDataFolder().getPath() + "/jail");
        if (!jailDirectory.exists()) jailDirectory.mkdir();

        inmates = new CSVInmateDatabase(jailDirectory);
        jailCells = new CSVJailCellDatabase(jailDirectory);
        inmates.load();
        jailCells.load();
        registerCommands(Commands.class);
        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
        server.getScheduler().scheduleSyncRepeatingTask(CommandBook.inst(), this, 20 * 2, 20 * 2);
    }

    @Override
    public void reload() {

        super.reload();
        inmates.load();
        jailCells.load();
        configure(config);
    }

    @Override
    public void disable() {

        inmates.unload();
        jailCells.unload();
    }

    private static class LocalConfiguration extends ConfigurationBase {

        @Setting("message")
        public String jailMessage = "You have been jailed";
        @Setting("broadcast-jails")
        public boolean broadcastJails = true;
        @Setting("move-threshold")
        public int moveThreshold = 8;
        @Setting("free-spots-held")
        public int freeSpotsHeld = 2;
    }

    /**
     * Get the inmate database.
     *
     * @return Inmates
     */
    public InmateDatabase getInmateDatabase() {

        return inmates;
    }

    /**
     * Get the jailcell database.
     *
     * @return Jail cells
     */
    public JailCellDatabase getJailCellDatabase() {

        return jailCells;
    }

    public void jail(String name, long time) {

        jail(name, time, false);
    }

    public void jail(String name, long time, boolean mute) {

        getInmateDatabase().jail(name, "lava-flow", server.getConsoleSender(), "", System.currentTimeMillis() + time, mute);
    }

    public boolean isJailed(Player player) {

        return isJailed(player.getName());
    }

    public boolean isJailed(String name) {

        return getInmateDatabase().isJailedName(name);
    }

    public boolean isJailMuted(Player player) {

        return isJailMuted(player.getName());
    }

    public boolean isJailMuted(String name) {

        return isJailed(name) && getInmateDatabase().getJailedName(name).isMuted();
    }

    @Override
    public void run() {

        for (Player player : server.getOnlinePlayers()) {
            try {

                if (getInmateDatabase().isJailedName(player.getName())) {

                    if (!player.isOnline() && cells.containsKey(player)) {
                        cells.remove(player);
                        continue;
                    }

                    if (player.isOnline() && !cells.containsKey(player)) {
                        Inmate inmate = getInmateDatabase().getJailedName(player.getName());
                        assignCell(player, inmate.getPrisonName());
                    }

                    adminComponent.standardizePlayer(player, true);
                    player.setFoodLevel(5);

                    JailCell cell = cells.get(player);
                    if (cell == null) {
                        player.kickPlayer("Unable to find a jail cell...");
                        continue;
                    }

                    Location loc = player.getLocation();
                    Location cellLoc = cell.getLocation();
                    if (player.getWorld() != cellLoc.getWorld() || loc.distanceSquared(cellLoc) > (config.moveThreshold * config.moveThreshold)) {
                        player.teleport(cell.getLocation(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    }

                    if (player.isOnline() && server.getMaxPlayers() - server.getOnlinePlayers().length <= config.freeSpotsHeld) {
                        player.kickPlayer("You are not currently permitted to be online!");
                    }
                }
            } catch (Exception e) {
                log.warning("Could not find a cell for the player: " + player.getName() + ".");
                player.kickPlayer("Kicked!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (isJailed(player)) {
            ChatUtil.sendWarning(player, "Your jail sentence does not permit this action!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        if (isJailed(player)) {
            ChatUtil.sendWarning(player, "Your jail sentence does not permit this action!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {

        Entity entity = event.getEntity();
        if (entity instanceof Player && isJailed((Player) entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        if (isJailed(player)) {
            ChatUtil.sendWarning(player, "Your jail sentence does not permit this action!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        if (isJailMuted(player)) {
            ChatUtil.sendWarning(player, "Your jail sentence does not permit this action!");
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {

        if (isJailed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public class Commands {

        @Command(aliases = {"jail"}, usage = "[-t end] <target> <prison> [reason...]",
                desc = "Jail a player", flags = "mset:o", min = 2, max = -1)
        @CommandPermissions({"aurora.jail.jail"})
        public void jailCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player inmate = null;
            String inmateName = "";
            String prisonName = args.getString(1);
            long endDate = args.hasFlag('t') ? CommandBookUtil.matchFutureDate(args.getFlag('t')) : 0L;
            String message = args.argsLength() >= 3 ? args.getJoinedStrings(1) : "Jailed!";

            final boolean hasExemptOverride = args.hasFlag('o') && inst.hasPermission(sender, "aurora.jail.exempt.override");

            // Check if it's a player in the server right now
            try {
                // Exact mode matches names exactly
                if (args.hasFlag('e')) {
                    inmate = PlayerUtil.matchPlayerExactly(sender, args.getString(0));
                } else {
                    inmate = PlayerUtil.matchSinglePlayer(sender, args.getString(0));
                }

                inmateName = inmate.getName();

                // They are offline
            } catch (CommandException e) {
                inmateName = args.getString(0).replace("\r", "").replace("\n", "").replace("\0", "").replace("\b", "");
            }

            if (!hasExemptOverride) {
                try {
                    if (inst.hasPermission(inmate, "aurora.jail.exempt")) {
                        if (inst.hasPermission(sender, "aurora.jail.exempt.override")) {
                            throw new CommandException("The player: " + inmateName + " is exempt from being jailed! (use -o flag to override this)");
                        } else {
                            throw new CommandException("The player: " + inmateName + " is exempt from being jailed!");
                        }
                    }
                } catch (NullPointerException npe) {
                    if (inst.hasPermission(sender, "aurora.jail.exempt.override")) {
                        throw new CommandException("The player: " + inmateName + " is offline, and cannot be jailed! (use -o flag to override this)");
                    } else {
                        throw new CommandException("The player: " + inmateName + " is offline, and cannot be jailed!");
                    }
                }
            }

            if (!getJailCellDatabase().prisonExist(prisonName)) throw new CommandException("No such prison exists.");


            // Jail the player
            getInmateDatabase().jail(inmateName, prisonName, sender, message, endDate, args.hasFlag('m'));

            // Tell the sender of their success
            ChatUtil.sendNotice(sender, "The player: " + inmateName + " has been jailed!");

            if (!getInmateDatabase().save()) {
                throw new CommandException("Inmate database failed to save. See console.");
            }

            // Broadcast the Message
            if (config.broadcastJails && !args.hasFlag('s')) {
                ChatUtil.sendNotice(server.getOnlinePlayers(), sender.getName() + " has jailed " + inmateName + " - " + message);
            }
        }

        @Command(aliases = {"unjail"}, usage = "<target> [reason...]", desc = "Unjail a player", min = 1, max = -1)
        @CommandPermissions({"aurora.jail.unjail"})
        public void unjailCmd(CommandContext args, CommandSender sender) throws CommandException {

            String message = args.argsLength() >= 2 ? args.getJoinedStrings(1) : "Unjailed!";

            String inmateName = args.getString(0).replace("\r", "").replace("\n", "").replace("\0", "").replace("\b", "");

            if (getInmateDatabase().unjail(inmateName, sender, message)) {
                ChatUtil.sendNotice(sender, inmateName + " unjailed.");

                if (!getInmateDatabase().save()) {
                    throw new CommandException("Inmate database failed to save. See console.");
                }
            } else {
                ChatUtil.sendError(sender, inmateName + " was not jailed.");
            }
        }

        @Command(aliases = {"cells"}, desc = "Jail Cell management")
        @NestedCommand({ManagementCommands.class})
        public void cellCmds(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class ManagementCommands {

        @Command(aliases = {"addcell"}, usage = "<prison> <name>", desc = "Create a cell", min = 2, max = 2)
        @CommandPermissions({"aurora.jail.cells.add"})
        public void addCellCmd(CommandContext args, CommandSender sender) throws CommandException {

            if (sender instanceof Player) {
                String prisonName = args.getString(0);
                String cellName = args.getString(1);
                Player player = (Player) sender;
                Location loc = player.getLocation();

                if (getJailCellDatabase().cellExist(prisonName, cellName)) {
                    throw new CommandException("Cell already exists!");
                }

                getJailCellDatabase().createJailCell(prisonName, cellName, player, loc);

                ChatUtil.sendNotice(sender, "Cell '" + cellName + "' created.");

                if (!getJailCellDatabase().save()) {
                    throw new CommandException("Inmate database failed to save. See console.");
                }
            } else {
                throw new CommandException("You must be a player to use this command.");
            }
        }

        @Command(aliases = {"del", "delete", "remove", "rem"}, usage = "<prison> <cell>",
                desc = "Remove a cell", min = 2, max = 2)
        @CommandPermissions({"aurora.jail.cells.remove"})
        public void removeCmd(CommandContext args, CommandSender sender) throws CommandException {

            String prisonName = args.getString(0);
            String cellName = args.getString(1);

            if (!getJailCellDatabase().deleteJailCell(prisonName, cellName, sender) || !getJailCellDatabase().save()) {
                throw new CommandException("No such cell could be successfully found/removed in that prison!");
            }
            ChatUtil.sendNotice(sender, "Cell '" + cellName + "' deleted.");
        }
    }

    private void assignCell(Player player, String prisonName) {

        Map<String, JailCell> prison = getJailCellDatabase().getPrison(prisonName);

        if (prison.size() > 1) {
            cells.put(player, prison.values().toArray(new JailCell[prison.size()])[ChanceUtil.getRandom(prison.size() - 1)]);
        } else {
            cells.put(player, null);
        }
    }
}
