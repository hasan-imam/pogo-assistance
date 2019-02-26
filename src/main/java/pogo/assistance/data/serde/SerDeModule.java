package pogo.assistance.data.serde;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import pogo.assistance.data.serde.gson.DurationTypeAdapter;
import pogo.assistance.data.serde.gson.GsonAdaptersLength;
import pogo.assistance.data.serde.gson.GsonAdaptersPoint;

@Module
public class SerDeModule {

    @Provides
    public static Gson providesGson(
            final Map<Type, TypeAdapter<?>> typeAdapters,
            final Set<TypeAdapterFactory> typeAdapterFactories) {
        final GsonBuilder gsonBuilder = new GsonBuilder();

        // Register adapters and adapter factories supplied by provider
        typeAdapters.forEach(gsonBuilder::registerTypeAdapter);
        typeAdapterFactories.forEach(gsonBuilder::registerTypeAdapterFactory);

        // Register Immutables generated adapter factories
        // See: https://immutables.github.io/json.html#type-adapter-registration)
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);

        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    @Provides
    public static Map<Type, TypeAdapter<?>> providesTypeAdapters() {
        return Collections.singletonMap(Duration.class, new DurationTypeAdapter());
    }

    @Provides
    @ElementsIntoSet
    public static Set<TypeAdapterFactory> providesTypeAdapterFactories() {
        return ImmutableSet.of(
                new GsonAdaptersLength(),
                new GsonAdaptersPoint());
    }

}
