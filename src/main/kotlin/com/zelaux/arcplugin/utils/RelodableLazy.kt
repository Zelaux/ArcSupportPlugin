package com.zelaux.arcplugin.utils

import arc.util.Reflect
import java.io.Serializable
import kotlin.reflect.KProperty

fun <T> relodableLazy(initializer: () -> T) = RelodableLazy(initializer)
class RelodableLazy<out T>(initializer: () -> T) : Lazy<T>, Serializable {
    private var initializer: (() -> T) = initializer

    @Volatile
    private var _value: Any? = uninitializedObj

    // final field is required to enable safe publication of constructed instance
    private val lock = this

    companion object UninitializedValueContainer {
        public val uninitializedObj: Any;

        init {

            val uninitialized = Lazy::class.java.classLoader.loadClass("kotlin.UNINITIALIZED_VALUE")
            uninitializedObj = Reflect.get<Any>(uninitialized.getField("INSTANCE"))
        }

        private fun getDelegate(obj: Any, property: KProperty<*>) = Reflect.get<Any>(obj, property.name + "\$delegate")
        fun resetValue(obj: Any, kProperty: KProperty<*>) {
            Reflect.set(getDelegate(obj, kProperty), "_value", uninitializedObj)
        }
    }

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== uninitializedObj) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== uninitializedObj) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    val typedValue = initializer()
                    _value = typedValue
//                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== uninitializedObj

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

}