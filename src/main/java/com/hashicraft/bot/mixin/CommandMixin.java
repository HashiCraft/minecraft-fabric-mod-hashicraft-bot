package com.hashicraft.bot.mixin;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

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
          System.out.println("bot_start");
          try {
            startCommand();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return 1;
        });
  }
  
  private LiteralArgumentBuilder<ServerCommandSource> killArgs() {
    return literal("kill").executes(context -> {
          System.out.println("bot_kill");
            try {
              killCommand();
            } catch (IOException e) {
              e.printStackTrace();
            }
          return 1;
        });
  }

  private void startCommand() throws IOException {
    URL u = new URL(botlocation + "/bot");
    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty( "Content-Type", "application/json" );
    conn.setRequestProperty( "Authorization", "bearer " + this.botAPIKey);
    conn.setConnectTimeout(3000);
    conn.setReadTimeout(30000);
    
    InputStream is = conn.getInputStream(); 

    System.out.println(conn.getResponseCode());
  }

  private void killCommand() throws IOException {
    URL u = new URL(botlocation + "/bot/mybot");
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
