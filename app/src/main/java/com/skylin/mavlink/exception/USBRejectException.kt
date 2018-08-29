package com.skylin.mavlink.exception

/**
 * Created by sjj on 2018/3/29.
 */
class USBRejectException : RuntimeException {
    constructor()
    constructor(message: String? = null) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(throwable: Throwable) : super(throwable)
}