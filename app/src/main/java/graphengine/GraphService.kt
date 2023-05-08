package graphengine


// inline fun <reified T> typeName(): String = T::class.simpleName ?: ""

class GraphService {
    interface WrapperBase {
        // Can't find the way to prob
        // fun prob() : List< String >
        fun invoke(vararg v: Any) : Any
    }

    class WrapperT0(val f: () -> Any) : WrapperBase {
        override fun invoke(vararg v: Any) : Any  = f()
    }

    class WrapperT1< T1 >(val f: (t1: T1) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any) : Any  = f(v[0] as T1)
    }
    class WrapperT2<T1, T2>(val f: (t1: T1, t2: T2) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any): Any = f(v[0] as T1, v[1] as T2)
    }

    class WrapperT3<T1, T2, T3>(val f: (t1: T1, t2: T2, t3: T3) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any): Any = f(v[0] as T1, v[1] as T2, v[2] as T3)
    }

    class WrapperT4<T1, T2, T3, T4>(val f: (t1: T1, t2: T2, t3: T3, t4: T4) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any): Any = f(v[0] as T1, v[1] as T2, v[2] as T3, v[3] as T4)
    }

    class WrapperT5<T1, T2, T3, T4, T5>(val f: (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any): Any = f(v[0] as T1, v[1] as T2, v[2] as T3, v[3] as T4, v[4] as T5)
    }

    class WrapperT6<T1, T2, T3, T4, T5, T6>(val f: (t1: T1, t2: T2, t3: T3, t4: T4,t5:T5,t6:T6) -> Any) : WrapperBase {
        override fun invoke(vararg v: Any): Any = f(v[0] as T1,v[1] as 	T2,v[2] as 	T3,v[3] as 	T4,v[4] as 	T5,v[5] as 	T6)
    }

    private val serviceMap = mutableMapOf<String, WrapperBase>()

    fun serviceRegister(serviceName: String, wrapper: WrapperBase) {
        serviceMap[serviceName] = wrapper
    }

    fun serviceRegister(serviceName: String, serviceFunction: () -> Any) {
        serviceMap[serviceName] = WrapperT0(serviceFunction)
    }

    fun <T1> serviceRegister(serviceName: String, serviceFunction: (T1) -> Any) {
        serviceMap[serviceName] = WrapperT1(serviceFunction)
    }

    fun <T1, T2> serviceRegister(serviceName: String, serviceFunction: (T1, T2) -> Any) {
        serviceMap[serviceName] = WrapperT2(serviceFunction)
    }

    fun <T1, T2, T3> serviceRegister(serviceName: String, serviceFunction: (T1, T2, T3) -> Any) {
        serviceMap[serviceName] = WrapperT3(serviceFunction)
    }

    fun <T1, T2, T3, T4> serviceRegister(serviceName: String, serviceFunction: (T1, T2, T3, T4) -> Any) {
        serviceMap[serviceName] = WrapperT4(serviceFunction)
    }

    fun <T1, T2, T3, T4, T5> serviceRegister(serviceName: String, serviceFunction: (T1, T2, T3, T4, T5) -> Any) {
        serviceMap[serviceName] = WrapperT5(serviceFunction)
    }

    fun <T1,T2,T3,T4,T5,T6> serviceRegister(serviceName:String,serviceFunction:(T1, T2, T3, T4, T5, T6)->Any){
        serviceMap[serviceName]=WrapperT6(serviceFunction)
    }

    fun serviceUnregister(serviceName: String) {
        serviceMap.remove(serviceName)
    }

    fun serviceCall(serviceName: String, vararg params: Any): Any? {
        val serviceFunction = serviceMap[serviceName] ?: return null
        return serviceFunction.invoke(params)
    }
}