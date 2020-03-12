package com.lindar.wellrested.vo

import com.google.gson.reflect.TypeToken

inline fun <reified T> WellRestedResponse.JsonResponseMapper.castTo(): T = castTo(T::class.java) as T
inline fun <reified T> WellRestedResponse.JsonResponseMapper.castToList(): List<T> = castToList(object : TypeToken<List<T>>() {}) as List<T>

inline fun <reified T> WellRestedResponse.XmlResponseMapper.castTo(): T = castTo(T::class.java) as T
