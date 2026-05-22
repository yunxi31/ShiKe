package com.pomodoroalert.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.pomodoroalert.data.AppDatabase;
import com.pomodoroalert.data.UserPreferences;
import com.pomodoroalert.network.WebhookApi;
import dagger.internal.DaggerGenerated;
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
public final class SyncWorker_Factory {
  private final Provider<AppDatabase> dbProvider;

  private final Provider<WebhookApi> webhookApiProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public SyncWorker_Factory(Provider<AppDatabase> dbProvider,
      Provider<WebhookApi> webhookApiProvider, Provider<UserPreferences> userPreferencesProvider) {
    this.dbProvider = dbProvider;
    this.webhookApiProvider = webhookApiProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  public SyncWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, dbProvider.get(), webhookApiProvider.get(), userPreferencesProvider.get());
  }

  public static SyncWorker_Factory create(Provider<AppDatabase> dbProvider,
      Provider<WebhookApi> webhookApiProvider, Provider<UserPreferences> userPreferencesProvider) {
    return new SyncWorker_Factory(dbProvider, webhookApiProvider, userPreferencesProvider);
  }

  public static SyncWorker newInstance(Context context, WorkerParameters params, AppDatabase db,
      WebhookApi webhookApi, UserPreferences userPreferences) {
    return new SyncWorker(context, params, db, webhookApi, userPreferences);
  }
}
