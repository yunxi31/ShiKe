package com.pomodoroalert.di;

import com.pomodoroalert.data.AlarmDao;
import com.pomodoroalert.data.AppDatabase;
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
public final class AppModule_ProvideAlarmDaoFactory implements Factory<AlarmDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAlarmDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlarmDao get() {
    return provideAlarmDao(dbProvider.get());
  }

  public static AppModule_ProvideAlarmDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAlarmDaoFactory(dbProvider);
  }

  public static AlarmDao provideAlarmDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlarmDao(db));
  }
}
