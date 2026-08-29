package com.hite.kickmod.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "kickmod", value = Dist.CLIENT)
public class PlayerAnimationHandler {
    private static final Logger LOGGER = LogManager.getLogger("KickMod");
    private static final Map<UUID, AnimationData> animations = new HashMap<>();
    private static final Map<UUID, float[]> originalRotations = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        animations.values().removeIf(data -> {
            data.timer--;
            return data.timer <= 0;
        });
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        AnimationData data = animations.get(uuid);
        if (data == null) return;

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        if (!originalRotations.containsKey(uuid)) {
            originalRotations.put(uuid, new float[]{
                model.leftLeg.xRot, model.rightLeg.xRot, model.body.xRot,
                model.leftArm.xRot, model.rightArm.xRot, model.head.xRot
            });
        }

        if ("kick_forward".equals(data.animation)) {
            animateKickForward(model, data);
        } else if ("kick_down".equals(data.animation)) {
            animateKickDown(model, data);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        AnimationData data = animations.get(uuid);
        if (data == null) return;

        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
        float[] original = originalRotations.get(uuid);

        if (original != null && data.timer <= 0) {
            model.leftLeg.xRot = original[0];
            model.rightLeg.xRot = original[1];
            model.body.xRot = original[2];
            model.leftArm.xRot = original[3];
            model.rightArm.xRot = original[4];
            model.head.xRot = original[5];
            originalRotations.remove(uuid);
        }
    }

    public static void playAnimation(Player player, boolean lookingDown) {
        if (!(player instanceof AbstractClientPlayer)) return;
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
        UUID uuid = clientPlayer.getUUID();
        String animName = lookingDown ? "kick_down" : "kick_forward";
        animations.put(uuid, new AnimationData(animName, 20));
        LOGGER.info("Playing animation: {} for {}", animName, clientPlayer.getName().getString());
    }

    private static void animateKickForward(PlayerModel<AbstractClientPlayer> model, AnimationData data) {
        float progress = (float) data.timer / 20f;
        float angle = -70f * (1 - progress);
        model.leftLeg.xRot = (float) Math.toRadians(angle);
        model.rightLeg.xRot = (float) Math.toRadians(angle * 0.3f);
        model.body.xRot = (float) Math.toRadians(-angle * 0.5f);
        model.rightArm.xRot = (float) Math.toRadians(angle * 0.6f);
        model.leftArm.xRot = (float) Math.toRadians(-angle * 0.4f);
        model.head.xRot = (float) Math.toRadians(angle * 0.2f);
    }

    private static void animateKickDown(PlayerModel<AbstractClientPlayer> model, AnimationData data) {
        float progress = (float) data.timer / 20f;
        float angle = 60f * (1 - progress);
        model.leftLeg.xRot = (float) Math.toRadians(-angle);
        model.rightLeg.xRot = (float) Math.toRadians(angle * 0.2f);
        model.body.xRot = (float) Math.toRadians(angle * 0.5f);
        model.rightArm.xRot = (float) Math.toRadians(angle * 0.4f);
        model.leftArm.xRot = (float) Math.toRadians(angle * 0.4f);
        model.head.xRot = (float) Math.toRadians(angle * 0.3f);
    }

    private static class AnimationData {
        String animation;
        int timer;
        AnimationData(String animation, int duration) {
            this.animation = animation;
            this.timer = duration;
        }
    }
}