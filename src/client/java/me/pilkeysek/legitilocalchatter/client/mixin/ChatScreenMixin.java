package me.pilkeysek.legitilocalchatter.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.pilkeysek.legitilocalchatter.client.LegitilocalchatterClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "sendMessage(Ljava/lang/String;Z)V", cancellable = true)
    public void injected(String chatText, boolean addToHistory, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> refChatText) {
        if(!LegitilocalchatterClient.ENABLED) return;

        if(!refChatText.get().startsWith("/") && !this.normalize(refChatText.get()).isEmpty()) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(refChatText.get());
            String newChatText = LegitilocalchatterClient.CHAT_PREFIX + refChatText.get();
            refChatText.set(newChatText);
            assert MinecraftClient.getInstance().player != null;
            if(newChatText.startsWith("/")) {
                MinecraftClient.getInstance().player.networkHandler.sendChatCommand(newChatText.substring(1));
            }
            else {
                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(newChatText);
            }
            ci.cancel();
        }
    }

    @Unique
    private String normalize(String chatText) {
        return StringHelper.truncateChat((String) StringUtils.normalizeSpace(chatText.trim()));
    }
}
