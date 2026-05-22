package com.pomodoroalert.ui.viewmodel;

import com.pomodoroalert.data.CalendarSyncRepository;
import com.pomodoroalert.data.ConfigRepository;
import com.pomodoroalert.data.TaskRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<TaskRepository> taskRepoProvider;

  private final Provider<ConfigRepository> configRepoProvider;

  private final Provider<CalendarSyncRepository> calendarRepoProvider;

  public HomeViewModel_Factory(Provider<TaskRepository> taskRepoProvider,
      Provider<ConfigRepository> configRepoProvider,
      Provider<CalendarSyncRepository> calendarRepoProvider) {
    this.taskRepoProvider = taskRepoProvider;
    this.configRepoProvider = configRepoProvider;
    this.calendarRepoProvider = calendarRepoProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(taskRepoProvider.get(), configRepoProvider.get(), calendarRepoProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<TaskRepository> taskRepoProvider,
      Provider<ConfigRepository> configRepoProvider,
      Provider<CalendarSyncRepository> calendarRepoProvider) {
    return new HomeViewModel_Factory(taskRepoProvider, configRepoProvider, calendarRepoProvider);
  }

  public static HomeViewModel newInstance(TaskRepository taskRepo, ConfigRepository configRepo,
      CalendarSyncRepository calendarRepo) {
    return new HomeViewModel(taskRepo, configRepo, calendarRepo);
  }
}
