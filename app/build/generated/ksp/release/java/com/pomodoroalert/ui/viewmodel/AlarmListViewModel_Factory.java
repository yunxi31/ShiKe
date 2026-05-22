package com.pomodoroalert.ui.viewmodel;

import android.content.Context;
import com.pomodoroalert.data.AlarmDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AlarmListViewModel_Factory implements Factory<AlarmListViewModel> {
  private final Provider<AlarmDao> alarmDaoProvider;

  private final Provider<Context> contextProvider;

  public AlarmListViewModel_Factory(Provider<AlarmDao> alarmDaoProvider,
      Provider<Context> contextProvider) {
    this.alarmDaoProvider = alarmDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AlarmListViewModel get() {
    return newInstance(alarmDaoProvider.get(), contextProvider.get());
  }

  public static AlarmListViewModel_Factory create(Provider<AlarmDao> alarmDaoProvider,
      Provider<Context> contextProvider) {
    return new AlarmListViewModel_Factory(alarmDaoProvider, contextProvider);
  }

  public static AlarmListViewModel newInstance(AlarmDao alarmDao, Context context) {
    return new AlarmListViewModel(alarmDao, context);
  }
}
