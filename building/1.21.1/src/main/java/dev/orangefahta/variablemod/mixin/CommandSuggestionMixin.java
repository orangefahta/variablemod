package dev.orangefahta.variablemod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.orangefahta.variablemod.storage.BuiltinVariables;
import dev.orangefahta.variablemod.storage.VariableStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Инжект автоподставления.
 * @author @OrangeFaHTA
 * @version 1.0.0
 */
@Mixin(value = CommandDispatcher.class, remap = false)
public abstract class CommandSuggestionMixin {

    @Inject(
        method = "getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;",
        at = @At("RETURN"),
        cancellable = true,
        require = 0
    )
    private <S> void varmod_appendVarSuggestions(
            ParseResults<S> parse,
            int cursor,
            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        try {
            String fullInput = parse.getReader().getString();
            String inputToCursor = cursor <= fullInput.length() ? fullInput.substring(0, cursor) : fullInput;

            // Find the last word being typed
            int lastSpace = inputToCursor.lastIndexOf(' ');
            String currentWord = inputToCursor.substring(lastSpace + 1);

            boolean isBuiltin = currentWord.startsWith("v!:");
            boolean isUser    = !isBuiltin && currentWord.startsWith("v:");

            if (!isBuiltin && !isUser) return;

            int start = lastSpace + 1;
            SuggestionsBuilder builder = new SuggestionsBuilder(fullInput, start);

            if (isBuiltin) {
                String prefix = currentWord.substring(3);
                for (String name : BuiltinVariables.names()) {
                    if (name.startsWith(prefix)) builder.suggest("v!:" + name);
                }
            } else {
                String prefix = currentWord.substring(2);
                for (String name : VariableStorage.names()) {
                    if (name.startsWith(prefix)) builder.suggest("v:" + name);
                }
            }

            Suggestions built = builder.build();
            if (built.isEmpty()) return;

            CompletableFuture<Suggestions> existing = cir.getReturnValue();
            Suggestions finalBuilt = built;
            cir.setReturnValue(existing.thenApply(existingSuggestions ->
                Suggestions.merge(fullInput, java.util.Arrays.asList(existingSuggestions, finalBuilt))
            ));
        } catch (Exception ignored) {

        }
    }
}
