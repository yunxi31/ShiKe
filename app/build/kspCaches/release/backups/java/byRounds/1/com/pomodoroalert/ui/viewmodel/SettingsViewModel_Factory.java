package com.pomodoroalert.ui.viewmodel;

import com.pomodoroalert.data.ConfigRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<ConfigRepository> configRepoProvider;

  public SettingsViewModel_Factory(Provider<ConfigRepository> configRepoProvider) {
    this.configRepoProvider = configRepoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(configRepoProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<ConfigRepository> configRepoProvider) {
    return new SettingsViewModel_Factory(configRepoProvider);
  }

  public static SettingsViewModel newInstance(ConfigRepository configRepo) {
    return new SettingsViewModel(configRepo);
  }
}
