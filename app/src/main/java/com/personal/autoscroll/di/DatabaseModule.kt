package com.personal.autoscroll.di

import android.content.Context
import androidx.room.Room
import com.personal.autoscroll.data.db.AppProfileDao
import com.personal.autoscroll.data.db.AutoScrollDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AutoScrollDatabase = Room.databaseBuilder(
        context,
        AutoScrollDatabase::class.java,
        "auto_scroll.db",
    ).build()

    @Provides
    fun provideAppProfileDao(database: AutoScrollDatabase): AppProfileDao =
        database.appProfileDao()
}
