/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.userprefs;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.core.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.interfaces.IModuleDataProvider;
import io.github.nucleuspowered.nucleus.util.TriConsumer;
import io.github.nucleuspowered.storage.dataobjects.keyed.DataKeyImpl;
import org.spongepowered.api.entity.living.player.User;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

public class PreferenceKeyImpl<T> extends DataKeyImpl<T, IUserDataObject> implements NucleusUserPreferenceService.PreferenceKey<T> {

    private final String key;
    @Nullable private final T def;
    private final Class<T> clazz;
    private final BiPredicate<INucleusServiceCollection, User> canAccess;
    private final String descriptionKey;
    private final String module;
    private final TriConsumer<INucleusServiceCollection, UUID, T> onSet;

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            String permission,
            String descriptionKey,
            String module) {
        this(key, def, clazz, permission, descriptionKey, module, (s, u, t) -> {});
    }

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            String permission,
            String descriptionKey,
            String module,
            TriConsumer<INucleusServiceCollection, UUID, T> onSet) {
        this(key,
                def,
                clazz,
                (serviceCollection, user) -> serviceCollection.permissionService().hasPermission(user, permission),
                descriptionKey,
                module,
                onSet);
    }

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            BiPredicate<INucleusServiceCollection, User> canAccess,
            String descriptionKey,
            String module) {
        this(key, def, clazz, canAccess, descriptionKey, module, (s, u, t) -> {});
    }

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            BiPredicate<INucleusServiceCollection, User> canAccess,
            String descriptionKey,
            String module,
            TriConsumer<INucleusServiceCollection, UUID, T> onSet) {
        super(new String[] { "user-prefs", key }, TypeToken.of(clazz), IUserDataObject.class, def);
        this.key = key;
        this.def = def;
        this.clazz = clazz;
        this.canAccess = canAccess;
        this.descriptionKey = descriptionKey;
        this.module = module;
        this.onSet = onSet;
    }

    public String getID() {
        return this.key;
    }

    public Optional<T> getDefaultValue() {
        return Optional.ofNullable(this.def);
    }

    public Class<T> getValueClass() {
        return this.clazz;
    }

    public boolean canAccess(INucleusServiceCollection serviceCollection, User user) {
        return this.canAccess.test(serviceCollection, user);
    }

    public String getDescription(IMessageProviderService messageProviderService) {
        return messageProviderService.getMessageString(this.descriptionKey);
    }

    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    public void onSet(INucleusServiceCollection serviceCollection, UUID uuid, T value) {
        this.onSet.accept(serviceCollection, uuid, value);
    }

    public Optional<NucleusUserPreferenceService.PreferenceKey<T>> getIfLoaded(final IModuleDataProvider provider) {
        if (provider.isLoaded(this.module)) {
            return Optional.of(this);
        }

        return Optional.empty();
    }

    public static class BooleanKey extends PreferenceKeyImpl<Boolean> {

        public BooleanKey(String key, @Nullable Boolean def, String permission, String descriptionKey, String module) {
            super(key, def, Boolean.class, permission, descriptionKey, module);
        }

        public BooleanKey(String key, @Nullable Boolean def, BiPredicate<INucleusServiceCollection, User> canAccess, String descriptionKey, String module) {
            super(key, def, Boolean.class, canAccess, descriptionKey, module);
        }
    }

    public static class LocaleKey extends PreferenceKeyImpl<Locale> {

        public LocaleKey(String key,
                @Nullable Locale def,
                String permission,
                String descriptionKey,
                String module,
                TriConsumer<INucleusServiceCollection, UUID, Locale> serviceCollectionConsumer) {
            super(key, def, Locale.class, permission, descriptionKey, module, serviceCollectionConsumer);
        }

    }
}
