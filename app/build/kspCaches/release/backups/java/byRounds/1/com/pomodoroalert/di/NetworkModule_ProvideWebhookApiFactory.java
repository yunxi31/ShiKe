package com.pomodoroalert.di;

import com.pomodoroalert.network.WebhookApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideWebhookApiFactory implements Factory<WebhookApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideWebhookApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public WebhookApi get() {
    return provideWebhookApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideWebhookApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideWebhookApiFactory(retrofitProvider);
  }

  public static WebhookApi provideWebhookApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideWebhookApi(retrofit));
  }
}
