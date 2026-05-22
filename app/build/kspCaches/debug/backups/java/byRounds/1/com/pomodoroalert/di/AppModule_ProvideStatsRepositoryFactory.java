package com.pomodoroalert.di;

import com.pomodoroalert.data.StatsRepository;
import com.pomodoroalert.data.TaskDao;
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
public final class AppModule_ProvideStatsRepositoryFactory implements Factory<StatsRepository> {
  private final Provider<TaskDao> taskDaoProvider;

  public AppModule_ProvideStatsRepositoryFactory(Provider<TaskDao> taskDaoProvider) {
    this.taskDaoProvider = taskDaoProvider;
  }

  @Override
  public StatsRepository get() {
    return provideStatsRepository(taskDaoProvider.get());
  }

  public static AppModule_ProvideStatsRepositoryFactory create(Provider<TaskDao> taskDaoProvider) {
    return new AppModule_ProvideStatsRepositoryFactory(taskDaoProvider);
  }

  public static StatsRepository provideStatsRepository(TaskDao taskDao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStatsRepository(taskDao));
  }
}
