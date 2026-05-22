package com.pomodoroalert.ui.viewmodel;

import com.pomodoroalert.data.StatsRepository;
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
public final class StatsViewModel_Factory implements Factory<StatsViewModel> {
  private final Provider<StatsRepository> statsRepoProvider;

  public StatsViewModel_Factory(Provider<StatsRepository> statsRepoProvider) {
    this.statsRepoProvider = statsRepoProvider;
  }

  @Override
  public StatsViewModel get() {
    return newInstance(statsRepoProvider.get());
  }

  public static StatsViewModel_Factory create(Provider<StatsRepository> statsRepoProvider) {
    return new StatsViewModel_Factory(statsRepoProvider);
  }

  public static StatsViewModel newInstance(StatsRepository statsRepo) {
    return new StatsViewModel(statsRepo);
  }
}
