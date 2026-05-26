package com.pomodoroalert.di

import android.content.Context
import androidx.room.Room
import com.pomodoroalert.data.AlarmDao
import com.pomodoroalert.data.AppDatabase
import com.pomodoroalert.data.ConfigRepository
import com.pomodoroalert.data.StatsRepository
import com.pomodoroalert.data.TaskDao
import com.pomodoroalert.data.TaskRepository
import com.pomodoroalert.data.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pomodoro_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideAlarmDao(db: AppDatabase): AlarmDao = db.alarmDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideConfigRepository(userPrefs: UserPreferences): ConfigRepository {
        return ConfigRepository(userPrefs)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(taskDao: TaskDao): StatsRepository {
        return StatsRepository(taskDao)
    }
}
