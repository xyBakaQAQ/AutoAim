package com.xybaka.autoaim.modules.render;

import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.StringSetting;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lwjgl.glfw.GLFW;

public class SkinOverlay extends Module {

    public final StringSetting playerName = new StringSetting("PlayerName", "");
    public final BooleanSetting loadCape = new BooleanSetting("LoadCape", true); // 加这行

    private ResourceLocation customSkinLocation = null;
    private boolean isSkinLoaded = false;
    private ResourceLocation customCloakLocation = null;
    private boolean isCloakLoaded = false;

    public SkinOverlay() {
        super("SkinChanger", Category.CLIENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() {
        String name = playerName.getValue().trim();
        if (!name.isEmpty()) {
            fetchAndLoadSkin(name);
        }
    }

    @Override
    public void onDisable() {
        customSkinLocation = null;
        isSkinLoaded = false;
        customCloakLocation = null;
        isCloakLoaded = false;
    }

    public void fetchAndLoadSkin(String username) {
        Thread thread = new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();

                // 1. 用户名 → UUID
                HttpRequest uuidReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                        .build();
                HttpResponse<String> uuidRes = client.send(uuidReq, HttpResponse.BodyHandlers.ofString());
                if (uuidRes.statusCode() != 200) return;

                JsonObject uuidJson = JsonParser.parseString(uuidRes.body()).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();
                String formattedUUID = UUID.fromString(
                        uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")
                ).toString();

                // 2. UUID → 皮肤URL
                HttpRequest profileReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + formattedUUID))
                        .build();
                HttpResponse<String> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofString());
                if (profileRes.statusCode() != 200) return;

                JsonObject profileJson = JsonParser.parseString(profileRes.body()).getAsJsonObject();
                String base64 = profileJson.getAsJsonArray("properties")
                        .get(0).getAsJsonObject()
                        .get("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(base64));
                JsonObject textureJson = JsonParser.parseString(decoded).getAsJsonObject();

                // 3. 下载皮肤
                String skinUrl = textureJson.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url").getAsString();

                HttpRequest skinReq = HttpRequest.newBuilder()
                        .uri(URI.create(skinUrl))
                        .build();
                HttpResponse<InputStream> skinRes = client.send(skinReq, HttpResponse.BodyHandlers.ofInputStream());
                if (skinRes.statusCode() == 200) {
                    InputStream skinStream = skinRes.body();
                    mc.execute(() -> {
                        try {
                            NativeImage image = NativeImage.read(skinStream);
                            DynamicTexture texture = new DynamicTexture(image);
                            ResourceLocation location = new ResourceLocation("autoaim", "skin/" + username.toLowerCase());
                            mc.getTextureManager().register(location, texture);
                            customSkinLocation = location;
                            isSkinLoaded = true;
                        } catch (Exception ignored) {}
                    });
                }

                // 4. 下载披风
                if (loadCape.isEnabled() && textureJson.getAsJsonObject("textures").has("CAPE")) {
                    String capeUrl = textureJson.getAsJsonObject("textures")
                            .getAsJsonObject("CAPE")
                            .get("url").getAsString();

                    HttpRequest capeReq = HttpRequest.newBuilder()
                            .uri(URI.create(capeUrl))
                            .build();
                    HttpResponse<InputStream> capeRes = client.send(capeReq, HttpResponse.BodyHandlers.ofInputStream());
                    if (capeRes.statusCode() == 200) {
                        InputStream capeStream = capeRes.body();
                        mc.execute(() -> {
                            try {
                                NativeImage capeImage = NativeImage.read(capeStream);
                                DynamicTexture capeTexture = new DynamicTexture(capeImage);
                                ResourceLocation capeLocation = new ResourceLocation("autoaim", "cape/" + username.toLowerCase());
                                mc.getTextureManager().register(capeLocation, capeTexture);
                                customCloakLocation = capeLocation;
                                isCloakLoaded = true;
                            } catch (Exception ignored) {}
                        });
                    }
                }

            } catch (Exception ignored) {}
        }, "SkinFetcher");
        thread.setDaemon(true);
        thread.start();
    }

    public ResourceLocation getCustomSkinLocation() { return customSkinLocation; }
    public boolean isSkinLoaded() { return isSkinLoaded; }
    public ResourceLocation getCustomCloakLocation() { return customCloakLocation; }
    public boolean isCloakLoaded() { return isCloakLoaded; }
}