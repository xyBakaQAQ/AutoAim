package com.xybaka.autoaim.modules.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.xybaka.autoaim.config.ConfigManager;
import com.xybaka.autoaim.modules.Category;
import com.xybaka.autoaim.modules.Module;
import com.xybaka.autoaim.modules.settings.BooleanSetting;
import com.xybaka.autoaim.modules.settings.StringSetting;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;

public class SkinOverlay extends Module {

    public final StringSetting playerName = new StringSetting("PlayerName", "");
    public final BooleanSetting loadCape = new BooleanSetting("LoadCape", true);

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

                HttpRequest uuidReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                        .build();
                HttpResponse<String> uuidRes = client.send(uuidReq, HttpResponse.BodyHandlers.ofString());
                if (uuidRes.statusCode() != 200) {
                    return;
                }

                JsonObject uuidJson = JsonParser.parseString(uuidRes.body()).getAsJsonObject();
                String playerId = uuidJson.get("id").getAsString();
                File cachedSkinFile = getCachedTextureFile(playerId, "skin");
                File cachedCapeFile = getCachedTextureFile(playerId, "cape");
                boolean shouldLoadCape = loadCape.isEnabled();

                if (cachedSkinFile.isFile()) {
                    loadTextureFromFile(cachedSkinFile, "skin/" + playerId, location -> customSkinLocation = location, () -> isSkinLoaded = true);
                }
                if (shouldLoadCape && cachedCapeFile.isFile()) {
                    loadTextureFromFile(cachedCapeFile, "cape/" + playerId, location -> customCloakLocation = location, () -> isCloakLoaded = true);
                }
                if (cachedSkinFile.isFile() && (!shouldLoadCape || cachedCapeFile.isFile())) {
                    return;
                }

                String formattedUUID = UUID.fromString(
                        playerId.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")
                ).toString();

                HttpRequest profileReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + formattedUUID))
                        .build();
                HttpResponse<String> profileRes = client.send(profileReq, HttpResponse.BodyHandlers.ofString());
                if (profileRes.statusCode() != 200) {
                    return;
                }

                JsonObject profileJson = JsonParser.parseString(profileRes.body()).getAsJsonObject();
                String base64 = profileJson.getAsJsonArray("properties")
                        .get(0).getAsJsonObject()
                        .get("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(base64));
                JsonObject textureJson = JsonParser.parseString(decoded).getAsJsonObject();
                JsonObject textures = textureJson.getAsJsonObject("textures");

                if (!cachedSkinFile.isFile() && textures.has("SKIN")) {
                    String skinUrl = textures.getAsJsonObject("SKIN").get("url").getAsString();
                    if (downloadTexture(client, skinUrl, cachedSkinFile)) {
                        loadTextureFromFile(cachedSkinFile, "skin/" + playerId, location -> customSkinLocation = location, () -> isSkinLoaded = true);
                    }
                }

                if (shouldLoadCape && !cachedCapeFile.isFile() && textures.has("CAPE")) {
                    String capeUrl = textures.getAsJsonObject("CAPE").get("url").getAsString();
                    if (downloadTexture(client, capeUrl, cachedCapeFile)) {
                        loadTextureFromFile(cachedCapeFile, "cape/" + playerId, location -> customCloakLocation = location, () -> isCloakLoaded = true);
                    }
                }
            } catch (Exception ignored) {
            }
        }, "SkinFetcher");
        thread.setDaemon(true);
        thread.start();
    }

    private File getSkinCacheDir() {
        File cacheDir = new File(ConfigManager.instance.getConfigDir(), "Skin");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private File getCachedTextureFile(String playerId, String textureType) {
        return new File(getSkinCacheDir(), playerId + "_" + textureType + ".png");
    }

    private boolean downloadTexture(HttpClient client, String textureUrl, File outputFile) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(textureUrl))
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                return false;
            }

            Files.createDirectories(outputFile.toPath().getParent());
            try (InputStream stream = response.body()) {
                Files.copy(stream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void loadTextureFromFile(File textureFile, String resourcePath, Consumer<ResourceLocation> locationConsumer, Runnable loadedCallback) {
        mc.execute(() -> {
            try (InputStream stream = Files.newInputStream(textureFile.toPath())) {
                NativeImage image = NativeImage.read(stream);
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = new ResourceLocation("autoaim", resourcePath);
                mc.getTextureManager().register(location, texture);
                locationConsumer.accept(location);
                loadedCallback.run();
            } catch (Exception ignored) {
            }
        });
    }

    public ResourceLocation getCustomSkinLocation() { return customSkinLocation; }
    public boolean isSkinLoaded() { return isSkinLoaded; }
    public ResourceLocation getCustomCloakLocation() { return customCloakLocation; }
    public boolean isCloakLoaded() { return isCloakLoaded; }
}