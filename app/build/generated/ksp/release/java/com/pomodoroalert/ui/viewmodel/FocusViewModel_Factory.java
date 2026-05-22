package com.pomodoroalert.ui.viewmodel;

import android.content.Context;
import com.pomodoroalert.data.TaskRepository;
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
public final class FocusViewModel_Factory implements Factory<FocusViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<TaskRepository> taskRepoProvider;

  public FocusViewModel_Factory(Provider<Context> contextProvider,
      Provider<TaskRepository> taskRepoProvider) {
    this.contextProvider = contextProvider;
    this.taskRepoProvider = taskRepoProvider;
  }

  @Override
  public FocusViewModel get() {
    return newInstance(contextProvider.get(), taskRepoProvider.get());
  }

  public static FocusViewModel_Factory create(Provider<Context> contextProvider,
      Provider<TaskRepository> taskRepoProvider) {
    return new FocusViewModel_Factory(contextProvider, taskRepoProvider);
  }

  public static FocusViewModel newInstance(Context context, TaskRepository taskRepo) {
    return new FocusViewModel(context, taskRepo);
  }
}
