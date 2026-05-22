package com.pomodoroalert.data;

import android.content.Context;
import com.pomodoroalert.network.WebhookApi;
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
public final class TaskRepository_Factory implements Factory<TaskRepository> {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<WebhookApi> webhookApiProvider;

  private final Provider<UserPreferences> userPrefsProvider;

  private final Provider<Context> contextProvider;

  public TaskRepository_Factory(Provider<AppDatabase> dbProvider,
      Provider<WebhookApi> webhookApiProvider, Provider<UserPreferences> userPrefsProvider,
      Provider<Context> contextProvider) {
    this.dbProvider = dbProvider;
    this.webhookApiProvider = webhookApiProvider;
    this.userPrefsProvider = userPrefsProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public TaskRepository get() {
    return newInstance(dbProvider.get(), webhookApiProvider.get(), userPrefsProvider.get(), contextProvider.get());
  }

  public static TaskRepository_Factory create(Provider<AppDatabase> dbProvider,
      Provider<WebhookApi> webhookApiProvider, Provider<UserPreferences> userPrefsProvider,
      Provider<Context> contextProvider) {
    return new TaskRepository_Factory(dbProvider, webhookApiProvider, userPrefsProvider, contextProvider);
  }

  public static TaskRepository newInstance(AppDatabase db, WebhookApi webhookApi,
      UserPreferences userPrefs, Context context) {
    return new TaskRepository(db, webhookApi, userPrefs, context);
  }
}
