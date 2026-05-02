package cc.cassian.clickthrough;

import cc.cassian.clickthrough.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class ClickThrough {
    public static final String MOD_ID = "clickthrough";
    public static final String MOD_NAME = "ClickThrough";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static ModConfig CONFIG;

    public static boolean isDyeOnSign = false;

    // Keybind: F9 to toggle on/off
    public static final KeyBinding TOGGLE_KEY = new KeyBinding(
            "key.clickthrough.toggle",
            GLFW.GLFW_KEY_F9,
            "key.categories.clickthrough"
    );

    public static void init(net.minecraft.util.registry.Registry<?> ignored) {
        // called after config is loaded
    }

    public static void setActive(boolean active) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            TranslationTextComponent msg = active
                    ? new TranslationTextComponent("clickthrough.msg.active")
                    : new TranslationTextComponent("clickthrough.msg.inactive");
            if (CONFIG.displayActiveTextAsTitle) {
                Minecraft.getInstance().gui.setOverlayMessage(msg, false);
            } else {
                player.sendMessage(msg, player.getUUID());
            }
        }
        CONFIG.isActive = active;
        CONFIG.save();
    }
}
