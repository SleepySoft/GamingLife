package graphengine


inline fun <reified T> typeName(): String = T::class.simpleName ?: ""

class GraphService {
    interface WrapperBase {
        fun prob() : List< String >
        fun invoke(vararg values: Any) : Any
    }

    class WrapperT0(val f: () -> Any) : WrapperBase {
        override fun prob() : List< String > = listOf()
        override fun invoke(vararg values: Any) : Any  = f()
    }

    class WrapperT1< T1 >(val f: (t1: T1) -> Any) : WrapperBase {
        override fun prob(): List<String> = listOf(typeName<T1>())
        override fun invoke(vararg v: Any) : Any  = f(v[0] as T1)
    }

    fun <T1> wrapper(f: (T1) -> Any, vararg values: Any): () -> Any = { f(values[0] as T1) }
    fun <T1, T2> wrapper(f: (T1, T2) -> Any, vararg values: Any): () -> Any = { f(values[0] as T1, values[1] as T2) }

    private val serviceMap = mutableMapOf<String, (Array<out Any>) -> Any>()

    fun <T1> serviceRegister(serviceName: String, serviceFunction: (T1) -> Any) {
        serviceMap[serviceName] = { vararg values: Any }
    }

    fun serviceUnregister(serviceName: String) {
        serviceMap.remove(serviceName)
    }

    fun serviceCall(serviceName: String, vararg params: Any): Any? {
        val serviceFunction = serviceMap[serviceName] ?: return null
        return serviceFunction(params)
    }
}