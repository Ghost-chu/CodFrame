package com.ghostchu.codframe;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public final class CodFrame extends JavaPlugin implements Listener {
    private final NamespacedKey KEY = new NamespacedKey(this, "owner");

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Optional<UUID> queryProtection(ItemFrame frame) {
        String dat = frame.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (dat == null) return Optional.empty();
        UUID uuid = UUID.fromString(dat);
        return Optional.of(uuid);
    }

    public boolean claimProtection(ItemFrame frame, UUID uuid) {
        if (queryProtection(frame).isPresent()) return false;
        frame.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, uuid.toString());
        frame.setFixed(true);
        frame.setInvulnerable(true);
        return true;
    }

    public boolean removeProtection(ItemFrame frame, UUID uuid) {
        if (!queryProtection(frame).isPresent()) return false;
        frame.setFixed(false);
        frame.getPersistentDataContainer().remove(KEY);
        frame.setInvulnerable(false);
        return true;
    }

    public void openBook(ItemFrame frame, Player player) {
        if (frame.getItem().getItemMeta() instanceof BookMeta) {
            BookMeta meta = (BookMeta) frame.getItem().getItemMeta();
            Component author = meta.author();
            Component title = meta.title();
            if (author == null)
                author = Component.text("??????");
            if (title == null)
                title = Component.text("??????");
            player.openBook(Book.book(title, author, meta.pages()));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Entity target = player.getTargetEntity(4);
            if (target instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) target;
                if (claimProtection(frame, player.getUniqueId())) {
                    player.sendMessage(ChatColor.GREEN + "???????????????????????????????????????");
                } else {
                    Optional<UUID> uuid = queryProtection(frame);
                    if (uuid.isPresent()) {
                        if (uuid.get().equals(player.getUniqueId()) || player.hasPermission("codframe.admin")) {
                            removeProtection(frame, player.getUniqueId());
                            player.sendMessage(ChatColor.GREEN + "????????????????????????????????????");
                        } else {
                            player.sendMessage(ChatColor.RED + "??????????????????????????????????????????");
                        }
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "???????????????????????????????????????????????????");
            }
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameDropping(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            if (queryProtection(frame).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameDamaging(EntityDamageEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            if (queryProtection(frame).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameDamaging(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            if (queryProtection(frame).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameDamaging(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getEntity();
            if (queryProtection(frame).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void interactFrame(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            if (queryProtection(frame).isPresent()) {
                event.setCancelled(true);
                if (frame.getItem().hasItemMeta()) {
                    openBook(frame, event.getPlayer());
                    sendChatPreview(frame.getItem(), event.getPlayer());
                }
            }
        }
    }

    private void sendChatPreview(ItemStack item, Player player) {
        player.sendMessage(Component.text("?????????????????????????????????????????????").color(NamedTextColor.AQUA).hoverEvent(item.asHoverEvent()));
    }
}
