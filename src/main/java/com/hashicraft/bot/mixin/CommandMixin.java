package com.hashicraft.bot.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import static net.minecraft.server.command.CommandManager.literal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandMixin {

  @Shadow
  @Final
  private CommandDispatcher<ServerCommandSource> dispatcher;

  private String botlocation = System.getenv("BOT_ADDRESS");
  private String botAPIKey = System.getenv("BOT_API_KEY");
  
  private String activeBot = "";

  @Inject(method = "<init>", at = @At("RETURN"))
  private void onRegister(CommandManager.RegistrationEnvironment arg, CallbackInfo ci) {
    System.out.println("Registering custom commands");

    if(this.botlocation == "") { 
      this.botlocation = "http://localhost:3000";
    }

    this.dispatcher.register(
        literal("hashicraft")
        .then(literal("bot")
        .then(startArgs())
        .then(killArgs())
        )
      );
  }

  private LiteralArgumentBuilder<ServerCommandSource> startArgs() {
    return literal("start").executes(context -> {
      CompletableFuture.runAsync(() -> {
          System.out.println("bot_start: " + this.activeBot);
          if(this.activeBot != "") {
            context.getSource().sendError(new LiteralText(
                "A bot is already running on the server, " +
                "use the kill command to remove it before creating a new bot"));

            return;
          }

          try {
            String id = startCommand();
            this.activeBot = id;
          } catch (IOException e) {
            e.printStackTrace();
          }
      });

      return 1;
    });
  }
  
  private LiteralArgumentBuilder<ServerCommandSource> killArgs() {
    return literal("kill").executes(context -> {
      CompletableFuture.runAsync(() -> {
          if (this.activeBot == "") { 
            context.getSource().sendError(new LiteralText(
                  "There are no bots running on this server"
                  ));
            return;
          }
              
          String id = this.activeBot;
          System.out.println("bot_kill: " + id);
          this.activeBot = "";

          try {
            killCommand(id);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        
        return 1;
      });
  }

  private String startCommand() throws IOException {
    URL u = new URL(botlocation + "/bot");
    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty( "Content-Type", "application/json" );
    conn.setRequestProperty( "Authorization", "bearer " + this.botAPIKey);
    conn.setConnectTimeout(3000);
    conn.setReadTimeout(30000);
    
    System.out.println(conn.getResponseCode());
    
    InputStream is = conn.getInputStream(); 
    Reader reader = new InputStreamReader(is);

    JsonParser jsonParser = new JsonParser();
    JsonElement je = jsonParser.parse(reader);

    String id = je.getAsJsonObject().get("id").getAsString();

    System.out.println("response " + je.toString());
    System.out.println("id " + id);

    return id;
  }

  private void killCommand(String botID) throws IOException {

    URL u = new URL(botlocation + "/bot/" + botID);
    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty( "Content-Type", "application/json" );
    conn.setRequestProperty( "Authorization", "bearer " + this.botAPIKey);
    conn.setConnectTimeout(3000);
    conn.setReadTimeout(30000);
    
    InputStream is = conn.getInputStream(); 

    System.out.println(conn.getResponseCode());
  }
}
