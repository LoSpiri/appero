package com.example.apper.di

import android.app.Application
import androidx.room.Room
import com.example.apper.data.TodoDatabase
import com.example.apper.data.TodoRepo
import com.example.apper.data.TodoRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(app: Application): TodoDatabase {
        return Room.databaseBuilder(
            app,
            TodoDatabase::class.java,
            "todo_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(db:TodoDatabase): TodoRepo {
        return TodoRepoImpl(db.dao)
    }
}