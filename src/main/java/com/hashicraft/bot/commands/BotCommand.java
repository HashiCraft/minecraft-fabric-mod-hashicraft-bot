import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

final class BotCommand implements Command<Object> {
    @Override
    public int run(CommandContext<Object> context) {
      System.out.println("Hello World");
      return 0;
    }
}
