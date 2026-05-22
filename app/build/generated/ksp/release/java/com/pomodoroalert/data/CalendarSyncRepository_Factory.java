package com.pomodoroalert.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CalendarSyncRepository_Factory implements Factory<CalendarSyncRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<TaskRepository> taskRepositoryProvider;

  private final Provider<ConfigRepository> configRepositoryProvider;

  public CalendarSyncRepository_Factory(Provider<Context> contextProvider,
      Provider<TaskRepository> taskRepositoryProvider,
      Provider<ConfigRepository> configRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.taskRepositoryProvider = taskRepositoryProvider;
    this.configRepositoryProvider = configRepositoryProvider;
  }

  @Override
  public CalendarSyncRepository get() {
    return newInstance(contextProvider.get(), taskRepositoryProvider.get(), configRepositoryProvider.get());
  }

  public static CalendarSyncRepository_Factory create(Provider<Context> contextProvider,
      Provider<TaskRepository> taskRepositoryProvider,
      Provider<ConfigRepository> configRepositoryProvider) {
    return new CalendarSyncRepository_Factory(contextProvider, taskRepositoryProvider, configRepositoryProvider);
  }

  public static CalendarSyncRepository newInstance(Context context, TaskRepository taskRepository,
      ConfigRepository configRepository) {
    return new CalendarSyncRepository(context, taskRepository, configRepository);
  }
}
