package com.lutukai.simpletodoapp.util.schedulerprovider

import io.reactivex.rxjava3.core.Scheduler

interface SchedulerProvider {
    fun io(): Scheduler
    fun ui(): Scheduler
    fun computation(): Scheduler
}