package com.pomodoroalert.di;

import com.pomodoroalert.data.ConfigRepository;
import com.pomodoroalert.data.UserPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideConfigRepositoryFactory implements Factory<ConfigRepository> {
  private final Provider<UserPreferences> userPrefsProvider;

  public AppModule_ProvideConfigRepositoryFactory(Provider<UserPreferences> userPrefsProvider) {
    this.userPrefsProvider = userPrefsProvider;
  }

  @Override
  public ConfigRepository get() {
    return provideConfigRepository(userPrefsProvider.get());
  }

  public static AppModule_ProvideConfigRepositoryFactory create(
      Provider<UserPreferences> userPrefsProvider) {
    return new AppModule_ProvideConfigRepositoryFactory(userPrefsProvider);
  }

  public static ConfigRepository provideConfigRepository(UserPreferences userPrefs) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideConfigRepository(userPrefs));
  }
}
