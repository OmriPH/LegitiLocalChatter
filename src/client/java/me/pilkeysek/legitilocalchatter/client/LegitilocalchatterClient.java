package me.pilkeysek.legitilocalchatter.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.Objects;

public class LegitilocalchatterClient implements ClientModInitializer {

    public static boolean ENABLED = false;
    public static Logger LOGGER;
    public static String LOGGER_NAMESPACE = "LegitiLocalChatter";
    public static String CHAT_PREFIX;
    public static final String VERSION = "0.2"; // Make sure to update this when the version changes

    private static KeyBinding keyBinding;

    public static File configFile;


    @Override
    public void onInitializeClient() {
        LOGGER = LogManager.getLogger(LOGGER_NAMESPACE);
        LOGGER.info("Loaded LegitiLocalChatter version " + VERSION);

        keyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.legitilocalchatter.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_X,
                        "category.legitilocalchatter.main"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while(keyBinding.wasPressed()) {
                assert MinecraftClient.getInstance().player != null;
                ENABLED = !ENABLED;
                MinecraftClient.getInstance().player.sendMessage(Text.translatable("chat.legitilocalchatter.legitilocalchatterIsNow").formatted(Formatting.AQUA).append(ENABLED ? Text.translatable("chat.legitilocalchatter.enabled").formatted(Formatting.GREEN) : Text.translatable("chat.legitilocalchatter.disabled").formatted(Formatting.RED)));
            }
        });

        configFile = new File(FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString() + "/legitilocalchatter");

        if(!configFile.exists()) {
            LOGGER.info("Hey, it looks like this is the first time running LegitiLocalChatter!");
            LOGGER.info("Configure the keybind to toggle this mod on and off in the control settings.");
            LOGGER.info("You can also change the text to prepend to a message in the config: " + FabricLoader.getInstance().getConfigDir().toAbsolutePath().toString() + "/legitilocalchatter");
            try {
                if(!configFile.createNewFile()) {
                    LOGGER.warn("It looks like the LegitiLocalChatter config file already exists, but it should be fine");
                }
                FileWriter writer = new FileWriter(configFile);
                writer.write("/lc ");
                writer.close();
            } catch (IOException e) {
                LOGGER.error("Failed to create the LegitiLocalChatter config file :( Here's the stacktrace:");
                LOGGER.error(e);
            }
            CHAT_PREFIX = "/lc ";
        }
        else {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(configFile));
            } catch (FileNotFoundException e) {
                LOGGER.error("Couldn't find the LegitiLocalChatter config file :( - Here's the stacktrace:");
                LOGGER.error(e.toString());
            }
            try {
                CHAT_PREFIX = Objects.requireNonNull(reader).readLine();
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Failed to read the LegitiLocalChatter config file :( - Here's the stacktrace:");
                LOGGER.error(e.toString());
            }
            finally {
                try {
                    Objects.requireNonNull(reader).close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close the BufferedReader :( - Here's the stacktrace:");
                    LOGGER.error(e.toString());
                }
            }

        }
    }
}
