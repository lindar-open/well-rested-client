package com.lindar.wellrested.vo

import com.google.gson.reflect.TypeToken

inline fun <reified T> WellRestedResponse.JsonResponseMapper.castTo(): T = castTo(object: TypeToken<T>() {}) as T
inline fun <reified T> WellRestedResponse.JsonResponseMapper.castToList(): List<T> = castToList(T::class.java) as List<T>