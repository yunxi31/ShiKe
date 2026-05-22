package com.pomodoroalert.ui;

import com.pomodoroalert.data.TaskRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AlarmWakeUpActivity_MembersInjector implements MembersInjector<AlarmWakeUpActivity> {
  private final Provider<TaskRepository> taskRepoProvider;

  public AlarmWakeUpActivity_MembersInjector(Provider<TaskRepository> taskRepoProvider) {
    this.taskRepoProvider = taskRepoProvider;
  }

  public static MembersInjector<AlarmWakeUpActivity> create(
      Provider<TaskRepository> taskRepoProvider) {
    return new AlarmWakeUpActivity_MembersInjector(taskRepoProvider);
  }

  @Override
  public void injectMembers(AlarmWakeUpActivity instance) {
    injectTaskRepo(instance, taskRepoProvider.get());
  }

  @InjectedFieldSignature("com.pomodoroalert.ui.AlarmWakeUpActivity.taskRepo")
  public static void injectTaskRepo(AlarmWakeUpActivity instance, TaskRepository taskRepo) {
    instance.taskRepo = taskRepo;
  }
}
